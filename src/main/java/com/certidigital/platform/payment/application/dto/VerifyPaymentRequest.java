package com.certidigital.platform.payment.application.dto;

public class VerifyPaymentRequest {

    private String action; // APPROVE or REJECT
    private String notes;

    public VerifyPaymentRequest() {}

    public VerifyPaymentRequest(String action, String notes) {
        this.action = action;
        this.notes = notes;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
