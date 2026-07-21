package com.certidigital.platform.credential.infrastructure.web;

import com.certidigital.platform.credential.application.dto.CredentialIssuanceRequest;
import com.certidigital.platform.credential.application.dto.RevokeCredentialRequest;
import com.certidigital.platform.credential.application.service.CredentialIssuanceApplicationService;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaEntity;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaRepository;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credentials")
public class CredentialIssuanceController {

    private final CredentialIssuanceApplicationService issuanceService;
    private final CredentialJpaRepository credentialRepository;

    public CredentialIssuanceController(
            CredentialIssuanceApplicationService issuanceService,
            CredentialJpaRepository credentialRepository
    ) {
        this.issuanceService = issuanceService;
        this.credentialRepository = credentialRepository;
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<CredentialJpaEntity>> issueCredential(
            @Valid @RequestBody CredentialIssuanceRequest request,
            Authentication authentication
    ) {
        String tenantId = request.getTenantId() != null && !request.getTenantId().isBlank()
                ? request.getTenantId() : TenantContextHolder.requireTenantId();

        request.setTenantId(tenantId);
        if (request.getRequestedByUserId() == null && authentication != null) {
            request.setRequestedByUserId(authentication.getName());
        }

        CredentialJpaEntity credential = issuanceService.issueCredential(request);
        return ResponseEntity.ok(ApiResponse.success(credential, "Credencial digital emitida y anclada exitosamente en la blockchain."));
    }

    @PostMapping("/revoke")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CredentialJpaEntity>> revokeCredential(
            @Valid @RequestBody RevokeCredentialRequest request,
            Authentication authentication
    ) {
        String tenantId = request.getTenantId() != null && !request.getTenantId().isBlank()
                ? request.getTenantId() : TenantContextHolder.requireTenantId();

        request.setTenantId(tenantId);
        if (request.getRevokedByUserId() == null && authentication != null) {
            request.setRevokedByUserId(authentication.getName());
        }

        CredentialJpaEntity credential = issuanceService.revokeCredential(request);
        return ResponseEntity.ok(ApiResponse.success(credential, "Credencial revocada exitosamente."));
    }

    @PostMapping("/reissue")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CredentialJpaEntity>> reissueCredential(
            @RequestParam String previousCredentialId,
            @RequestParam String reason,
            Authentication authentication
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        String userId = authentication != null ? authentication.getName() : "SYSTEM";

        CredentialJpaEntity newCredential = issuanceService.reissueCredential(previousCredentialId, tenantId, reason, userId);
        return ResponseEntity.ok(ApiResponse.success(newCredential, "Credencial reacreditada exitosamente vinculando el historial de antecedente."));
    }

    @GetMapping("/my-credentials")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CredentialJpaEntity>>> getMyCredentials(Authentication authentication) {
        String participantId = authentication != null ? authentication.getName() : "ANONYMOUS";
        String tenantId = TenantContextHolder.requireTenantId();

        List<CredentialJpaEntity> credentials = credentialRepository.findAllByParticipantIdAndTenantId(participantId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(credentials, "Credenciales del participante recuperadas."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'TENANT_ADMIN', 'SUPER_ADMIN', 'ORGANIZER', 'TEACHER')")
    public ResponseEntity<ApiResponse<CredentialJpaEntity>> getCredentialById(@PathVariable String id) {
        String tenantId = TenantContextHolder.requireTenantId();
        CredentialJpaEntity credential = credentialRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial no encontrada."));

        return ResponseEntity.ok(ApiResponse.success(credential, "Credencial recuperada."));
    }
}
