package com.reporadar.repository;

import com.reporadar.domain.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, UUID> {
    Optional<RepositoryEntity> findByGithubId(long githubId);
    Optional<RepositoryEntity> findByFullNameIgnoreCase(String fullName);
    boolean existsByGithubId(long githubId);
}
