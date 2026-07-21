package com.certidigital.platform.participation.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "enrollments",
    indexes = {
        @Index(name = "idx_enrollments_tenant_id",      columnList = "tenant_id"),
        @Index(name = "idx_enrollments_event_id",       columnList = "event_id"),
        @Index(name = "idx_enrollments_participant_id", columnList = "participant_id"),
        @Index(name = "idx_enrollments_status",         columnList = "tenant_id, status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_enrollment_event_participant",
            columnNames = {"event_id", "participant_id"}
        )
    }
)
public class EnrollmentJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "participant_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_enrollments_participant")
    )
    private ParticipantJpaEntity participant;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "payment_status", length = 30, nullable = false)
    private String paymentStatus = "NOT_REQUIRED";

    @Column(name = "attendance_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal attendancePercentage = BigDecimal.ZERO;

    @Column(name = "overall_score", precision = 6, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "enrolled_by", length = 36)
    private String enrolledBy;

    @OneToMany(
        mappedBy = "enrollment",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<AttendanceRecordJpaEntity> attendanceRecords = new ArrayList<>();

    @OneToMany(
        mappedBy = "enrollment",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<EvaluationResultJpaEntity> evaluationResults = new ArrayList<>();

    @OneToMany(
        mappedBy = "enrollment",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PaymentRecordJpaEntity> paymentRecords = new ArrayList<>();

    public EnrollmentJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public ParticipantJpaEntity getParticipant() { return participant; }
    public void setParticipant(ParticipantJpaEntity participant) { this.participant = participant; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }

    public String getEnrolledBy() { return enrolledBy; }
    public void setEnrolledBy(String enrolledBy) { this.enrolledBy = enrolledBy; }

    public List<AttendanceRecordJpaEntity> getAttendanceRecords() { return attendanceRecords; }
    public void setAttendanceRecords(List<AttendanceRecordJpaEntity> attendanceRecords) { this.attendanceRecords = attendanceRecords; }

    public List<EvaluationResultJpaEntity> getEvaluationResults() { return evaluationResults; }
    public void setEvaluationResults(List<EvaluationResultJpaEntity> evaluationResults) { this.evaluationResults = evaluationResults; }

    public List<PaymentRecordJpaEntity> getPaymentRecords() { return paymentRecords; }
    public void setPaymentRecords(List<PaymentRecordJpaEntity> paymentRecords) { this.paymentRecords = paymentRecords; }
}
