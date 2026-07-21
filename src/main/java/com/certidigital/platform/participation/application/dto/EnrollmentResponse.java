package com.certidigital.platform.participation.application.dto;

import java.time.LocalDateTime;

public class EnrollmentResponse {

    private String id;
    private String eventId;
    private String eventName;
    private String eventType;
    private String eventMode;
    private String tenantId;
    private String participantId;
    private String participantName;
    private String participantEmail;
    private String status;
    private String paymentStatus;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse() {}

    public EnrollmentResponse(
        String id, String eventId, String eventName, String eventType, String eventMode,
        String tenantId, String participantId, String participantName, String participantEmail,
        String status, String paymentStatus, LocalDateTime enrolledAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventMode = eventMode;
        this.tenantId = tenantId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.enrolledAt = enrolledAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventMode() { return eventMode; }
    public void setEventMode(String eventMode) { this.eventMode = eventMode; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String participantEmail) { this.participantEmail = participantEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}
