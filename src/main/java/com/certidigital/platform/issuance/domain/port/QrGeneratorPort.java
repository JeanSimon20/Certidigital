package com.certidigital.platform.issuance.domain.port;

public interface QrGeneratorPort {
    String generateQrDataUrl(String verificationUrl);
}
