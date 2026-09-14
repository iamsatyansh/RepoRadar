package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "commit_snapshots", uniqueConstraints = @UniqueConstraint(name = "uk_commit_snapshot_analysis_sha", columnNames = {"analysis_id", "sha"}))
public class CommitSnapshot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;
    @Column(nullable = false, length = 64)
    private String sha;
    @Column(nullable = false, columnDefinition = "text")
    private String message;
    @Column(name = "author_login", length = 100)
    private String authorLogin;
    @Column(name = "committed_at", nullable = false)
    private Instant committedAt;
    @Column(nullable = false)
    private long additions;
    @Column(nullable = false)
    private long deletions;

    protected CommitSnapshot() { }
    public CommitSnapshot(Analysis analysis, String sha, String message, Instant committedAt) { this.analysis = analysis; this.sha = sha; this.message = message; this.committedAt = committedAt; }
    public CommitSnapshot(Analysis analysis, String sha, String message, String authorLogin, Instant committedAt) { this(analysis, sha, message, committedAt); this.authorLogin = authorLogin; }
    public String getSha() { return sha; }
    public String getMessage() { return message; }
    public String getAuthorLogin() { return authorLogin; }
    public Instant getCommittedAt() { return committedAt; }
}
