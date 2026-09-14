package com.reporadar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GitHubClientConfiguration {
    @Bean
    RestClient gitHubRestClient(RestClient.Builder builder, GitHubProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.requestTimeout());
        return builder.baseUrl(properties.apiBaseUrl())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", properties.apiVersion())
                .defaultHeader(HttpHeaders.USER_AGENT, "RepoRadar/1.0")
                .requestInterceptor((request, body, execution) -> {
                    if (properties.token() != null && !properties.token().isBlank()) request.getHeaders().setBearerAuth(properties.token());
                    return execution.execute(request, body);
                })
                .build();
    }
}
