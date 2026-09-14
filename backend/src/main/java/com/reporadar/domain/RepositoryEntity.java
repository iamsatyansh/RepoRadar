package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repositories")
public class RepositoryEntity extends BaseEntity {
    @Column(name = "github_id", nullable = false, unique = true)
    private long githubId;
    @Column(name = "owner_login", nullable = false, length = 100)
    private String ownerLogin;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "full_name", nullable = false, unique = true, length = 201)
    private String fullName;
    @Column(name = "html_url", nullable = false, length = 512)
    private String htmlUrl;
    @Column(columnDefinition = "text")
    private String description;
    @Column(name = "default_branch", length = 255)
    private String defaultBranch;
    @Column(name = "stars_count", nullable = false)
    private int starsCount;
    @Column(name = "forks_count", nullable = false)
    private int forksCount;
    @Column(name = "open_issues_count", nullable = false)
    private int openIssuesCount;
    @Column(name = "watchers_count", nullable = false)
    private int watchersCount;
    @Column(name = "primary_language", length = 100)
    private String primaryLanguage;
    @Column(name = "pushed_at")
    private Instant pushedAt;
    @Column(name = "github_created_at")
    private Instant githubCreatedAt;
    @OneToMany(mappedBy = "repository")
    private List<Analysis> analyses = new ArrayList<>();

    protected RepositoryEntity() { }
    public RepositoryEntity(long githubId, String ownerLogin, String name, String htmlUrl) { this.githubId = githubId; this.ownerLogin = ownerLogin; this.name = name; this.fullName = ownerLogin + "/" + name; this.htmlUrl = htmlUrl; }
    public void updateMetadata(String description, String defaultBranch, int starsCount, int forksCount, int openIssuesCount, int watchersCount, String primaryLanguage, Instant pushedAt, Instant githubCreatedAt) { this.description = description; this.defaultBranch = defaultBranch; this.starsCount = starsCount; this.forksCount = forksCount; this.openIssuesCount = openIssuesCount; this.watchersCount = watchersCount; this.primaryLanguage = primaryLanguage; this.pushedAt = pushedAt; this.githubCreatedAt = githubCreatedAt; }
    public long getGithubId() { return githubId; }
    public String getOwnerLogin() { return ownerLogin; }
    public String getName() { return name; }
    public String getFullName() { return fullName; }
    public String getHtmlUrl() { return htmlUrl; }
    public String getDescription() { return description; }
    public String getDefaultBranch() { return defaultBranch; }
    public int getStarsCount() { return starsCount; }
    public int getForksCount() { return forksCount; }
    public int getOpenIssuesCount() { return openIssuesCount; }
    public int getWatchersCount() { return watchersCount; }
    public String getPrimaryLanguage() { return primaryLanguage; }
    public Instant getPushedAt() { return pushedAt; }
    public Instant getGithubCreatedAt() { return githubCreatedAt; }
}
