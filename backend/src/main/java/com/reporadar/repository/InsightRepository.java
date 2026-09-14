package com.reporadar.repository;

import com.reporadar.domain.Insight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InsightRepository extends JpaRepository<Insight, UUID> {
    List<Insight> findByAnalysisIdOrderBySeverityAsc(UUID analysisId);
}
