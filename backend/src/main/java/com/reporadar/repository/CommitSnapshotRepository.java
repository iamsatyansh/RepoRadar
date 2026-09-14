package com.reporadar.repository;

import com.reporadar.domain.CommitSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitSnapshotRepository extends JpaRepository<CommitSnapshot, UUID> {
    List<CommitSnapshot> findByAnalysisIdOrderByCommittedAtDesc(UUID analysisId);
}
