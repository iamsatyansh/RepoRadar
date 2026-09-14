package com.reporadar.service;

import com.reporadar.api.analysis.AnalysisDto;
import com.reporadar.domain.Analysis;
import com.reporadar.domain.CommitSnapshot;
import com.reporadar.domain.ContributorSnapshot;
import com.reporadar.domain.DomainEnums;
import com.reporadar.domain.Insight;
import com.reporadar.domain.LanguageStat;
import com.reporadar.domain.RepositoryEntity;
import com.reporadar.domain.UserAccount;
import com.reporadar.error.ApiException;
import com.reporadar.github.GitHubClient;
import com.reporadar.github.GitHubModels;
import com.reporadar.repository.AnalysisRepository;
import com.reporadar.repository.RepositoryEntityRepository;
import com.reporadar.repository.UserAccountRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalysisService {
    private final UserAccountRepository users;
    private final RepositoryEntityRepository repositories;
    private final AnalysisRepository analyses;
    private final GitHubClient github;

    public AnalysisService(UserAccountRepository users, RepositoryEntityRepository repositories, AnalysisRepository analyses, GitHubClient github) { this.users = users; this.repositories = repositories; this.analyses = analyses; this.github = github; }

    @Transactional
    public AnalysisDto.AnalysisResponse create(UUID userId, AnalysisDto.CreateAnalysisRequest request) {
        ParsedRepository source = ParsedRepository.parse(request.repositoryUrl());
        UserAccount user = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "The authenticated user no longer exists."));
        GitHubModels.RepositoryPayload payload = github.repository(source.owner(), source.name());
        if (payload.privateRepository()) throw new ApiException(HttpStatus.FORBIDDEN, "PRIVATE_REPOSITORY_UNSUPPORTED", "RepoRadar analyzes public GitHub repositories only.");
        RepositoryEntity repository = repositories.findByGithubId(payload.id()).orElseGet(() -> new RepositoryEntity(payload.id(), payload.owner().login(), payload.name(), payload.htmlUrl()));
        repository.updateMetadata(payload.description(), payload.defaultBranch(), payload.starsCount(), payload.forksCount(), payload.openIssuesCount(), payload.subscribersCount(), payload.language(), payload.pushedAt(), payload.createdAt());
        repository = repositories.save(repository);

        Analysis analysis = analyses.save(new Analysis(repository, user));
        List<GitHubModels.CommitPayload> commits = github.commits(source.owner(), source.name());
        populateLanguages(analysis, github.languages(source.owner(), source.name()));
        populateContributors(analysis, github.contributors(source.owner(), source.name()));
        populateCommits(analysis, commits);
        createInsights(analysis, repository, commits.size());
        analysis.complete(commits.isEmpty() ? null : commits.getFirst().sha());
        return toResponse(analysis);
    }

    @Transactional(readOnly = true)
    public AnalysisDto.AnalysisResponse get(UUID userId, UUID analysisId) {
        return toResponse(findOwnedAnalysis(userId, analysisId));
    }

    @Transactional(readOnly = true)
    public List<AnalysisDto.AnalysisResponse> list(UUID userId) {
        return analyses.findByRequestedByIdOrderByRequestedAtDesc(userId, PageRequest.of(0, 100)).map(this::toResponse).getContent();
    }

    private Analysis findOwnedAnalysis(UUID userId, UUID analysisId) {
        return analyses.findByIdAndRequestedById(analysisId, userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ANALYSIS_NOT_FOUND", "No analysis record was found for this workspace."));
    }

    private void populateLanguages(Analysis analysis, Map<String, Long> languages) {
        long totalBytes = languages.values().stream().mapToLong(Long::longValue).sum();
        languages.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).forEach(entry -> {
            BigDecimal percentage = totalBytes == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(entry.getValue()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalBytes), 2, RoundingMode.HALF_UP);
            analysis.addLanguage(new LanguageStat(analysis, entry.getKey(), entry.getValue(), percentage));
        });
    }

    private void populateContributors(Analysis analysis, List<GitHubModels.ContributorPayload> contributors) {
        contributors.stream().sorted(Comparator.comparingInt(GitHubModels.ContributorPayload::contributions).reversed()).forEach(contributor -> analysis.addContributor(new ContributorSnapshot(analysis, contributor.id(), contributor.login(), contributor.avatarUrl(), contributor.contributions())));
    }

    private void populateCommits(Analysis analysis, List<GitHubModels.CommitPayload> commits) {
        commits.forEach(commit -> {
            String author = commit.author() == null ? null : commit.author().login();
            Instant committedAt = commit.commit() == null || commit.commit().author() == null || commit.commit().author().date() == null ? Instant.now() : commit.commit().author().date();
            String message = commit.commit() == null || commit.commit().message() == null ? "No commit message supplied" : commit.commit().message();
            analysis.addRecentCommit(new CommitSnapshot(analysis, commit.sha(), message, author, committedAt));
        });
    }

    private void createInsights(Analysis analysis, RepositoryEntity repository, int commitCount) {
        analysis.addInsight(new Insight(analysis, "RECENT_COMMIT_SNAPSHOT", DomainEnums.InsightSeverity.INFO, "Captured recent development activity", commitCount + " recent commits were available from GitHub when this snapshot was created."));
        if (repository.getPushedAt() == null) analysis.addInsight(new Insight(analysis, "PUSH_TIME_UNAVAILABLE", DomainEnums.InsightSeverity.NOTICE, "Push timestamp is unavailable", "GitHub did not supply a last-push timestamp for this repository snapshot."));
        else if (repository.getPushedAt().isBefore(Instant.now().minusSeconds(180L * 24 * 60 * 60))) analysis.addInsight(new Insight(analysis, "ACTIVITY_MAY_BE_STALE", DomainEnums.InsightSeverity.NOTICE, "Recent push activity may be stale", "The repository’s last push timestamp was more than 180 days before this analysis."));
        if (analysis.getLanguages().isEmpty()) analysis.addInsight(new Insight(analysis, "LANGUAGE_DATA_UNAVAILABLE", DomainEnums.InsightSeverity.NOTICE, "Language composition is unavailable", "GitHub returned no language byte counts for this repository."));
        else analysis.addInsight(new Insight(analysis, "LANGUAGE_COMPOSITION_CAPTURED", DomainEnums.InsightSeverity.INFO, "Language composition captured", analysis.getLanguages().size() + " language measurements were captured from GitHub byte counts."));
    }

    private AnalysisDto.AnalysisResponse toResponse(Analysis analysis) {
        RepositoryEntity repository = analysis.getRepository();
        return new AnalysisDto.AnalysisResponse(analysis.getId(), analysis.getStatus(), analysis.getRequestedAt(), analysis.getCompletedAt(), new AnalysisDto.RepositoryResponse(repository.getOwnerLogin(), repository.getName(), repository.getFullName(), repository.getHtmlUrl(), repository.getDescription(), repository.getStarsCount(), repository.getForksCount(), repository.getOpenIssuesCount(), repository.getWatchersCount(), repository.getPrimaryLanguage(), repository.getDefaultBranch(), repository.getPushedAt(), repository.getGithubCreatedAt()), analysis.getAnalyzedCommitSha(), analysis.getLanguages().stream().map(language -> new AnalysisDto.LanguageResponse(language.getLanguageName(), language.getBytes(), language.getPercentage())).toList(), analysis.getContributors().stream().map(contributor -> new AnalysisDto.ContributorResponse(contributor.getLogin(), contributor.getAvatarUrl(), contributor.getCommitCount())).toList(), analysis.getRecentCommits().stream().sorted(Comparator.comparing(CommitSnapshot::getCommittedAt).reversed()).map(commit -> new AnalysisDto.CommitResponse(commit.getSha(), commit.getMessage(), commit.getAuthorLogin(), commit.getCommittedAt())).toList(), activity(analysis.getRecentCommits()), analysis.getInsights().stream().map(insight -> new AnalysisDto.InsightResponse(insight.getCode(), insight.getSeverity(), insight.getTitle(), insight.getDescription())).toList());
    }

    private List<AnalysisDto.ActivityPoint> activity(List<CommitSnapshot> commits) {
        Map<String, Long> weeks = commits.stream().collect(java.util.stream.Collectors.groupingBy(commit -> commit.getCommittedAt().atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString(), LinkedHashMap::new, java.util.stream.Collectors.counting()));
        return weeks.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> new AnalysisDto.ActivityPoint(entry.getKey(), entry.getValue())).toList();
    }

    private record ParsedRepository(String owner, String name) {
        static ParsedRepository parse(String url) {
            try {
                URI uri = URI.create(url.trim());
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || !(uri.getHost().equalsIgnoreCase("github.com") || uri.getHost().equalsIgnoreCase("www.github.com"))) throw invalidUrl();
                String[] parts = uri.getPath().replaceFirst("^/", "").replaceFirst("/$", "").split("/");
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) throw invalidUrl();
                String name = parts[1].endsWith(".git") ? parts[1].substring(0, parts[1].length() - 4) : parts[1];
                if (name.isBlank()) throw invalidUrl();
                return new ParsedRepository(parts[0].toLowerCase(Locale.ROOT), name.toLowerCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) { throw invalidUrl(); }
        }
        private static ApiException invalidUrl() { return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REPOSITORY_URL", "Provide a canonical public GitHub repository URL, for example https://github.com/owner/repository."); }
    }
}
