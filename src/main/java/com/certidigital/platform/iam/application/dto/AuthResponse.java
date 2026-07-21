package com.certidigital.platform.iam.application.dto;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userId;
    private String email;
    private String fullName;
    private String activeTenantId;
    private List<String> roles;
    private List<String> permissions;
    private List<TenantSummaryResponse> availableTenants;

    public AuthResponse() {}

    public AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String email,
        String fullName,
        String activeTenantId,
        List<String> roles,
        List<String> permissions,
        List<TenantSummaryResponse> availableTenants
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.activeTenantId = activeTenantId;
        this.roles = roles;
        this.permissions = permissions;
        this.availableTenants = availableTenants;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getActiveTenantId() { return activeTenantId; }
    public void setActiveTenantId(String activeTenantId) { this.activeTenantId = activeTenantId; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public List<TenantSummaryResponse> getAvailableTenants() { return availableTenants; }
    public void setAvailableTenants(List<TenantSummaryResponse> availableTenants) { this.availableTenants = availableTenants; }
}
