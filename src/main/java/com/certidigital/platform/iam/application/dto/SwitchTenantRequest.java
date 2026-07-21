package com.certidigital.platform.iam.application.dto;

import jakarta.validation.constraints.NotBlank;

public class SwitchTenantRequest {

    @NotBlank(message = "El tenantId es obligatorio")
    private String tenantId;

    public SwitchTenantRequest() {}

    public SwitchTenantRequest(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
