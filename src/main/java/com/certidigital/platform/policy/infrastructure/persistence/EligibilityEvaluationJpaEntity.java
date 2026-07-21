package com.certidigital.platform.policy.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "eligibility_evaluations",
    indexes = {
        @Index(name = "idx_eligibility_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_eligibility_tenant",     columnList = "tenant_id"),
        @Index(name = "idx_eligibility_result",     columnList = "result")
    }
)
public class EligibilityEvaluationJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "enrollment_id", length = 36, nullable = false)
    private String enrollmentId;

    @Column(name = "policy_id", length = 36, nullable = false)
    private String policyId;

    @Column(name = "result", length = 30, nullable = false)
    private String result;

    @Column(name = "condition_results", columnDefinition = "TEXT", nullable = false)
    private String conditionResults;

    @Column(name = "evidence_snapshot", columnDefinition = "TEXT", nullable = false)
    private String evidenceSnapshot;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.evaluatedAt == null) this.evaluatedAt = LocalDateTime.now();
    }

    public EligibilityEvaluationJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getConditionResults() { return conditionResults; }
    public void setConditionResults(String conditionResults) { this.conditionResults = conditionResults; }

    public String getEvidenceSnapshot() { return evidenceSnapshot; }
    public void setEvidenceSnapshot(String evidenceSnapshot) { this.evidenceSnapshot = evidenceSnapshot; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
