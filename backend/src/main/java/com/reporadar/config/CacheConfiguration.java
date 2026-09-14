package com.reporadar.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfiguration {
    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("githubRepository", "githubLanguages", "githubCommits", "githubContributors");
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(Duration.ofMinutes(15)).recordStats());
        return manager;
    }
}
