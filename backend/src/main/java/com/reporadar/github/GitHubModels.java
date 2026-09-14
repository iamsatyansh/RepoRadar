package com.reporadar.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public final class GitHubModels {
    private GitHubModels() { }
    public record RepositoryPayload(
            long id,
            @JsonProperty("owner") OwnerPayload owner,
            String name,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("html_url") String htmlUrl,
            String description,
            @JsonProperty("default_branch") String defaultBranch,
            @JsonProperty("stargazers_count") int starsCount,
            @JsonProperty("forks_count") int forksCount,
            @JsonProperty("open_issues_count") int openIssuesCount,
            @JsonProperty("subscribers_count") int subscribersCount,
            @JsonProperty("private") boolean privateRepository,
            String language,
            @JsonProperty("pushed_at") Instant pushedAt,
            @JsonProperty("created_at") Instant createdAt) { }
    public record OwnerPayload(String login) { }
    public record ContributorPayload(long id, String login, @JsonProperty("avatar_url") String avatarUrl, int contributions) { }
    public record CommitPayload(String sha, CommitDetailPayload commit, UserPayload author) { }
    public record CommitDetailPayload(String message, CommitAuthorPayload author) { }
    public record CommitAuthorPayload(String name, Instant date) { }
    public record UserPayload(String login) { }
}
