package com.certidigital.platform.credential.infrastructure.web;

import com.certidigital.platform.credential.application.dto.CredentialVerificationResult;
import com.certidigital.platform.credential.application.service.CredentialIssuanceApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
public class PublicVerificationController {

    private final CredentialIssuanceApplicationService issuanceService;

    public PublicVerificationController(CredentialIssuanceApplicationService issuanceService) {
        this.issuanceService = issuanceService;
    }

    @GetMapping("/{codeOrHash}")
    public ResponseEntity<ApiResponse<CredentialVerificationResult>> verifyCredential(@PathVariable String codeOrHash) {
        CredentialVerificationResult result = issuanceService.verifyCredential(codeOrHash);
        return ResponseEntity.ok(ApiResponse.success(result, "Resultado de verificación de la credencial."));
    }
}
