package com.certidigital.platform.credential.application.service;

import com.certidigital.platform.credential.application.dto.CredentialIssuanceRequest;
import com.certidigital.platform.credential.application.dto.CredentialVerificationResult;
import com.certidigital.platform.credential.application.dto.RevokeCredentialRequest;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaEntity;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaRepository;
import com.certidigital.platform.credential.infrastructure.persistence.RevocationRecordJpaEntity;
import com.certidigital.platform.credential.infrastructure.persistence.RevocationRecordJpaRepository;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.issuance.domain.port.BlockchainPort;
import com.certidigital.platform.issuance.domain.port.PdfGeneratorPort;
import com.certidigital.platform.issuance.domain.port.QrGeneratorPort;
import com.certidigital.platform.organization.infrastructure.persistence.OrganizationProfileJpaEntity;
import com.certidigital.platform.organization.infrastructure.persistence.OrganizationProfileJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.policy.application.dto.EvaluateEligibilityRequest;
import com.certidigital.platform.policy.application.service.EligibilityApplicationService;
import com.certidigital.platform.policy.domain.model.EligibilityResult;
import com.certidigital.platform.policy.infrastructure.persistence.EligibilityEvaluationJpaEntity;
import com.certidigital.platform.policy.infrastructure.persistence.IssuanceRequestJpaEntity;
import com.certidigital.platform.policy.infrastructure.persistence.IssuanceRequestJpaRepository;
import com.certidigital.platform.shared.infrastructure.security.TenantAccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CredentialIssuanceApplicationService {

    private final CredentialJpaRepository credentialRepository;
    private final RevocationRecordJpaRepository revocationRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final OrganizationProfileJpaRepository organizationRepository;
    private final IssuanceRequestJpaRepository issuanceRequestRepository;
    private final EligibilityApplicationService eligibilityService;
    private final BlockchainPort blockchainPort;
    private final PdfGeneratorPort pdfGeneratorPort;
    private final QrGeneratorPort qrGeneratorPort;

    public CredentialIssuanceApplicationService(
            CredentialJpaRepository credentialRepository,
            RevocationRecordJpaRepository revocationRepository,
            EnrollmentJpaRepository enrollmentRepository,
            EventJpaRepository eventRepository,
            OrganizationProfileJpaRepository organizationRepository,
            IssuanceRequestJpaRepository issuanceRequestRepository,
            EligibilityApplicationService eligibilityService,
            BlockchainPort blockchainPort,
            PdfGeneratorPort pdfGeneratorPort,
            QrGeneratorPort qrGeneratorPort
    ) {
        this.credentialRepository = credentialRepository;
        this.revocationRepository = revocationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.organizationRepository = organizationRepository;
        this.issuanceRequestRepository = issuanceRequestRepository;
        this.eligibilityService = eligibilityService;
        this.blockchainPort = blockchainPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
        this.qrGeneratorPort = qrGeneratorPort;
    }

    @Transactional
    public CredentialJpaEntity issueCredential(CredentialIssuanceRequest request) {
        // 1. Cargar la inscripción
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + request.getEnrollmentId()));

        // 2. Validación de Aislamiento Multi-Tenant
        if (!enrollment.getTenantId().equals(request.getTenantId())) {
            throw new TenantAccessDeniedException("La inscripción no pertenece al Tenant: " + request.getTenantId());
        }

        // 3. Verificación previa en el Motor de Elegibilidad
        Double score = enrollment.getOverallScore() != null ? enrollment.getOverallScore().doubleValue() : 16.0;
        EvaluateEligibilityRequest evalReq = new EvaluateEligibilityRequest(enrollment.getId(), request.getPolicyId(), score);
        EligibilityResult eligibilityResult = eligibilityService.evaluateEligibility(evalReq);
        if (!"ELIGIBLE".equalsIgnoreCase(eligibilityResult.getStatus())) {
            throw new IllegalStateException("El participante no es ELIGIBLE para recibir una credencial: " + eligibilityResult.getSummaryReason());
        }

        // 4. Prevención de Doble Emisión Activa
        String participantId = enrollment.getParticipant().getId();
        Optional<CredentialJpaEntity> existingActive = credentialRepository
                .findFirstByParticipantIdAndEventIdAndTenantIdAndStatus(
                        participantId,
                        enrollment.getEventId(),
                        enrollment.getTenantId(),
                        "ACTIVE"
                );
        if (existingActive.isPresent()) {
            throw new IllegalStateException("Ya existe una credencial activa emitida para esta inscripción/evento (Código: " + existingActive.get().getPublicCode() + ")");
        }

        // 5. Cargar datos del evento y organización emisor
        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + enrollment.getEventId()));

        OrganizationProfileJpaEntity org = organizationRepository.findByTenantId(enrollment.getTenantId()).orElse(null);
        String issuerName = org != null ? org.getLegalName() : "CertiDigital Issuer Authority";
        String issuerCountry = org != null && org.getCountryCode() != null ? org.getCountryCode() : "PER";

        // 6. Generar Identificador Único No Predecible (Código Público)
        String credentialId = UUID.randomUUID().toString();
        String publicCode = generateUnpredictablePublicCode();

        // 7. Construir y calcular el Hash SHA-256 del contenido canónico
        LocalDateTime now = LocalDateTime.now();
        String canonicalContent = String.format(
                "ID:%s|TENANT:%s|CODE:%s|PARTICIPANT:%s|EVENT:%s|ISSUED:%s",
                credentialId, enrollment.getTenantId(), publicCode, enrollment.getParticipant().getFullName(), event.getName(), now
        );
        String contentHash = computeSha256(canonicalContent);

        // 8. Construir URL pública de verificación y QR Code
        String verificationUrl = "http://localhost:8080/api/verification/" + publicCode;
        String qrCodeUrl = qrGeneratorPort.generateQrDataUrl(verificationUrl);

        // 8. Crear y persistir la Solicitud de Emisión (IssuanceRequestJpaEntity)
        List<EligibilityEvaluationJpaEntity> evals = eligibilityService.getEvaluationsForEnrollment(enrollment.getId());
        String evalId = !evals.isEmpty() ? evals.get(evals.size() - 1).getId() : UUID.randomUUID().toString();

        IssuanceRequestJpaEntity issuanceReqEntity = new IssuanceRequestJpaEntity();
        issuanceReqEntity.setId(UUID.randomUUID().toString());
        issuanceReqEntity.setTenantId(enrollment.getTenantId());
        issuanceReqEntity.setEvaluationId(evalId);
        issuanceReqEntity.setEnrollmentId(enrollment.getId());
        issuanceReqEntity.setParticipantId(participantId);
        issuanceReqEntity.setEventId(event.getId());
        issuanceReqEntity.setPolicyId(eligibilityResult.getPolicyId() != null ? eligibilityResult.getPolicyId() : "policy-default");
        issuanceReqEntity.setStatus("APPROVED");
        issuanceReqEntity.setReviewerUserId(request.getRequestedByUserId() != null ? request.getRequestedByUserId() : "SYSTEM");
        issuanceReqEntity.setReviewedAt(now);
        issuanceReqEntity.setReviewDecision("APPROVED");
        issuanceReqEntity.setReviewNotes("Solicitud generada y aprobada automáticamente por elegibilidad");
        issuanceReqEntity = issuanceRequestRepository.save(issuanceReqEntity);

        // 9. Construir la Entidad CredentialJpaEntity (Inmutable)
        CredentialJpaEntity credential = new CredentialJpaEntity();
        credential.setId(credentialId);
        credential.setTenantId(enrollment.getTenantId());
        credential.setCredentialType(event.getEventType() != null ? event.getEventType() : "CERTIFICATE");
        credential.setPublicCode(publicCode);

        // Sujeto
        credential.setParticipantId(participantId);
        credential.setParticipantName(enrollment.getParticipant().getFullName());
        credential.setParticipantEmail(enrollment.getParticipant().getEmail());
        credential.setParticipantDoc(enrollment.getParticipant().getDocNumber());

        // Emisor
        credential.setIssuerTenantId(enrollment.getTenantId());
        credential.setIssuerName(issuerName);
        credential.setIssuerCountry(issuerCountry);

        // Contexto
        credential.setEventId(event.getId());
        credential.setEventName(event.getName());
        credential.setPolicyId(eligibilityResult.getPolicyId() != null ? eligibilityResult.getPolicyId() : "policy-default");
        credential.setIssuanceRequestId(issuanceReqEntity.getId());
        credential.setAttributes(request.getAttributesJson() != null ? request.getAttributesJson() : "{\"source\":\"automated_issuance\"}");

        // Fechas e Integridad
        credential.setIssuedAt(now);
        credential.setStatus("ACTIVE");
        credential.setContentHash(contentHash);
        credential.setVerificationUrl(verificationUrl);
        credential.setQrCodeUrl(qrCodeUrl);

        // Guardar primero la credencial en BD para respetar la FK de blockchain_records
        credential = credentialRepository.save(credential);

        // 9. Anclaje en la Blockchain (BlockchainSimulatorAdapter)
        BlockchainPort.BlockchainAnchorResult anchorResult = blockchainPort.anchorCredentialHash(
                enrollment.getTenantId(), credentialId, contentHash
        );

        credential.setBlockchainNetwork(anchorResult.network());
        credential.setBlockchainTxId(anchorResult.txId());
        credential.setBlockchainRegisteredAt(anchorResult.registeredAt());

        // Actualizar estado de inscripción a ISSUED
        enrollment.setStatus("ISSUED");
        enrollmentRepository.save(enrollment);

        return credentialRepository.save(credential);
    }

    @Transactional
    public CredentialJpaEntity revokeCredential(RevokeCredentialRequest request) {
        CredentialJpaEntity credential = credentialRepository.findByIdAndTenantId(request.getCredentialId(), request.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Credencial no encontrada o no pertenece al Tenant"));

        if ("REVOKED".equalsIgnoreCase(credential.getStatus())) {
            throw new IllegalStateException("La credencial ya se encuentra en estado REVOKED");
        }

        // Inmutabilidad: Solo se actualiza el estado
        credential.setStatus("REVOKED");

        RevocationRecordJpaEntity revocationRecord = new RevocationRecordJpaEntity();
        revocationRecord.setId(UUID.randomUUID().toString());
        revocationRecord.setTenantId(request.getTenantId());
        revocationRecord.setCredential(credential);
        revocationRecord.setRevocationReason(request.getReason());
        revocationRecord.setRevocationNotes(request.getNotes() != null ? request.getNotes() : "Revocado administrativamente");
        revocationRecord.setRevokedBy(request.getRevokedByUserId() != null ? request.getRevokedByUserId() : "SYSTEM_ADMIN");
        revocationRecord.setRevokedAt(LocalDateTime.now());

        revocationRepository.save(revocationRecord);
        return credentialRepository.save(credential);
    }

    @Transactional
    public CredentialJpaEntity reissueCredential(String previousCredentialId, String tenantId, String reason, String requestedByUserId) {
        // 1. Revocar la credencial previa
        RevokeCredentialRequest revokeReq = new RevokeCredentialRequest(
                previousCredentialId, tenantId, "REISSUE: " + reason, "Reacreditación por corrección de credencial previa", requestedByUserId
        );
        CredentialJpaEntity previousCredential = revokeCredential(revokeReq);

        // 2. Buscar la inscripción original
        List<EnrollmentJpaEntity> enrollments = enrollmentRepository.findAllByParticipantIdAndTenantId(previousCredential.getParticipantId(), tenantId);
        EnrollmentJpaEntity matchingEnrollment = enrollments.stream()
                .filter(e -> e.getEventId().equals(previousCredential.getEventId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró la inscripción vinculada al evento para reacreditar"));

        // 3. Emitir nueva credencial vinculando linaje
        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(
                matchingEnrollment.getId(), tenantId, requestedByUserId, previousCredential.getPolicyId(), previousCredential.getAttributes()
        );

        CredentialJpaEntity newCredential = issueCredential(issueReq);
        newCredential.setPredecessorCredentialId(previousCredential.getId());
        return credentialRepository.save(newCredential);
    }

    @Transactional(readOnly = true)
    public CredentialVerificationResult verifyCredential(String publicCodeOrHash) {
        Optional<CredentialJpaEntity> optionalCredential = credentialRepository.findByPublicCode(publicCodeOrHash);
        if (optionalCredential.isEmpty()) {
            optionalCredential = credentialRepository.findByContentHash(publicCodeOrHash);
        }

        if (optionalCredential.isEmpty()) {
            return CredentialVerificationResult.notFound(publicCodeOrHash);
        }

        CredentialJpaEntity credential = optionalCredential.get();
        CredentialVerificationResult result = new CredentialVerificationResult();
        result.setPublicCode(credential.getPublicCode());
        result.setParticipantName(credential.getParticipantName());
        result.setParticipantDocMasked(maskDoc(credential.getParticipantDoc()));
        result.setIssuerName(credential.getIssuerName());
        result.setEventName(credential.getEventName());
        result.setEventType(credential.getCredentialType());
        result.setIssuedAt(credential.getIssuedAt());
        result.setContentHash(credential.getContentHash());
        result.setBlockchainTxId(credential.getBlockchainTxId());
        result.setBlockchainNetwork(credential.getBlockchainNetwork());
        result.setVerificationUrl(credential.getVerificationUrl());
        result.setQrCodeUrl(credential.getQrCodeUrl());

        if ("REVOKED".equalsIgnoreCase(credential.getStatus())) {
            result.setStatus("REVOKED");
            result.setValid(false);
            
            Optional<RevocationRecordJpaEntity> revocationOpt = revocationRepository.findByCredentialId(credential.getId());
            if (revocationOpt.isPresent()) {
                RevocationRecordJpaEntity rev = revocationOpt.get();
                result.setRevocationReason(rev.getRevocationReason());
                result.setRevokedAt(rev.getRevokedAt());
                result.setMessage("Esta credencial fue REVOCADA el " + rev.getRevokedAt() + " y ya no es válida.");
            } else if (credential.getRevocationRecord() != null) {
                result.setRevocationReason(credential.getRevocationRecord().getRevocationReason());
                result.setRevokedAt(credential.getRevocationRecord().getRevokedAt());
                result.setMessage("Esta credencial fue REVOCADA y ya no es válida.");
            } else {
                result.setMessage("Esta credencial fue REVOCADA y ya no es válida.");
            }
        } else if ("ACTIVE".equalsIgnoreCase(credential.getStatus())) {
            result.setStatus("VALID");
            result.setValid(true);
            result.setMessage("Credencial auténtica, activa y verificada formalmente.");
        } else {
            result.setStatus(credential.getStatus());
            result.setValid(false);
            result.setMessage("Estado de la credencial: " + credential.getStatus());
        }

        return result;
    }

    private String generateUnpredictablePublicCode() {
        String year = String.valueOf(Year.now().getValue());
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "CDIG-" + year + "-" + randomSuffix;
    }

    public static String computeSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se encontró el algoritmo SHA-256", e);
        }
    }

    private String maskDoc(String doc) {
        if (doc == null || doc.length() < 4) return "****";
        return doc.substring(0, 2) + "****" + doc.substring(doc.length() - 2);
    }
}
