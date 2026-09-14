package com.reporadar.api.analysis;

import com.reporadar.domain.DomainEnums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AnalysisDto {
    private AnalysisDto() { }
    public record CreateAnalysisRequest(@NotBlank @Size(max = 512) String repositoryUrl) { }
    public record AnalysisResponse(UUID id, DomainEnums.AnalysisStatus status, Instant requestedAt, Instant completedAt, RepositoryResponse repository, String analyzedCommitSha, List<LanguageResponse> languages, List<ContributorResponse> contributors, List<CommitResponse> recentCommits, List<ActivityPoint> activity, List<InsightResponse> insights) { }
    public record RepositoryResponse(String owner, String name, String fullName, String htmlUrl, String description, Integer stars, Integer forks, Integer openIssues, Integer watchers, String primaryLanguage, String defaultBranch, Instant pushedAt, Instant createdAt) { }
    public record LanguageResponse(String name, long bytes, BigDecimal percentage) { }
    public record ContributorResponse(String login, String avatarUrl, int commitCount) { }
    public record CommitResponse(String sha, String message, String authorLogin, Instant committedAt) { }
    public record ActivityPoint(String week, long commits) { }
    public record InsightResponse(String code, DomainEnums.InsightSeverity severity, String title, String description) { }
}
