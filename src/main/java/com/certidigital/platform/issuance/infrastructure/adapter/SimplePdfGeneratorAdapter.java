package com.certidigital.platform.issuance.infrastructure.adapter;

import com.certidigital.platform.issuance.domain.port.PdfGeneratorPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SimplePdfGeneratorAdapter implements PdfGeneratorPort {

    @Override
    public byte[] generateCertificatePdf(CredentialDocumentData data) {
        String pdfHeader = "%PDF-1.4\n";
        String content = "Certificado Digital Oficial CertiDigital\n"
                + "Código: " + data.publicCode() + "\n"
                + "Participante: " + data.participantName() + "\n"
                + "Institución: " + data.issuerName() + "\n"
                + "Evento: " + data.eventName() + " (" + data.eventType() + ")\n"
                + "Fecha Emisión: " + data.issuedDateStr() + "\n"
                + "Verificación: " + data.verificationUrl() + "\n"
                + "SHA-256: " + data.sha256Hash() + "\n";
        return (pdfHeader + content).getBytes(StandardCharsets.UTF_8);
    }
}
