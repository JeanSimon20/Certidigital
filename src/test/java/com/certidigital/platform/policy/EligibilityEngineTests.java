package com.certidigital.platform.policy;

import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;
import com.certidigital.platform.policy.application.dto.EvaluateEligibilityRequest;
import com.certidigital.platform.policy.application.service.EligibilityApplicationService;
import com.certidigital.platform.policy.domain.model.EligibilityResult;
import com.certidigital.platform.policy.domain.model.RuleEvaluationDetail;
import com.certidigital.platform.shared.infrastructure.security.TenantAccessDeniedException;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EligibilityEngineTests {

    @Autowired
    private EventApplicationService eventService;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private PaymentApplicationService paymentService;

    @Autowired
    private EligibilityApplicationService eligibilityService;

    @Autowired
    private EnrollmentJpaRepository enrollmentRepository;

    private String tenantId;
    private String userId;
    private String eventId;
    private String enrollmentId;

    @BeforeEach
    void setUp() {
        tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
        userId = "user-admin-0001-aaaa-bbbb-cccccccc";
        TenantContextHolder.setTenantId(tenantId);

        // Crear evento de prueba (Curso Kubernetes)
        CreateEventRequest createEvent = new CreateEventRequest();
        createEvent.setName("Curso Kubernetes y Microservicios");
        createEvent.setDescription("Programa intensivo de orquestación de contenedores");
        createEvent.setEventType("COURSE");
        createEvent.setMode("ONLINE_SYNC");
        createEvent.setStartDate(LocalDateTime.now().plusDays(1));
        createEvent.setEndDate(LocalDateTime.now().plusDays(10));
        createEvent.setMaxCapacity(50);
        createEvent.setPrice(150.0);

        EventResponse createdEvent = eventService.createEvent(createEvent, tenantId, userId);
        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), tenantId, userId);
        eventId = publishedEvent.getId();

        // Inscribir participante
        CreateEnrollmentRequest enrollRequest = new CreateEnrollmentRequest(eventId);
        EnrollmentResponse enrResponse = enrollmentService.enrollParticipant(enrollRequest, userId);
        enrollmentId = enrResponse.getId();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("1. Participante elegible: Cumple pago, asistencia (85%) y nota (16.0)")
    void testEligibleParticipant() {
        // Confirmar pago
        PaymentRequest paymentRequest = new PaymentRequest(
                tenantId,
                enrollmentId,
                BigDecimal.valueOf(150.0),
                "USD",
                "SIMULATED_CARD",
                false,
                null
        );
        paymentService.processEnrollmentPayment(paymentRequest, userId);

        // Asignar asistencia 85%
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setAttendancePercentage(BigDecimal.valueOf(85.0));
        enrollmentRepository.save(enrollment);

        // Evaluar elegibilidad con nota 16.0
        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 16.0);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("ELIGIBLE", result.getStatus(), "El participante debe ser ELIGIBLE");
        assertEquals(3, result.getRuleResults().size(), "Debe haber 3 reglas evaluadas");
        assertTrue(result.getRuleResults().stream().allMatch(RuleEvaluationDetail::isPassed), "Todas las reglas deben cumplirse");
    }

    @Test
    @DisplayName("2. Pago no confirmado: Estado PENDING resulta en NOT_ELIGIBLE")
    void testPaymentNotConfirmed() {
        // Asignar asistencia 90% y nota 15.0 pero SIN realizar el pago
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setAttendancePercentage(BigDecimal.valueOf(90.0));
        enrollment.setPaymentStatus("PENDING_PAYMENT");
        enrollmentRepository.save(enrollment);

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 15.0);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("NOT_ELIGIBLE", result.getStatus());
        RuleEvaluationDetail paymentRule = result.getRuleResults().stream()
                .filter(r -> "PAYMENT_CONFIRMED".equals(r.getRuleType()))
                .findFirst()
                .orElseThrow();

        assertFalse(paymentRule.isPassed(), "La regla de pago debe haber fallado");
        assertTrue(paymentRule.getReason().contains("no ha sido confirmado"), "El motivo debe indicar fallo de pago");
    }

    @Test
    @DisplayName("3. Asistencia insuficiente: 70% asistencia (< 80%) resulta en NOT_ELIGIBLE")
    void testInsufficientAttendance() {
        // Confirmar pago
        PaymentRequest paymentRequest = new PaymentRequest(
                tenantId,
                enrollmentId,
                BigDecimal.valueOf(150.0),
                "USD",
                "SIMULATED_CARD",
                false,
                null
        );
        paymentService.processEnrollmentPayment(paymentRequest, userId);

        // Asignar asistencia de solo 70%
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setAttendancePercentage(BigDecimal.valueOf(70.0));
        enrollmentRepository.save(enrollment);

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 18.0);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("NOT_ELIGIBLE", result.getStatus());
        RuleEvaluationDetail attRule = result.getRuleResults().stream()
                .filter(r -> "ATTENDANCE_PERCENTAGE".equals(r.getRuleType()))
                .findFirst()
                .orElseThrow();

        assertFalse(attRule.isPassed());
        assertEquals("70.00%", attRule.getObtainedValue());
        assertTrue(attRule.getReason().contains("insuficiente"));
    }

    @Test
    @DisplayName("4. Evaluación insuficiente: Nota 11.5 (< 14.0) resulta en NOT_ELIGIBLE")
    void testInsufficientEvaluation() {
        // Confirmar pago
        PaymentRequest paymentRequest = new PaymentRequest(
                tenantId,
                enrollmentId,
                BigDecimal.valueOf(150.0),
                "USD",
                "SIMULATED_CARD",
                false,
                null
        );
        paymentService.processPayment(paymentRequest);

        // Asignar asistencia 95%
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setAttendancePercentage(BigDecimal.valueOf(95.0));
        enrollmentRepository.save(enrollment);

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 11.5);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("NOT_ELIGIBLE", result.getStatus());
        RuleEvaluationDetail scoreRule = result.getRuleResults().stream()
                .filter(r -> "EVALUATION_SCORE".equals(r.getRuleType()))
                .findFirst()
                .orElseThrow();

        assertFalse(scoreRule.isPassed());
        assertEquals("11.50", scoreRule.getObtainedValue());
    }

    @Test
    @DisplayName("5. Varias reglas incumplidas: Reporta múltiples fallos en la trazabilidad")
    void testMultipleUnfulfilledRules() {
        // Asignar asistencia 60% y nota 10.0 sin pago
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        enrollment.setAttendancePercentage(BigDecimal.valueOf(60.0));
        enrollment.setPaymentStatus("PENDING_PAYMENT");
        enrollmentRepository.save(enrollment);

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 10.0);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("NOT_ELIGIBLE", result.getStatus());
        long failedRulesCount = result.getRuleResults().stream().filter(r -> !r.isPassed()).count();
        assertEquals(3, failedRulesCount, "Las 3 reglas (pago, asistencia y nota) deben haber fallado");
    }

    @Test
    @DisplayName("6. Evento incompleto: Evento en estado DRAFT resulta en NOT_ELIGIBLE")
    void testIncompleteEvent() {
        // Crear evento en estado borrador (DRAFT)
        CreateEventRequest createEvent = new CreateEventRequest();
        createEvent.setName("Curso Docker DRAFT");
        createEvent.setEventType("COURSE");
        createEvent.setMode("ONLINE_ASYNC");
        createEvent.setPrice(0.0);

        EventResponse draftEvent = eventService.createEvent(createEvent, tenantId, userId);

        CreateEnrollmentRequest enrollRequest = new CreateEnrollmentRequest(draftEvent.getId());
        EnrollmentResponse draftEnr = enrollmentService.enrollParticipant(enrollRequest, userId);

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(draftEnr.getId(), null, 20.0);
        EligibilityResult result = eligibilityService.evaluateEligibility(req);

        assertEquals("NOT_ELIGIBLE", result.getStatus());
        assertTrue(result.getSummaryReason().contains("incompleto") || result.getSummaryReason().contains("DRAFT"));
    }

    @Test
    @DisplayName("7. Aislamiento Cross-Tenant: Deniega evaluación cuando pertenece a otra organización")
    void testCrossTenant() {
        // Cambiar contexto a otro tenant ajeno
        TenantContextHolder.setTenantId("tenant-other-9999-aaaa-bbbb");

        EvaluateEligibilityRequest req = new EvaluateEligibilityRequest(enrollmentId, null, 18.0);
        assertThrows(TenantAccessDeniedException.class, () -> eligibilityService.evaluateEligibility(req));
    }
}
