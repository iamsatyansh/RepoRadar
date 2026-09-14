package com.reporadar.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analyses")
public class Analysis extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private UserAccount requestedBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DomainEnums.AnalysisStatus status = DomainEnums.AnalysisStatus.PENDING;
    @Column(name = "analyzed_commit_sha", length = 64)
    private String analyzedCommitSha;
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "error_code", length = 64)
    private String errorCode;
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LanguageStat> languages = new ArrayList<>();
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContributorSnapshot> contributors = new ArrayList<>();
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommitSnapshot> recentCommits = new ArrayList<>();
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Insight> insights = new ArrayList<>();

    protected Analysis() { }
    public Analysis(RepositoryEntity repository, UserAccount requestedBy) { this.repository = repository; this.requestedBy = requestedBy; }
    public void complete(String commitSha) { this.status = DomainEnums.AnalysisStatus.COMPLETED; this.analyzedCommitSha = commitSha; this.completedAt = Instant.now(); this.errorCode = null; this.errorMessage = null; }
    public void fail(String code, String message) { this.status = DomainEnums.AnalysisStatus.FAILED; this.errorCode = code; this.errorMessage = message; this.completedAt = Instant.now(); }
    public void addLanguage(LanguageStat language) { languages.add(language); }
    public void addContributor(ContributorSnapshot contributor) { contributors.add(contributor); }
    public void addRecentCommit(CommitSnapshot commit) { recentCommits.add(commit); }
    public void addInsight(Insight insight) { insights.add(insight); }
    public RepositoryEntity getRepository() { return repository; }
    public UserAccount getRequestedBy() { return requestedBy; }
    public DomainEnums.AnalysisStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getAnalyzedCommitSha() { return analyzedCommitSha; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public List<LanguageStat> getLanguages() { return List.copyOf(languages); }
    public List<ContributorSnapshot> getContributors() { return List.copyOf(contributors); }
    public List<CommitSnapshot> getRecentCommits() { return List.copyOf(recentCommits); }
    public List<Insight> getInsights() { return List.copyOf(insights); }
}
