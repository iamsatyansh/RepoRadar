package com.reporadar.repository;

import com.reporadar.domain.Analysis;
import com.reporadar.domain.LanguageStat;
import com.reporadar.domain.RepositoryEntity;
import com.reporadar.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceRepositoryIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private RepositoryEntityRepository repositoryEntityRepository;
    @Autowired private AnalysisRepository analysisRepository;
    @Autowired private LanguageStatRepository languageStatRepository;

    @Test
    void repositoriesSupportCaseInsensitiveUserLookupAndOwnedAnalysisQueries() {
        UserAccount owner = userAccountRepository.save(new UserAccount("radar-user", "Radar.User@Example.com", "encoded-password"));
        UserAccount differentUser = userAccountRepository.save(new UserAccount("other-user", "other@example.com", "encoded-password"));
        RepositoryEntity repository = repositoryEntityRepository.save(new RepositoryEntity(641200L, "spring-projects", "spring-boot", "https://github.com/spring-projects/spring-boot"));
        Analysis analysis = analysisRepository.save(new Analysis(repository, owner));

        assertThat(userAccountRepository.findByEmailIgnoreCase("radar.user@example.com")).contains(owner);
        assertThat(analysisRepository.findByIdAndRequestedById(analysis.getId(), owner.getId())).contains(analysis);
        assertThat(analysisRepository.findByIdAndRequestedById(analysis.getId(), differentUser.getId())).isEmpty();
        assertThat(analysisRepository.findByRequestedByIdOrderByRequestedAtDesc(owner.getId(), org.springframework.data.domain.PageRequest.of(0, 10)).getContent()).containsExactly(analysis);
    }

    @Test
    void languageRowsAreConstrainedToOneLanguagePerAnalysisAndReturnedByPercentage() {
        UserAccount owner = userAccountRepository.save(new UserAccount("language-user", "language@example.com", "encoded-password"));
        RepositoryEntity repository = repositoryEntityRepository.save(new RepositoryEntity(641201L, "owner", "repository", "https://github.com/owner/repository"));
        Analysis analysis = analysisRepository.save(new Analysis(repository, owner));
        languageStatRepository.save(new LanguageStat(analysis, "Java", 800L, new BigDecimal("80.00")));
        languageStatRepository.save(new LanguageStat(analysis, "TypeScript", 200L, new BigDecimal("20.00")));

        assertThat(languageStatRepository.findByAnalysisIdOrderByPercentageDesc(analysis.getId()))
                .extracting(LanguageStat::getLanguageName)
                .containsExactly("Java", "TypeScript");
    }
}
