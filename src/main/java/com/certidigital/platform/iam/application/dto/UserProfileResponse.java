package com.certidigital.platform.iam.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileResponse {

    private String id;
    private String email;
    private String fullName;
    private String status;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private String activeTenantId;
    private List<String> activeRoles;
    private List<String> activePermissions;

    public UserProfileResponse() {}

    public UserProfileResponse(
        String id,
        String email,
        String fullName,
        String status,
        Boolean emailVerified,
        LocalDateTime lastLoginAt,
        String activeTenantId,
        List<String> activeRoles,
        List<String> activePermissions
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.emailVerified = emailVerified;
        this.lastLoginAt = lastLoginAt;
        this.activeTenantId = activeTenantId;
        this.activeRoles = activeRoles;
        this.activePermissions = activePermissions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getActiveTenantId() { return activeTenantId; }
    public void setActiveTenantId(String activeTenantId) { this.activeTenantId = activeTenantId; }

    public List<String> getActiveRoles() { return activeRoles; }
    public void setActiveRoles(List<String> activeRoles) { this.activeRoles = activeRoles; }

    public List<String> getActivePermissions() { return activePermissions; }
    public void setActivePermissions(List<String> activePermissions) { this.activePermissions = activePermissions; }
}
