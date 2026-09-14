package com.reporadar.github;

import com.reporadar.config.GitHubProperties;
import com.reporadar.error.ApiException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GitHubClient {
    private final RestClient client;
    private final GitHubProperties properties;

    public GitHubClient(RestClient gitHubRestClient, GitHubProperties properties) { this.client = gitHubRestClient; this.properties = properties; }

    @Cacheable(cacheNames = "githubRepository", key = "#owner + '/' + #repository")
    public GitHubModels.RepositoryPayload repository(String owner, String repository) {
        return exchange(() -> client.get().uri("/repos/{owner}/{repository}", owner, repository).retrieve().body(GitHubModels.RepositoryPayload.class));
    }

    @Cacheable(cacheNames = "githubLanguages", key = "#owner + '/' + #repository")
    public Map<String, Long> languages(String owner, String repository) {
        Map<String, Long> result = exchange(() -> client.get().uri("/repos/{owner}/{repository}/languages", owner, repository).retrieve().body(new ParameterizedTypeReference<Map<String, Long>>() { }));
        return result == null ? Map.of() : result;
    }

    @Cacheable(cacheNames = "githubCommits", key = "#owner + '/' + #repository")
    public List<GitHubModels.CommitPayload> commits(String owner, String repository) {
        List<GitHubModels.CommitPayload> result = exchange(() -> client.get().uri(uri -> uri.path("/repos/{owner}/{repository}/commits").queryParam("per_page", properties.commitLimit()).build(owner, repository)).retrieve().body(new ParameterizedTypeReference<List<GitHubModels.CommitPayload>>() { }));
        return result == null ? List.of() : result;
    }

    @Cacheable(cacheNames = "githubContributors", key = "#owner + '/' + #repository")
    public List<GitHubModels.ContributorPayload> contributors(String owner, String repository) {
        List<GitHubModels.ContributorPayload> result = exchange(() -> client.get().uri(uri -> uri.path("/repos/{owner}/{repository}/contributors").queryParam("per_page", properties.contributorLimit()).build(owner, repository)).retrieve().body(new ParameterizedTypeReference<List<GitHubModels.ContributorPayload>>() { }));
        return result == null ? List.of() : result;
    }

    private <T> T exchange(ExternalCall<T> call) {
        try { return call.execute(); }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) throw new ApiException(HttpStatus.NOT_FOUND, "GITHUB_REPOSITORY_NOT_FOUND", "GitHub could not find this public repository.");
            if (exception.getStatusCode() == HttpStatus.FORBIDDEN && "0".equals(exception.getResponseHeaders().getFirst("X-RateLimit-Remaining"))) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "GITHUB_RATE_LIMITED", "GitHub rate limit reached. Please try again later.");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "GITHUB_API_ERROR", "GitHub did not return a usable repository response.");
        }
        catch (Exception exception) { throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "GITHUB_UNAVAILABLE", "GitHub is temporarily unavailable. Please try again shortly."); }
    }

    @FunctionalInterface private interface ExternalCall<T> { T execute(); }
}
