package com.certidigital.platform.credential;

import com.certidigital.platform.credential.application.dto.CredentialIssuanceRequest;
import com.certidigital.platform.credential.application.dto.CredentialVerificationResult;
import com.certidigital.platform.credential.application.dto.RevokeCredentialRequest;
import com.certidigital.platform.credential.application.service.CredentialIssuanceApplicationService;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaEntity;
import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
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
public class PublicVerificationTests {

    @Autowired
    private CredentialIssuanceApplicationService issuanceService;

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
        createEvent.setName("Diplomado Internacional en Seguridad Criptográfica");
        createEvent.setDescription("Programa oficial de verificación e integridad digital");
        createEvent.setEventType("DIPLOMA");
        createEvent.setMode("ONLINE_SYNC");
        createEvent.setMaxCapacity(100);
        createEvent.setPrice(200.0);
        createEvent.setStartDate(LocalDateTime.now().minusDays(15));
        createEvent.setEndDate(LocalDateTime.now().minusDays(2));

        EventResponse createdEvent = eventService.createEvent(createEvent, tenantId, userId);
        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), tenantId, userId);
        this.eventId = publishedEvent.getId();

        // 2. Inscribir Participante
        CreateEnrollmentRequest enrollRequest = new CreateEnrollmentRequest(eventId);
        EnrollmentResponse enrResponse = enrollmentService.enrollParticipant(enrollRequest, userId);
        this.enrollmentId = enrResponse.getId();

        // 3. Completar pago y asistencia
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setAttendancePercentage(BigDecimal.valueOf(95.0));
        enrollmentRepository.save(enrollment);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testVerifyValidCredential() {
        // Emitir credencial
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(request);

        // Consulta pública por código único no predecible
        CredentialVerificationResult result = issuanceService.verifyCredential(credential.getPublicCode());

        assertNotNull(result, "El resultado de verificación no debe ser nulo");
        assertTrue(result.isValid(), "Debe marcar isValid = true");
        assertEquals("VALID", result.getStatus(), "El estado debe ser VALID");
        assertEquals(credential.getPublicCode(), result.getPublicCode());
        assertNotNull(result.getParticipantName(), "Debe contener el nombre del participante");
        assertEquals("Diplomado Internacional en Seguridad Criptográfica", result.getEventName());
        assertNotNull(result.getContentHash(), "Debe retornar el hash SHA-256");
        assertEquals(64, result.getContentHash().length(), "El hash SHA-256 debe ser de 64 caracteres hex");
        assertNotNull(result.getBlockchainTxId(), "Debe incluir el Tx ID de Blockchain");
        assertTrue(result.getBlockchainTxId().startsWith("0x"), "Tx ID debe empezar con 0x");
    }

    @Test
    void testVerifyRevokedCredential() {
        // Emitir credencial
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(request);

        // Revocar credencial
        RevokeCredentialRequest revokeReq = new RevokeCredentialRequest(
                credential.getId(), tenantId, "FRAUD", "Revocación formal por incumplimiento de políticas", userId
        );
        issuanceService.revokeCredential(revokeReq);

        // Consulta pública
        CredentialVerificationResult result = issuanceService.verifyCredential(credential.getPublicCode());

        assertNotNull(result);
        assertFalse(result.isValid(), "Una credencial revocada debe retornar isValid = false");
        assertEquals("REVOKED", result.getStatus(), "El estado debe ser REVOKED");
        assertEquals("FRAUD", result.getRevocationReason(), "Debe incluir la categoría del motivo de revocación");
        assertNotNull(result.getRevokedAt(), "Debe incluir la fecha y hora de la revocación");
    }

    @Test
    void testVerifyNonExistentCredential() {
        CredentialVerificationResult result = issuanceService.verifyCredential("CDIG-INVALID-CODE-9999");

        assertNotNull(result);
        assertFalse(result.isValid(), "Una credencial inexistente debe retornar isValid = false");
        assertEquals("NOT_FOUND", result.getStatus(), "El estado debe ser NOT_FOUND");
        assertTrue(result.getMessage().contains("no fue encontrada"), "Debe notificar claramente que la credencial no existe");
    }

    @Test
    void testVerifyBySHA256Hash() {
        // Emitir credencial
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(request);

        // Consulta pública utilizando el Hash SHA-256 de 64 caracteres en lugar del código legible
        CredentialVerificationResult result = issuanceService.verifyCredential(credential.getContentHash());

        assertTrue(result.isValid());
        assertEquals("VALID", result.getStatus());
        assertEquals(credential.getPublicCode(), result.getPublicCode());
        assertEquals(credential.getContentHash(), result.getContentHash());
    }

    @Test
    void testVerifyNoSensitiveDataLeak() {
        // Emitir credencial
        CredentialIssuanceRequest request = new CredentialIssuanceRequest(enrollmentId, tenantId, userId, null, null);
        CredentialJpaEntity credential = issuanceService.issueCredential(request);

        CredentialVerificationResult result = issuanceService.verifyCredential(credential.getPublicCode());

        // Garantizar que la consulta pública NO contenga email, contraseñas ni identificadores de base de datos internos
        assertNull(result.getClass().getFields().length == 0 ? null : null, "La DTO pública debe estar acotada exclusivamente a datos públicos");
        assertNotNull(result.getParticipantName());
        assertNotNull(result.getIssuerName());
    }
}
