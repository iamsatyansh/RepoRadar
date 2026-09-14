package com.reporadar.github;

import com.reporadar.config.CacheConfiguration;
import com.reporadar.config.GitHubClientConfiguration;
import com.reporadar.config.GitHubProperties;
import com.reporadar.error.ApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(GitHubClientTest.TestConfig.class)
class GitHubClientTest {
    @Autowired private GitHubClient client;
    @Autowired private MockWebServer github;

    @AfterEach
    void drainRequests() throws InterruptedException {
        while (github.takeRequest(25, TimeUnit.MILLISECONDS) != null) { }
    }

    @Test
    void mapsRepositoryMetadataAndSendsRequiredGitHubHeaders() throws Exception {
        github.enqueue(json(200, """
                {"id":42,"owner":{"login":"octo"},"name":"radar","full_name":"octo/radar","html_url":"https://github.com/octo/radar","description":"Evidence","default_branch":"main","stargazers_count":12,"forks_count":3,"open_issues_count":4,"subscribers_count":5,"private":false,"language":"Java","pushed_at":"2026-08-25T12:00:00Z","created_at":"2024-01-01T00:00:00Z"}
                """));

        GitHubModels.RepositoryPayload payload = client.repository("octo", "radar");

        assertThat(payload.id()).isEqualTo(42L);
        assertThat(payload.fullName()).isEqualTo("octo/radar");
        assertThat(payload.pushedAt()).isEqualTo("2026-08-25T12:00:00Z");
        RecordedRequest request = github.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/repos/octo/radar");
        assertThat(request.getHeader("Accept")).contains("application/vnd.github+json");
        assertThat(request.getHeader("X-GitHub-Api-Version")).isEqualTo("2022-11-28");
        assertThat(request.getHeader("User-Agent")).isEqualTo("RepoRadar/1.0");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer read-only-token");
    }

    @Test
    void readsLanguagesCommitsAndContributorsUsingBoundedPagination() throws Exception {
        github.enqueue(json(200, "{\"Java\":900,\"TypeScript\":100}"));
        github.enqueue(json(200, "[{\"sha\":\"abc\",\"commit\":{\"message\":\"ship it\",\"author\":{\"name\":\"Ada\",\"date\":\"2026-08-25T12:00:00Z\"}},\"author\":{\"login\":\"ada\"}}]"));
        github.enqueue(json(200, "[{\"id\":7,\"login\":\"ada\",\"avatar_url\":\"https://avatars.example/ada\",\"contributions\":11}]"));

        Map<String, Long> languages = client.languages("octo", "signals");
        List<GitHubModels.CommitPayload> commits = client.commits("octo", "signals");
        List<GitHubModels.ContributorPayload> contributors = client.contributors("octo", "signals");

        assertThat(languages).containsEntry("Java", 900L);
        assertThat(commits).singleElement().satisfies(commit -> { assertThat(commit.sha()).isEqualTo("abc"); assertThat(commit.author().login()).isEqualTo("ada"); });
        assertThat(contributors).singleElement().satisfies(contributor -> { assertThat(contributor.login()).isEqualTo("ada"); assertThat(contributor.contributions()).isEqualTo(11); });
        assertThat(github.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/repos/octo/signals/languages");
        assertThat(github.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/repos/octo/signals/commits?per_page=20");
        assertThat(github.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/repos/octo/signals/contributors?per_page=10");
    }

    @Test
    void cachesARepeatedRepositoryLookupInsideTheConfiguredCacheWindow() throws Exception {
        github.enqueue(json(200, "{\"id\":99,\"owner\":{\"login\":\"cache\"},\"name\":\"probe\",\"full_name\":\"cache/probe\",\"html_url\":\"https://github.com/cache/probe\",\"stargazers_count\":0,\"forks_count\":0,\"open_issues_count\":0,\"subscribers_count\":0,\"private\":false}"));

        client.repository("cache", "probe");
        client.repository("cache", "probe");

        assertThat(github.takeRequest(1, TimeUnit.SECONDS)).isNotNull();
        assertThat(github.takeRequest(100, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void mapsGitHubNotFoundToProductNotFound() {
        github.enqueue(json(404, "{\"message\":\"Not Found\"}"));

        assertThatThrownBy(() -> client.repository("missing", "repository"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(404);
                    assertThat(exception.getCode()).isEqualTo("GITHUB_REPOSITORY_NOT_FOUND");
                });
    }

    @Test
    void mapsExhaustedGitHubRateLimitToRetryableProductError() {
        github.enqueue(json(403, "{\"message\":\"rate limit exceeded\"}").addHeader("X-RateLimit-Remaining", "0"));

        assertThatThrownBy(() -> client.languages("limited", "repository"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(429);
                    assertThat(exception.getCode()).isEqualTo("GITHUB_RATE_LIMITED");
                });
    }

    @Test
    void mapsOtherUpstreamFailuresToBadGatewayAndTransportFailuresToServiceUnavailable() {
        github.enqueue(json(500, "{\"message\":\"Internal Server Error\"}"));
        assertThatThrownBy(() -> client.contributors("broken", "upstream"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(502);
                    assertThat(exception.getCode()).isEqualTo("GITHUB_API_ERROR");
                });

        GitHubProperties unreachable = new GitHubProperties("http://127.0.0.1:1", "", "2022-11-28", Duration.ofMillis(50), 1, 1);
        GitHubClient unavailableClient = new GitHubClient(RestClient.builder().baseUrl(unreachable.apiBaseUrl()).build(), unreachable);
        assertThatThrownBy(() -> unavailableClient.repository("offline", "upstream"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(503);
                    assertThat(exception.getCode()).isEqualTo("GITHUB_UNAVAILABLE");
                });
    }

    private static MockResponse json(int status, String body) { return new MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body); }

    @Configuration
    @EnableCaching
    @Import({CacheConfiguration.class, GitHubClientConfiguration.class})
    static class TestConfig {
        @Bean RestClient.Builder restClientBuilder() { return RestClient.builder(); }
        @Bean(destroyMethod = "shutdown")
        MockWebServer mockWebServer() throws IOException { MockWebServer server = new MockWebServer(); server.start(); return server; }
        @Bean GitHubProperties gitHubProperties(MockWebServer server) { return new GitHubProperties(server.url("/").toString().replaceAll("/$", ""), "read-only-token", "2022-11-28", Duration.ofSeconds(1), 20, 10); }
        @Bean GitHubClient gitHubClient(RestClient gitHubRestClient, GitHubProperties properties) { return new GitHubClient(gitHubRestClient, properties); }
    }
}
