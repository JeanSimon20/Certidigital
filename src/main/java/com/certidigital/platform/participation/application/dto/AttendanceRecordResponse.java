package com.certidigital.platform.participation.application.dto;

import java.time.LocalDateTime;

public class AttendanceRecordResponse {

    private String id;
    private String tenantId;
    private String enrollmentId;
    private String participantId;
    private String participantName;
    private String sessionId;
    private Boolean attended;
    private String method;
    private String recordedBy;
    private LocalDateTime recordedAt;
    private String notes;

    public AttendanceRecordResponse() {}

    public AttendanceRecordResponse(
        String id, String tenantId, String enrollmentId, String participantId,
        String participantName, String sessionId, Boolean attended, String method,
        String recordedBy, LocalDateTime recordedAt, String notes
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.enrollmentId = enrollmentId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.sessionId = sessionId;
        this.attended = attended;
        this.method = method;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Boolean getAttended() { return attended; }
    public void setAttended(Boolean attended) { this.attended = attended; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
