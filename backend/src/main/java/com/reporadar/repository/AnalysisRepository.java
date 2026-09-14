package com.reporadar.repository;

import com.reporadar.domain.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Page<Analysis> findByRequestedByIdOrderByRequestedAtDesc(UUID userId, Pageable pageable);
    Optional<Analysis> findByIdAndRequestedById(UUID analysisId, UUID userId);
    Optional<Analysis> findFirstByRepositoryIdOrderByRequestedAtDesc(UUID repositoryId);
}
