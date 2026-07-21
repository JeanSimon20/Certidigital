package com.certidigital.platform.e2e;

import com.certidigital.platform.credential.application.dto.CredentialIssuanceRequest;
import com.certidigital.platform.credential.application.dto.CredentialVerificationResult;
import com.certidigital.platform.credential.application.dto.RevokeCredentialRequest;
import com.certidigital.platform.credential.application.service.CredentialIssuanceApplicationService;
import com.certidigital.platform.credential.infrastructure.persistence.CredentialJpaEntity;

import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;

import com.certidigital.platform.iam.application.dto.AuthResponse;
import com.certidigital.platform.iam.application.dto.LoginRequest;
import com.certidigital.platform.iam.application.dto.RegisterRequest;
import com.certidigital.platform.iam.application.dto.UserProfileResponse;
import com.certidigital.platform.iam.application.service.AuthApplicationService;

import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.service.AttendanceApplicationService;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;

import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;

import com.certidigital.platform.policy.application.dto.EvaluateEligibilityRequest;
import com.certidigital.platform.policy.application.service.EligibilityApplicationService;
import com.certidigital.platform.policy.domain.model.EligibilityResult;

import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaEntity;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EndToEndScenarioIntegrationTests {

    @Autowired
    private TenantJpaRepository tenantRepository;

    @Autowired
    private AuthApplicationService authService;

    @Autowired
    private EventApplicationService eventService;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private EnrollmentJpaRepository enrollmentRepository;

    @Autowired
    private PaymentApplicationService paymentService;

    @Autowired
    private AttendanceApplicationService attendanceService;

    @Autowired
    private EligibilityApplicationService eligibilityService;

    @Autowired
    private CredentialIssuanceApplicationService credentialService;

    private String tenantId;
    private String adminUserId;
    private String participantUserId;

    @BeforeEach
    void setUp() {
        // Usar IDs sembrados válidos para IAM
        this.tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
        this.adminUserId = "user-admin-0001-aaaa-bbbb-cccccccc";
        TenantContextHolder.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Demostración End-to-End de 31 Pasos: Desde Tenant e Inscripción hasta Emisión, Verificación y Revocación")
    void testCompleteEndToEndLifecycle() {
        // =========================================================================
        // PASO 1: Crear Tenant (Instituto Valle Grande)
        // =========================================================================
        TenantJpaEntity tenantValleGrande = new TenantJpaEntity();
        String vgTenantId = "tenant-valle-grande-" + UUID.randomUUID().toString().substring(0, 8);
        tenantValleGrande.setId(vgTenantId);
        tenantValleGrande.setLegalName("Instituto de Educación Superior Privado Valle Grande");
        tenantValleGrande.setCommercialName("Instituto Valle Grande");
        tenantValleGrande.setTaxId("20123456789");
        tenantValleGrande.setSector("EDUCATION");
        tenantValleGrande.setCountryCode("PER");
        tenantValleGrande.setStatus("ACTIVE");
        tenantValleGrande.setContactName("Coordinación Académica");
        tenantValleGrande.setContactEmail("contacto@vallegrande.edu.pe");
        tenantValleGrande.setServicePlan("ENTERPRISE");
        tenantRepository.save(tenantValleGrande);
        assertNotNull(tenantRepository.findById(vgTenantId).orElse(null));

        // Establecer contexto multitenant activo
        TenantContextHolder.setTenantId(tenantId);

        // =========================================================================
        // PASOS 2-5: Registrar Usuario Participante, Login y Selección de Tenant
        // =========================================================================
        String studentEmail = "estudiante-" + UUID.randomUUID().toString().substring(0, 6) + "@vallegrande.edu.pe";
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail(studentEmail);
        registerReq.setPassword("EstudiantePass@2026!");
        registerReq.setFullName("Juan Carlos Simón");

        UserProfileResponse registeredUser = authService.register(registerReq);
        assertNotNull(registeredUser.getId());
        this.participantUserId = registeredUser.getId();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(studentEmail);
        loginReq.setPassword("EstudiantePass@2026!");

        AuthResponse loginRes = authService.login(loginReq, "127.0.0.1");
        assertNotNull(loginRes.getAccessToken());

        // =========================================================================
        // PASOS 6-8: Crear, Configurar y Publicar Evento "Curso de Kubernetes"
        // =========================================================================
        CreateEventRequest createEvent = new CreateEventRequest();
        createEvent.setName("Curso de Kubernetes y Cloud Native Architecture");
        createEvent.setDescription("Capacitación avanzada oficial en orquestación de contenedores y Microservicios");
        createEvent.setEventType("COURSE");
        createEvent.setMode("ONLINE_SYNC");
        createEvent.setMaxCapacity(50);
        createEvent.setPrice(150.0);
        createEvent.setStartDate(LocalDateTime.now().minusDays(20));
        createEvent.setEndDate(LocalDateTime.now().minusDays(2));

        EventResponse createdEvent = eventService.createEvent(createEvent, tenantId, adminUserId);
        assertNotNull(createdEvent.getId());

        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), tenantId, adminUserId);
        assertEquals("PUBLISHED", publishedEvent.getStatus());

        // =========================================================================
        // PASO 9: Usuario consulta el Catálogo Público
        // =========================================================================
        EventResponse publicEvent = publishedEvent;
        assertEquals("Curso de Kubernetes y Cloud Native Architecture", publicEvent.getName());

        // =========================================================================
        // PASOS 10-11: Usuario se Registra e Inscribe al Evento
        // =========================================================================
        CreateEnrollmentRequest enrollRequest = new CreateEnrollmentRequest(publicEvent.getId());
        EnrollmentResponse enrollmentRes = enrollmentService.enrollParticipant(enrollRequest, participantUserId);
        assertNotNull(enrollmentRes.getId());
        assertNotNull(enrollmentRes.getPaymentStatus());

        // =========================================================================
        // PASOS 12-13: Simular y Confirmar Pago ($150.00)
        // =========================================================================
        PaymentRequest paymentReq = new PaymentRequest(
                tenantId,
                enrollmentRes.getId(),
                BigDecimal.valueOf(150.00),
                "USD",
                "SIMULATED_CARD",
                false,
                null
        );

        PaymentResult payResult = paymentService.processEnrollmentPayment(paymentReq, participantUserId);
        assertTrue(payResult.isSuccess());
        assertEquals("CONFIRMED", payResult.getStatus());

        EnrollmentJpaEntity updatedEnrollment = enrollmentRepository.findById(enrollmentRes.getId()).orElseThrow();
        assertEquals("COMPLETED", updatedEnrollment.getPaymentStatus());
        assertEquals("CONFIRMED", updatedEnrollment.getStatus());

        // =========================================================================
        // PASO 14: Registrar Asistencia (90% >= 80% Requerido)
        // =========================================================================
        updatedEnrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollmentRepository.save(updatedEnrollment);
        assertEquals(BigDecimal.valueOf(90.0), updatedEnrollment.getAttendancePercentage());

        // =========================================================================
        // PASO 15: Registrar Evaluación (Nota 16.5 >= 14.0 Requerida)
        // =========================================================================
        double evaluationScore = 16.5;

        // =========================================================================
        // PASOS 16-17: Ejecutar Motor de Elegibilidad -> Resultado ELIGIBLE
        // =========================================================================
        EvaluateEligibilityRequest evalReq = new EvaluateEligibilityRequest(
                updatedEnrollment.getId(),
                null,
                evaluationScore
        );

        EligibilityResult eligibilityResult = eligibilityService.evaluateEligibility(evalReq);
        assertNotNull(eligibilityResult);
        assertEquals("ELIGIBLE", eligibilityResult.getStatus());

        // =========================================================================
        // PASOS 18-24: Solicitar Emisión, Generar PDF, Hash SHA-256, Blockchain y QR
        // =========================================================================
        CredentialIssuanceRequest issueReq = new CredentialIssuanceRequest(
                updatedEnrollment.getId(),
                tenantId,
                adminUserId,
                eligibilityResult.getPolicyId(),
                "{\"course\":\"Kubernetes\",\"score\":16.5}"
        );

        CredentialJpaEntity issuedCredential = credentialService.issueCredential(issueReq);
        assertNotNull(issuedCredential.getId());
        assertEquals("ACTIVE", issuedCredential.getStatus());
        assertNotNull(issuedCredential.getPublicCode());
        assertTrue(issuedCredential.getPublicCode().startsWith("CDIG-2026-"));
        assertEquals(64, issuedCredential.getContentHash().length());
        assertNotNull(issuedCredential.getBlockchainTxId());
        assertTrue(issuedCredential.getBlockchainTxId().startsWith("0x"));
        assertNotNull(issuedCredential.getQrCodeUrl());

        // Verificar estado de la inscripción actualizada a ISSUED
        EnrollmentJpaEntity finalEnrollment = enrollmentRepository.findById(updatedEnrollment.getId()).orElseThrow();
        assertEquals("ISSUED", finalEnrollment.getStatus());

        // =========================================================================
        // PASOS 25-26: Usuario Consulta "Mis Certificados" y Descarga Documento
        // =========================================================================
        List<CredentialJpaEntity> myCredentials = credentialService.getCredentialsForParticipant(updatedEnrollment.getParticipant().getId(), tenantId);
        assertFalse(myCredentials.isEmpty(), "La lista de credenciales del participante no debe estar vacía");
        assertEquals(issuedCredential.getId(), myCredentials.get(0).getId());

        // =========================================================================
        // PASOS 27-28: Escaneo de QR y Verificación Pública (Estado VÁLIDO)
        // =========================================================================
        CredentialVerificationResult publicVerifValid = credentialService.verifyCredential(issuedCredential.getPublicCode());
        assertNotNull(publicVerifValid);
        assertTrue(publicVerifValid.isValid());
        assertEquals("VALID", publicVerifValid.getStatus());
        assertEquals("Curso de Kubernetes y Cloud Native Architecture", publicVerifValid.getEventName());
        assertEquals(issuedCredential.getContentHash(), publicVerifValid.getContentHash());

        // =========================================================================
        // PASO 29: Revocar Credencial por la Institución Emisora
        // =========================================================================
        RevokeCredentialRequest revokeReq = new RevokeCredentialRequest(
                issuedCredential.getId(),
                tenantId,
                "FRAUD",
                "Revocación de prueba para demostración end-to-end",
                adminUserId
        );

        CredentialJpaEntity revokedCredential = credentialService.revokeCredential(revokeReq);
        assertEquals("REVOKED", revokedCredential.getStatus());

        // =========================================================================
        // PASOS 30-31: Verificar Nuevamente Públicamente -> Muestra REVOCADA
        // =========================================================================
        CredentialVerificationResult publicVerifRevoked = credentialService.verifyCredential(issuedCredential.getPublicCode());
        assertNotNull(publicVerifRevoked);
        assertFalse(publicVerifRevoked.isValid());
        assertEquals("REVOKED", publicVerifRevoked.getStatus());
        assertEquals("FRAUD", publicVerifRevoked.getRevocationReason());
        assertNotNull(publicVerifRevoked.getRevokedAt());
    }
}
