package com.certidigital.platform.issuance.domain.port;

public interface PdfGeneratorPort {

    record CredentialDocumentData(
            String publicCode,
            String participantName,
            String participantDoc,
            String issuerName,
            String eventName,
            String eventType,
            String issuedDateStr,
            String verificationUrl,
            String sha256Hash
    ) {}

    byte[] generateCertificatePdf(CredentialDocumentData data);
}
