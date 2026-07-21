package com.certidigital.platform.payment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentVerificationResponse {

    private String paymentId;
    private String enrollmentId;
    private String participantName;
    private String participantEmail;
    private String eventName;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String operationNumber;
    private String voucherUrl;
    private String paymentStatus; // WAITING_VERIFICATION, CONFIRMED, REJECTED
    private LocalDateTime paymentDate;
    private String notes;

    public PaymentVerificationResponse() {}

    public PaymentVerificationResponse(
        String paymentId,
        String enrollmentId,
        String participantName,
        String participantEmail,
        String eventName,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String operationNumber,
        String voucherUrl,
        String paymentStatus,
        LocalDateTime paymentDate,
        String notes
    ) {
        this.paymentId = paymentId;
        this.enrollmentId = enrollmentId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.eventName = eventName;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.operationNumber = operationNumber;
        this.voucherUrl = voucherUrl;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.notes = notes;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String participantEmail) { this.participantEmail = participantEmail; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOperationNumber() { return operationNumber; }
    public void setOperationNumber(String operationNumber) { this.operationNumber = operationNumber; }

    public String getVoucherUrl() { return voucherUrl; }
    public void setVoucherUrl(String voucherUrl) { this.voucherUrl = voucherUrl; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
