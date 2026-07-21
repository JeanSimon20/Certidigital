package com.certidigital.platform.participation.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "attendance_records",
    indexes = {
        @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_attendance_tenant",     columnList = "tenant_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_attendance_enrollment_session",
            columnNames = {"enrollment_id", "session_id"}
        )
    }
)
public class AttendanceRecordJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_attendance_enrollment"))
    private EnrollmentJpaEntity enrollment;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "attended", nullable = false)
    private Boolean attended = false;

    @Column(name = "recorded_by", length = 36)
    private String recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    @PrePersist
    protected void onPrePersist() {
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }

    public AttendanceRecordJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public EnrollmentJpaEntity getEnrollment() { return enrollment; }
    public void setEnrollment(EnrollmentJpaEntity enrollment) { this.enrollment = enrollment; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Boolean getAttended() { return attended; }
    public void setAttended(Boolean attended) { this.attended = attended; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
