package com.certidigital.platform.payment.application.dto;

import java.math.BigDecimal;

public class SubmitVoucherRequest {

    private String enrollmentId;
    private String paymentMethod; // YAPE, PLIN, BANK_TRANSFER
    private String operationNumber;
    private String voucherUrl; // Base64 Data URL or File URL
    private BigDecimal amount;

    public SubmitVoucherRequest() {}

    public SubmitVoucherRequest(String enrollmentId, String paymentMethod, String operationNumber, String voucherUrl, BigDecimal amount) {
        this.enrollmentId = enrollmentId;
        this.paymentMethod = paymentMethod;
        this.operationNumber = operationNumber;
        this.voucherUrl = voucherUrl;
        this.amount = amount;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOperationNumber() { return operationNumber; }
    public void setOperationNumber(String operationNumber) { this.operationNumber = operationNumber; }

    public String getVoucherUrl() { return voucherUrl; }
    public void setVoucherUrl(String voucherUrl) { this.voucherUrl = voucherUrl; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
