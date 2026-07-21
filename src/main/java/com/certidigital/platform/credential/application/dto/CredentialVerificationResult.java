package com.certidigital.platform.credential.application.dto;

import java.time.LocalDateTime;

public class CredentialVerificationResult {

    private String status; // VALID, REVOKED, EXPIRED, NOT_FOUND
    private boolean isValid;
    private String publicCode;
    private String participantName;
    private String participantDocMasked;
    private String issuerName;
    private String eventName;
    private String eventType;
    private LocalDateTime issuedAt;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private String contentHash;
    private String blockchainTxId;
    private String blockchainNetwork;
    private String verificationUrl;
    private String qrCodeUrl;
    private String message;

    public CredentialVerificationResult() {}

    public static CredentialVerificationResult notFound(String code) {
        CredentialVerificationResult res = new CredentialVerificationResult();
        res.setStatus("NOT_FOUND");
        res.setValid(false);
        res.setPublicCode(code);
        res.setMessage("La credencial solicitada no fue encontrada en los registros oficiales.");
        return res;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }

    public String getPublicCode() { return publicCode; }
    public void setPublicCode(String publicCode) { this.publicCode = publicCode; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getParticipantDocMasked() { return participantDocMasked; }
    public void setParticipantDocMasked(String participantDocMasked) { this.participantDocMasked = participantDocMasked; }

    public String getIssuerName() { return issuerName; }
    public void setIssuerName(String issuerName) { this.issuerName = issuerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getBlockchainTxId() { return blockchainTxId; }
    public void setBlockchainTxId(String blockchainTxId) { this.blockchainTxId = blockchainTxId; }

    public String getBlockchainNetwork() { return blockchainNetwork; }
    public void setBlockchainNetwork(String blockchainNetwork) { this.blockchainNetwork = blockchainNetwork; }

    public String getVerificationUrl() { return verificationUrl; }
    public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
