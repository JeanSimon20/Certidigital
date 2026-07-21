package com.certidigital.platform.payment.application.dto;

import java.time.LocalDateTime;

public class PaymentResult {

    private boolean success;
    private String status; // CONFIRMED, REJECTED, REFUNDED, PENDING
    private String externalReference;
    private String receiptUrl;
    private String errorMessage;
    private LocalDateTime timestamp;

    public PaymentResult() {
        this.timestamp = LocalDateTime.now();
    }

    public PaymentResult(boolean success, String status, String externalReference, String receiptUrl, String errorMessage) {
        this.success = success;
        this.status = status;
        this.externalReference = externalReference;
        this.receiptUrl = receiptUrl;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }

    public static PaymentResult success(String externalReference, String receiptUrl) {
        return new PaymentResult(true, "CONFIRMED", externalReference, receiptUrl, null);
    }

    public static PaymentResult failure(String errorMessage) {
        return new PaymentResult(false, "REJECTED", null, null, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
