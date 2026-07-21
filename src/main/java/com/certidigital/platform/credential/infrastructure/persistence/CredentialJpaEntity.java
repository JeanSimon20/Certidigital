package com.certidigital.platform.credential.infrastructure.persistence;

import com.certidigital.platform.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "credentials",
    indexes = {
        @Index(name = "idx_credentials_tenant_id",    columnList = "tenant_id"),
        @Index(name = "idx_credentials_participant",  columnList = "participant_id"),
        @Index(name = "idx_credentials_event_id",     columnList = "event_id"),
        @Index(name = "idx_credentials_status",       columnList = "tenant_id, status"),
        @Index(name = "idx_credentials_content_hash", columnList = "content_hash"),
        @Index(name = "idx_credentials_public_code",  columnList = "public_code"),
        @Index(name = "idx_credentials_issued_at",    columnList = "issued_at")
    }
)
public class CredentialJpaEntity extends AuditableEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "credential_type", length = 50, nullable = false)
    private String credentialType = "CERTIFICATE";

    @Column(name = "public_code", length = 100)
    private String publicCode;

    @Column(name = "participant_id", length = 36, nullable = false)
    private String participantId;

    @Column(name = "participant_name", length = 500, nullable = false)
    private String participantName;

    @Column(name = "participant_email", length = 255, nullable = false)
    private String participantEmail;

    @Column(name = "participant_doc", length = 200)
    private String participantDoc;

    @Column(name = "issuer_tenant_id", length = 36, nullable = false)
    private String issuerTenantId;

    @Column(name = "issuer_name", length = 500, nullable = false)
    private String issuerName;

    @Column(name = "issuer_country", length = 3, nullable = false)
    private String issuerCountry;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "event_name", length = 500, nullable = false)
    private String eventName;

    @Column(name = "policy_id", length = 36, nullable = false)
    private String policyId;

    @Column(name = "issuance_request_id", length = 36, nullable = false)
    private String issuanceRequestId;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "signature_value", columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signing_algorithm", length = 100)
    private String signingAlgorithm;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "digital_cert_id", length = 36)
    private String digitalCertId;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "blockchain_network", length = 100)
    private String blockchainNetwork;

    @Column(name = "blockchain_tx_id", length = 255)
    private String blockchainTxId;

    @Column(name = "blockchain_registered_at")
    private LocalDateTime blockchainRegisteredAt;

    @Column(name = "verification_url", length = 1000)
    private String verificationUrl;

    @Column(name = "qr_code_url", length = 1000)
    private String qrCodeUrl;

    @Column(name = "document_url", length = 1000)
    private String documentUrl;

    @Column(name = "predecessor_credential_id", length = 36)
    private String predecessorCredentialId;

    @Column(name = "lineage_id", length = 36)
    private String lineageId;

    @OneToOne(mappedBy = "credential", fetch = FetchType.LAZY,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private RevocationRecordJpaEntity revocationRecord;

    public CredentialJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

    public String getPublicCode() { return publicCode; }
    public void setPublicCode(String publicCode) { this.publicCode = publicCode; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String participantEmail) { this.participantEmail = participantEmail; }

    public String getParticipantDoc() { return participantDoc; }
    public void setParticipantDoc(String participantDoc) { this.participantDoc = participantDoc; }

    public String getIssuerTenantId() { return issuerTenantId; }
    public void setIssuerTenantId(String issuerTenantId) { this.issuerTenantId = issuerTenantId; }

    public String getIssuerName() { return issuerName; }
    public void setIssuerName(String issuerName) { this.issuerName = issuerName; }

    public String getIssuerCountry() { return issuerCountry; }
    public void setIssuerCountry(String issuerCountry) { this.issuerCountry = issuerCountry; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getIssuanceRequestId() { return issuanceRequestId; }
    public void setIssuanceRequestId(String issuanceRequestId) { this.issuanceRequestId = issuanceRequestId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSignatureValue() { return signatureValue; }
    public void setSignatureValue(String signatureValue) { this.signatureValue = signatureValue; }

    public String getSigningAlgorithm() { return signingAlgorithm; }
    public void setSigningAlgorithm(String signingAlgorithm) { this.signingAlgorithm = signingAlgorithm; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getDigitalCertId() { return digitalCertId; }
    public void setDigitalCertId(String digitalCertId) { this.digitalCertId = digitalCertId; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getBlockchainNetwork() { return blockchainNetwork; }
    public void setBlockchainNetwork(String blockchainNetwork) { this.blockchainNetwork = blockchainNetwork; }

    public String getBlockchainTxId() { return blockchainTxId; }
    public void setBlockchainTxId(String blockchainTxId) { this.blockchainTxId = blockchainTxId; }

    public LocalDateTime getBlockchainRegisteredAt() { return blockchainRegisteredAt; }
    public void setBlockchainRegisteredAt(LocalDateTime blockchainRegisteredAt) { this.blockchainRegisteredAt = blockchainRegisteredAt; }

    public String getVerificationUrl() { return verificationUrl; }
    public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getPredecessorCredentialId() { return predecessorCredentialId; }
    public void setPredecessorCredentialId(String predecessorCredentialId) { this.predecessorCredentialId = predecessorCredentialId; }

    public String getLineageId() { return lineageId; }
    public void setLineageId(String lineageId) { this.lineageId = lineageId; }

    public RevocationRecordJpaEntity getRevocationRecord() { return revocationRecord; }
    public void setRevocationRecord(RevocationRecordJpaEntity revocationRecord) { this.revocationRecord = revocationRecord; }
}
