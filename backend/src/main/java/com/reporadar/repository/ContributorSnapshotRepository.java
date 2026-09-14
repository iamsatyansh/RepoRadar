package com.reporadar.repository;

import com.reporadar.domain.ContributorSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContributorSnapshotRepository extends JpaRepository<ContributorSnapshot, UUID> {
    List<ContributorSnapshot> findByAnalysisIdOrderByCommitCountDesc(UUID analysisId);
}
