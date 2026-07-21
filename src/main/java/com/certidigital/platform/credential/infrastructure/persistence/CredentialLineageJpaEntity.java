package com.certidigital.platform.credential.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "credential_lineages",
    indexes = {
        @Index(name = "idx_lineage_original", columnList = "original_credential_id"),
        @Index(name = "idx_lineage_tenant",   columnList = "tenant_id")
    }
)
public class CredentialLineageJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "original_credential_id", length = 36, nullable = false)
    private String originalCredentialId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public CredentialLineageJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getOriginalCredentialId() { return originalCredentialId; }
    public void setOriginalCredentialId(String originalCredentialId) { this.originalCredentialId = originalCredentialId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
