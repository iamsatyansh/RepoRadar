package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "insights")
public class Insight extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;
    @Column(nullable = false, length = 80)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DomainEnums.InsightSeverity severity;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    protected Insight() { }
    public Insight(Analysis analysis, String code, DomainEnums.InsightSeverity severity, String title, String description) { this.analysis = analysis; this.code = code; this.severity = severity; this.title = title; this.description = description; }
    public String getCode() { return code; }
    public DomainEnums.InsightSeverity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
