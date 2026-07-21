package com.certidigital.platform.issuance.infrastructure.adapter;

import com.certidigital.platform.issuance.domain.port.QrGeneratorPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SimpleQrGeneratorAdapter implements QrGeneratorPort {

    @Override
    public String generateQrDataUrl(String verificationUrl) {
        String base64Payload = Base64.getEncoder().encodeToString(verificationUrl.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64Payload;
    }
}
