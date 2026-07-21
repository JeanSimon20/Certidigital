package com.certidigital.platform.policy.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "issuance_requests",
    indexes = {
        @Index(name = "idx_issuance_requests_tenant",      columnList = "tenant_id"),
        @Index(name = "idx_issuance_requests_status",      columnList = "tenant_id, status"),
        @Index(name = "idx_issuance_requests_event",       columnList = "event_id"),
        @Index(name = "idx_issuance_requests_participant", columnList = "participant_id")
    }
)
public class IssuanceRequestJpaEntity extends AuditableEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "evaluation_id", length = 36, nullable = false)
    private String evaluationId;

    @Column(name = "enrollment_id", length = 36, nullable = false)
    private String enrollmentId;

    @Column(name = "participant_id", length = 36, nullable = false)
    private String participantId;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "policy_id", length = 36, nullable = false)
    private String policyId;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "PENDING";

    @Column(name = "reviewer_user_id", length = 36)
    private String reviewerUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_decision", length = 30)
    private String reviewDecision;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "processing_log", columnDefinition = "TEXT")
    private String processingLog;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public IssuanceRequestJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEvaluationId() { return evaluationId; }
    public void setEvaluationId(String evaluationId) { this.evaluationId = evaluationId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(String reviewerUserId) { this.reviewerUserId = reviewerUserId; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewDecision() { return reviewDecision; }
    public void setReviewDecision(String reviewDecision) { this.reviewDecision = reviewDecision; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public String getProcessingLog() { return processingLog; }
    public void setProcessingLog(String processingLog) { this.processingLog = processingLog; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
