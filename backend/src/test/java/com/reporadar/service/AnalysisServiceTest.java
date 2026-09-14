package com.reporadar.service;

import com.reporadar.api.analysis.AnalysisDto;
import com.reporadar.error.ApiException;
import com.reporadar.github.GitHubClient;
import com.reporadar.repository.AnalysisRepository;
import com.reporadar.repository.RepositoryEntityRepository;
import com.reporadar.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AnalysisServiceTest {
    private final AnalysisService service = new AnalysisService(mock(UserAccountRepository.class), mock(RepositoryEntityRepository.class), mock(AnalysisRepository.class), mock(GitHubClient.class));

    @Test
    void rejectsNonCanonicalOrNonGithubRepositoryUrlsBeforeAnyExternalCall() {
        assertThatThrownBy(() -> service.create(UUID.randomUUID(), new AnalysisDto.CreateAnalysisRequest("https://gitlab.com/owner/repository")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("canonical public GitHub repository URL");
        assertThatThrownBy(() -> service.create(UUID.randomUUID(), new AnalysisDto.CreateAnalysisRequest("https://github.com/owner/repository/issues")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("canonical public GitHub repository URL");
    }
}
