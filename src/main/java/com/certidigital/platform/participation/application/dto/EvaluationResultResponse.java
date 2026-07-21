package com.certidigital.platform.participation.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EvaluationResultResponse {

    private String id;
    private String tenantId;
    private String enrollmentId;
    private String evaluationName;
    private String evaluationType;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal passingScore;
    private Boolean passed;
    private LocalDateTime recordedAt;

    public EvaluationResultResponse() {}

    public EvaluationResultResponse(
        String id, String tenantId, String enrollmentId, String evaluationName,
        String evaluationType, BigDecimal score, BigDecimal maxScore,
        BigDecimal passingScore, Boolean passed, LocalDateTime recordedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.enrollmentId = enrollmentId;
        this.evaluationName = evaluationName;
        this.evaluationType = evaluationType;
        this.score = score;
        this.maxScore = maxScore;
        this.passingScore = passingScore;
        this.passed = passed;
        this.recordedAt = recordedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getEvaluationName() { return evaluationName; }
    public void setEvaluationName(String evaluationName) { this.evaluationName = evaluationName; }

    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public BigDecimal getPassingScore() { return passingScore; }
    public void setPassingScore(BigDecimal passingScore) { this.passingScore = passingScore; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
