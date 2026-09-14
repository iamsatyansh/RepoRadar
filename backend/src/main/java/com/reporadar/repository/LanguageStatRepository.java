package com.reporadar.repository;

import com.reporadar.domain.LanguageStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LanguageStatRepository extends JpaRepository<LanguageStat, UUID> {
    List<LanguageStat> findByAnalysisIdOrderByPercentageDesc(UUID analysisId);
}
