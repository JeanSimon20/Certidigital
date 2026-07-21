package com.certidigital.platform.credential.application.dto;

import jakarta.validation.constraints.NotBlank;

public class CredentialIssuanceRequest {

    @NotBlank(message = "El enrollmentId es obligatorio")
    private String enrollmentId;

    @NotBlank(message = "El tenantId es obligatorio")
    private String tenantId;

    private String requestedByUserId;
    private String policyId;
    private String attributesJson;

    public CredentialIssuanceRequest() {}

    public CredentialIssuanceRequest(String enrollmentId, String tenantId, String requestedByUserId, String policyId, String attributesJson) {
        this.enrollmentId = enrollmentId;
        this.tenantId = tenantId;
        this.requestedByUserId = requestedByUserId;
        this.policyId = policyId;
        this.attributesJson = attributesJson;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(String requestedByUserId) { this.requestedByUserId = requestedByUserId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
}
