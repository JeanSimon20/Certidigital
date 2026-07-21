package com.certidigital.platform.participation.application.dto;

public class CreateEnrollmentRequest {

    private String eventId;

    public CreateEnrollmentRequest() {}

    public CreateEnrollmentRequest(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
