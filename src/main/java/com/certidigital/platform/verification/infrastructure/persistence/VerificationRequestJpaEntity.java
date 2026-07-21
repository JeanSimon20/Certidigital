package com.certidigital.platform.verification.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "verification_requests",
    indexes = {
        @Index(name = "idx_verification_credential", columnList = "credential_id"),
        @Index(name = "idx_verification_method",     columnList = "verification_method"),
        @Index(name = "idx_verification_at",         columnList = "verified_at")
    }
)
public class VerificationRequestJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "credential_id", length = 36, nullable = false)
    private String credentialId;

    @Column(name = "verification_method", length = 30, nullable = false)
    private String verificationMethod = "QR";

    @Column(name = "result", length = 30, nullable = false)
    private String result;

    @Column(name = "requestor_ip", length = 50)
    private String requestorIp;

    @Column(name = "requestor_user_agent", length = 500)
    private String requestorUserAgent;

    @Column(name = "verified_at", nullable = false, updatable = false)
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.verifiedAt == null) this.verifiedAt = LocalDateTime.now();
    }

    public VerificationRequestJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getRequestorIp() { return requestorIp; }
    public void setRequestorIp(String requestorIp) { this.requestorIp = requestorIp; }

    public String getRequestorUserAgent() { return requestorUserAgent; }
    public void setRequestorUserAgent(String requestorUserAgent) { this.requestorUserAgent = requestorUserAgent; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
