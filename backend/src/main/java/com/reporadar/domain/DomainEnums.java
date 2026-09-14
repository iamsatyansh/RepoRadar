package com.reporadar.domain;

public final class DomainEnums {
    private DomainEnums() { }

    public enum UserRole { USER, ADMIN }
    public enum AnalysisStatus { PENDING, RUNNING, COMPLETED, FAILED }
    public enum InsightSeverity { INFO, NOTICE, ATTENTION }
}
