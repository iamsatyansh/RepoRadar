package com.reporadar.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.github")
public record GitHubProperties(
        @NotBlank String apiBaseUrl,
        String token,
        @NotBlank String apiVersion,
        @NotNull Duration requestTimeout,
        @Min(1) @Max(100) int commitLimit,
        @Min(1) @Max(100) int contributorLimit) { }
