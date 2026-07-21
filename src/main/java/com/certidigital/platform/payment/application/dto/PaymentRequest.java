package com.certidigital.platform.payment.application.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private String tenantId;
    private String enrollmentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private boolean simulateFailure;
    private String failureReason;

    public PaymentRequest() {}

    public PaymentRequest(String tenantId, String enrollmentId, BigDecimal amount, String currency, String paymentMethod, boolean simulateFailure, String failureReason) {
        this.tenantId = tenantId;
        this.enrollmentId = enrollmentId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.simulateFailure = simulateFailure;
        this.failureReason = failureReason;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isSimulateFailure() { return simulateFailure; }
    public void setSimulateFailure(boolean simulateFailure) { this.simulateFailure = simulateFailure; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
