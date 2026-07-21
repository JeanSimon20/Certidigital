package com.certidigital.platform.credential.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "revocation_records",
    indexes = {
        @Index(name = "idx_revocation_credential", columnList = "credential_id"),
        @Index(name = "idx_revocation_tenant",     columnList = "tenant_id")
    }
)
public class RevocationRecordJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_revocation_credential"))
    private CredentialJpaEntity credential;

    @Column(name = "revocation_reason", length = 100, nullable = false)
    private String revocationReason;

    @Column(name = "revocation_notes", columnDefinition = "TEXT")
    private String revocationNotes;

    @Column(name = "revoked_by", length = 36, nullable = false)
    private String revokedBy;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.revokedAt == null) this.revokedAt = LocalDateTime.now();
    }

    public RevocationRecordJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public CredentialJpaEntity getCredential() { return credential; }
    public void setCredential(CredentialJpaEntity credential) { this.credential = credential; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }

    public String getRevocationNotes() { return revocationNotes; }
    public void setRevocationNotes(String revocationNotes) { this.revocationNotes = revocationNotes; }

    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}
