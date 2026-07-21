package com.certidigital.platform.credential;

import com.certidigital.platform.credential.application.dto.CredentialIssuanceRequest;
import com.certidigital.platform.credential.application.dto.CredentialVerificationResult;
import com.certidigital.platform.credential.application.dto.RevokeCredentialRequest;
import com.certidigital.platform.credential.application.service.CredentialIssuanceApplicationService;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaEntity;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaRepository;
import com.certidigital.platform.credential.infrastructure.persistence.RevocationRecordJpaEntity;
import com.certidigital.platform.credential.infrastructure.persistence.RevocationRecordJpaRepository;
import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
import com.certidigital.platform.issuance.infrastructure.persistence.BlockchainRecordJpaEntity;
import com.certidigital.platform.issuance.infrastructure.persistence.BlockchainRecordJpaRepository;
import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CredentialIssuanceTests {

    @Autowired
    private CredentialIssuanceApplicationService issuanceService;

    @Autowired
    private CredentialJpaRepository credentialRepository;

    @Autowired
    private RevocationRecordJpaRepository revocationRepository;

    @Autowired
    private BlockchainRecordJpaRepository blockchainRecordRepository;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private EnrollmentJpaRepository enrollmentRepository;

    @Autowired
    private EventApplicationService eventService;

    private String tenantId;
    private String userId;
    private String eventId;
    private String enrollmentId;

    @BeforeEach
    void setUp() {
        tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
        userId = "user-admin-0001-aaaa-bbbb-cccccccc";
        TenantContextHolder.setTenantId(tenantId);

        // 1. Crear Evento
        CreateEventRequest createEvent = new CreateEventRequest();
        createEvent.setName("Curso de Arquitectura Software Blockchain & DDD");
        createEvent.setDescription("Capacitación avanzada en diseño conducido por el dominio");
        createEvent.setEventType("COURSE");
        createEvent.setMode("ONLINE_SYNC");
        createEvent.setMaxCapacity(50);
        createEvent.setPrice(100.0);
        createEvent.setStartDate(LocalDateTime.now().minusDays(10));
        createEvent.setEndDate(LocalDateTime.now().minusDays(1));

        EventResponse createdEvent = eventService.createEvent(createEvent, tenantId, userId);
        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), tenantId, userId);
        this.eventId = publishedEvent.getId();

        // 2. Inscribir Participante
        CreateEnrollmentRequest enrollRequest = new CreateEnrollmentRequest(eventId);
        EnrollmentResponse enrResponse = enrollmentService.enrollParticipant(enrollRequest, userId);
        this.enrollmentId = enrResponse.getId();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testIssueCredentialWhenEligible() {
        // Simular pago completado y 90% asistencia
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        // Solicitud de Emisión
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(request);

        assertNotNull(credential.getId(), "La credencial debe tener ID");
        assertEquals("ACTIVE", credential.getStatus(), "El estado debe ser ACTIVE");
        assertTrue(credential.getPublicCode().startsWith("CDIG-"), "El código público debe empezar con CDIG-");
        assertNotNull(credential.getContentHash(), "El hash SHA-256 debe estar presente");
        assertEquals(64, credential.getContentHash().length(), "El hash SHA-256 debe tener 64 caracteres hex");
        assertNotNull(credential.getBlockchainTxId(), "Debe existir un Transaction ID de Blockchain");
        assertTrue(credential.getBlockchainTxId().startsWith("0x"), "El Tx ID debe comenzar con 0x");
        assertNotNull(credential.getVerificationUrl(), "La URL de verificación debe estar generada");
        assertNotNull(credential.getQrCodeUrl(), "El código QR en Base64 debe estar generado");

        // Verificar actualización de inscripción
        EnrollmentJpaEntity updatedEnrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertEquals("ISSUED", updatedEnrollment.getStatus(), "El estado de inscripción debe actualizarse a ISSUED");
    }

    @Test
    void testRejectIssuanceWhenNotEligible() {
        // Dejar inscripción sin pagar (PENDING_PAYMENT)
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            issuanceService.issueCredential(request);
        });

        assertTrue(ex.getMessage().contains("no es ELIGIBLE"), "Debe rechazar la emisión si el participante no es elegible");
    }

    @Test
    void testPreventDoubleIssuance() {
        // Preparar elegibilidad
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        // Primera Emisión (Exitosa)
        CredentialIssuanceRequest request1 = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        issuanceService.issueCredential(request1);

        // Segunda Emisión (Debe fallar)
        CredentialIssuanceRequest request2 = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            issuanceService.issueCredential(request2);
        });

        assertTrue(ex.getMessage().contains("Ya existe una credencial activa emitida"), "Debe prevenir doble emisión activa");
    }

    @Test
    void testRevokeCredential() {
        // Emitir credencial
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(issueReq);

        // Revocar credencial
        RevokeCredentialRequest revokeReq = new RevokeCredentialRequest(
                credential.getId(), tenantId, "Error tipográfico en el nombre del participante", "Revocación solicitada por administración", userId
        );
        CredentialJpaEntity revokedCredential = issuanceService.revokeCredential(revokeReq);

        assertEquals("REVOKED", revokedCredential.getStatus(), "El estado debe cambiar a REVOKED");

        // Verificar historial en tabla revocation_records
        RevocationRecordJpaEntity record = revocationRepository.findByCredentialIdAndTenantId(credential.getId(), tenantId).orElseThrow();
        assertEquals("Error tipográfico en el nombre del participante", record.getRevocationReason());
        assertNotNull(record.getRevokedAt());
    }

    @Test
    void testReissueAfterRevocation() {
        // Emitir primera credencial
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity firstCredential = issuanceService.issueCredential(issueReq);

        // Reacreditar (Revoca la anterior y emite una nueva)
        CredentialJpaEntity newCredential = issuanceService.reissueCredential(
                firstCredential.getId(), tenantId, "Corrección oficial de datos", userId
        );

        // Verificar estado de la primera credencial
        CredentialJpaEntity previousCredentialRefreshed = credentialRepository.findById(firstCredential.getId()).orElseThrow();
        assertEquals("REVOKED", previousCredentialRefreshed.getStatus(), "La credencial previa debe estar REVOKED");

        // Verificar estado de la nueva credencial y su linaje
        assertEquals("ACTIVE", newCredential.getStatus(), "La nueva credencial debe estar ACTIVE");
        assertEquals(firstCredential.getId(), newCredential.getPredecessorCredentialId(), "La nueva credencial debe apuntar a la previa como predecesor");
    }

    @Test
    void testSHA256HashCalculation() {
        String input = "CertiDigital Immutable Document Content Payload 2026";
        String hash1 = CredentialIssuanceApplicationService.computeSha256(input);
        String hash2 = CredentialIssuanceApplicationService.computeSha256(input);

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "El hash SHA-256 debe ser de 64 caracteres hexadecimales");
        assertEquals(hash1, hash2, "El cálculo del hash SHA-256 debe ser strictly determinista e inmutable");
    }

    @Test
    void testBlockchainSimulatorAnchoring() {
        // Emitir credencial
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(issueReq);

        // Consultar registro anclado en la blockchain simulada
        BlockchainRecordJpaEntity record = blockchainRecordRepository
                .findByCredentialIdAndTenantId(credential.getId(), tenantId)
                .orElseThrow();

        assertEquals("SIMULATOR", record.getNetwork(), "La red debe ser SIMULATOR");
        assertEquals(credential.getContentHash(), record.getContentHash(), "El hash anclado en blockchain debe coincidir exactamente con el hash de la credencial");
        assertTrue(record.getTxId().startsWith("0x"), "El Transaction ID debe comenzar con 0x");
        assertNotNull(record.getRegisteredAt(), "La fecha de registro en blockchain debe estar presente");
    }

    @Test
    void testPublicVerification() {
        // Emitir credencial
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(enrollment);

        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(issueReq);

        // Verificación pública mediante código público
        CredentialVerificationResult result = issuanceService.verifyCredential(credential.getPublicCode());

        assertTrue(result.isValid(), "La credencial emitida debe ser válida");
        assertEquals("VALID", result.getStatus());
        assertNotNull(result.getParticipantName());
        assertEquals("Curso de Arquitectura Software Blockchain & DDD", result.getEventName());
        assertEquals(credential.getContentHash(), result.getContentHash());
        assertEquals(credential.getBlockchainTxId(), result.getBlockchainTxId());
    }
}
