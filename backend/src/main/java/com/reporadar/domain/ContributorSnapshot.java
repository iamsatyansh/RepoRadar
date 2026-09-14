package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "contributor_snapshots", uniqueConstraints = @UniqueConstraint(name = "uk_contributor_snapshot_analysis_login", columnNames = {"analysis_id", "login"}))
public class ContributorSnapshot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;
    @Column(name = "github_user_id")
    private Long githubUserId;
    @Column(nullable = false, length = 100)
    private String login;
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;
    @Column(name = "commit_count", nullable = false)
    private int commitCount;
    @Column(nullable = false)
    private long additions;
    @Column(nullable = false)
    private long deletions;

    protected ContributorSnapshot() { }
    public ContributorSnapshot(Analysis analysis, String login, int commitCount) { this.analysis = analysis; this.login = login; this.commitCount = commitCount; }
    public ContributorSnapshot(Analysis analysis, long githubUserId, String login, String avatarUrl, int commitCount) { this(analysis, login, commitCount); this.githubUserId = githubUserId; this.avatarUrl = avatarUrl; }
    public String getLogin() { return login; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getCommitCount() { return commitCount; }
}
