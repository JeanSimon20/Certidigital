package com.certidigital.platform.credential.application.dto;

import jakarta.validation.constraints.NotBlank;

public class RevokeCredentialRequest {

    @NotBlank(message = "El credentialId es obligatorio")
    private String credentialId;

    @NotBlank(message = "El tenantId es obligatorio")
    private String tenantId;

    @NotBlank(message = "El motivo de revocación es obligatorio")
    private String reason;

    private String notes;
    private String revokedByUserId;

    public RevokeCredentialRequest() {}

    public RevokeCredentialRequest(String credentialId, String tenantId, String reason, String notes, String revokedByUserId) {
        this.credentialId = credentialId;
        this.tenantId = tenantId;
        this.reason = reason;
        this.notes = notes;
        this.revokedByUserId = revokedByUserId;
    }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRevokedByUserId() { return revokedByUserId; }
    public void setRevokedByUserId(String revokedByUserId) { this.revokedByUserId = revokedByUserId; }
}
