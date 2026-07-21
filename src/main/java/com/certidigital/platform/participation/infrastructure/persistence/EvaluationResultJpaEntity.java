package com.certidigital.platform.participation.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "evaluation_results",
    indexes = {
        @Index(name = "idx_evaluation_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_evaluation_tenant",     columnList = "tenant_id")
    }
)
public class EvaluationResultJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_evaluation_enrollment"))
    private EnrollmentJpaEntity enrollment;

    @Column(name = "evaluation_name", length = 255, nullable = false)
    private String evaluationName;

    @Column(name = "evaluation_type", length = 100, nullable = false)
    private String evaluationType = "EXAM";

    @Column(name = "score", precision = 6, scale = 2, nullable = false)
    private BigDecimal score;

    @Column(name = "max_score", precision = 6, scale = 2, nullable = false)
    private BigDecimal maxScore = new BigDecimal("100.00");

    @Column(name = "passing_score", precision = 6, scale = 2, nullable = false)
    private BigDecimal passingScore = new BigDecimal("60.00");

    @Column(name = "passed", nullable = false)
    private Boolean passed = false;

    @Column(name = "recorded_by", length = 36)
    private String recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }

    public EvaluationResultJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public EnrollmentJpaEntity getEnrollment() { return enrollment; }
    public void setEnrollment(EnrollmentJpaEntity enrollment) { this.enrollment = enrollment; }

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

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
