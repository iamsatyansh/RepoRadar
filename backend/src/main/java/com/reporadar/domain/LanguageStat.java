package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "language_stats", uniqueConstraints = @UniqueConstraint(name = "uk_language_stats_analysis_language", columnNames = {"analysis_id", "language_name"}))
public class LanguageStat extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;
    @Column(name = "language_name", nullable = false, length = 100)
    private String languageName;
    @Column(nullable = false)
    private long bytes;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    protected LanguageStat() { }
    public LanguageStat(Analysis analysis, String languageName, long bytes, BigDecimal percentage) { this.analysis = analysis; this.languageName = languageName; this.bytes = bytes; this.percentage = percentage; }
    public String getLanguageName() { return languageName; }
    public long getBytes() { return bytes; }
    public BigDecimal getPercentage() { return percentage; }
}
