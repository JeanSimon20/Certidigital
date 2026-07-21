package com.certidigital.platform.participation.application.dto;

public class RecordAttendanceRequest {

    private String enrollmentId;
    private String sessionId;
    private Boolean attended = true;
    private String method = "MANUAL"; // MANUAL, QR_CODE, IMPORT
    private String notes;

    public RecordAttendanceRequest() {}

    public RecordAttendanceRequest(String enrollmentId, String sessionId, Boolean attended, String method, String notes) {
        this.enrollmentId = enrollmentId;
        this.sessionId = sessionId;
        this.attended = attended;
        this.method = method;
        this.notes = notes;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Boolean getAttended() { return attended; }
    public void setAttended(Boolean attended) { this.attended = attended; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
