package com.certidigital.platform.participation;

import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
import com.certidigital.platform.participation.application.dto.AttendanceRecordResponse;
import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.dto.RecordAttendanceRequest;
import com.certidigital.platform.participation.application.service.AttendanceApplicationService;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentAndAttendanceTests {

    @Autowired
    private EventApplicationService eventService;

    @Autowired
    private EnrollmentApplicationService enrollmentService;

    @Autowired
    private PaymentApplicationService paymentService;

    @Autowired
    private AttendanceApplicationService attendanceService;

    @Autowired
    private com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository eventRepository;

    @Autowired
    private com.certidigital.platform.event.infrastructure.persistence.EventSessionJpaRepository sessionRepository;

    private String tenantId;
    private String userId;
    private String eventId;
    private String sessionId;

    @BeforeEach
    void setUp() {
        tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
        userId = "user-admin-0001-aaaa-bbbb-cccccccc";
        sessionId = UUID.randomUUID().toString();
        TenantContextHolder.setTenantId(tenantId);

        // Crear y publicar evento de prueba
        CreateEventRequest createEvent = new CreateEventRequest();
        createEvent.setName("Taller de Seguridad y Microservicios");
        createEvent.setDescription("Curso práctico de Spring Security y React");
        createEvent.setEventType("WORKSHOP");
        createEvent.setMode("IN_PERSON");
        createEvent.setStartDate(LocalDateTime.now().plusDays(1));
        createEvent.setEndDate(LocalDateTime.now().plusDays(2));
        createEvent.setMaxCapacity(30);
        createEvent.setPrice(0.0);

        EventResponse createdEvent = eventService.createEvent(createEvent, tenantId, userId);
        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), tenantId, userId);
        eventId = publishedEvent.getId();

        // Crear sesión en base de datos para foreign key constraint
        com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity eventEntity =
            eventRepository.findById(eventId).orElseThrow();

        com.certidigital.platform.event.infrastructure.persistence.EventSessionJpaEntity sessionEntity =
            new com.certidigital.platform.event.infrastructure.persistence.EventSessionJpaEntity();
        sessionEntity.setId(sessionId);
        sessionEntity.setTenantId(tenantId);
        sessionEntity.setEvent(eventEntity);
        sessionEntity.setName("Sesión 1: Introducción a la Arquitectura");
        sessionEntity.setSessionDate(LocalDateTime.now().plusDays(1));
        sessionEntity.setDurationMinutes(120);

        sessionRepository.save(sessionEntity);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("1. Pago gratuito: Inscripción en evento de costo $0 resulta en estado CONFIRMED")
    void testFreeEventPayment() {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest(eventId);
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(request, userId);

        assertNotNull(enrollment.getId());
        assertEquals("CONFIRMED", enrollment.getStatus());
        assertEquals("COMPLETED", enrollment.getPaymentStatus());
    }

    @Test
    @DisplayName("2. Evento de pago: Creación de inscripción para evento con costo")
    void testPaidEventPaymentCreation() {
        // Crear evento de pago
        CreateEventRequest createPaid = new CreateEventRequest();
        createPaid.setName("Diplomado Criptografía");
        createPaid.setDescription("Curso pagado");
        createPaid.setEventType("DIPLOMA");
        createPaid.setMode("VIRTUAL");
        createPaid.setStartDate(LocalDateTime.now().plusDays(5));
        createPaid.setEndDate(LocalDateTime.now().plusDays(10));
        createPaid.setMaxCapacity(20);
        createPaid.setPrice(150.0);

        EventResponse event = eventService.createEvent(createPaid, tenantId, userId);
        eventService.publishEvent(event.getId(), tenantId, userId);

        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(event.getId()), userId);

        assertNotNull(enrollment.getId());
        // Inscripción inicial registrada
        assertEquals("CONFIRMED", enrollment.getStatus());
    }

    @Test
    @DisplayName("3. Pago confirmado: Proceso exitoso vía PaymentSimulatorAdapter")
    void testPaymentConfirmation() {
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(eventId), userId);

        PaymentRequest paymentRequest = new PaymentRequest(
            tenantId,
            enrollment.getId(),
            BigDecimal.valueOf(100.00),
            "USD",
            "SIMULATED_CARD",
            false,
            null
        );

        PaymentResult result = paymentService.processEnrollmentPayment(paymentRequest, userId);

        assertTrue(result.isSuccess());
        assertEquals("CONFIRMED", result.getStatus());
        assertNotNull(result.getExternalReference());
        assertNotNull(result.getReceiptUrl());
    }

    @Test
    @DisplayName("4. Pago rechazado: Simulación de fallo en pasarela de pago")
    void testPaymentRejection() {
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(eventId), userId);

        PaymentRequest paymentRequest = new PaymentRequest(
            tenantId,
            enrollment.getId(),
            BigDecimal.valueOf(100.00),
            "USD",
            "SIMULATED_CARD",
            true,
            "Tarjeta de crédito rechazada por emisor"
        );

        PaymentResult result = paymentService.processEnrollmentPayment(paymentRequest, userId);

        assertFalse(result.isSuccess());
        assertEquals("REJECTED", result.getStatus());
        assertEquals("Tarjeta de crédito rechazada por emisor", result.getErrorMessage());
    }

    @Test
    @DisplayName("5. Asistencia válida: Registro manual de asistencia para participante inscrito")
    void testValidAttendanceRegistration() {
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(eventId), userId);

        RecordAttendanceRequest attendanceReq = new RecordAttendanceRequest(
            enrollment.getId(),
            sessionId,
            true,
            "MANUAL",
            "Participante asistió puntualmente"
        );

        AttendanceRecordResponse response = attendanceService.recordAttendance(attendanceReq, tenantId, userId);

        assertNotNull(response.getId());
        assertEquals(enrollment.getId(), response.getEnrollmentId());
        assertTrue(response.getAttended());
        assertEquals(userId, response.getRecordedBy());
    }

    @Test
    @DisplayName("6. Asistencia inválida: Error al registrar asistencia para un ID de inscripción inexistente")
    void testInvalidAttendanceNotEnrolled() {
        String invalidEnrollmentId = "enrollment-non-existent-" + UUID.randomUUID();

        RecordAttendanceRequest attendanceReq = new RecordAttendanceRequest(
            invalidEnrollmentId,
            sessionId,
            true,
            "MANUAL",
            "Test"
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.recordAttendance(attendanceReq, tenantId, userId);
        });

        assertTrue(exception.getMessage().contains("Inscripción no encontrada"));
    }

    @Test
    @DisplayName("7. Asistencia duplicada: Error al intentar registrar asistencia dos veces para la misma sesión")
    void testDuplicateAttendanceRegistration() {
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(eventId), userId);

        RecordAttendanceRequest attendanceReq = new RecordAttendanceRequest(
            enrollment.getId(),
            sessionId,
            true,
            "MANUAL",
            "Primer registro"
        );

        attendanceService.recordAttendance(attendanceReq, tenantId, userId);

        // Segundo intento de asistencia para la misma sesión
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.recordAttendance(attendanceReq, tenantId, userId);
        });

        assertTrue(exception.getMessage().contains("Asistencia duplicada"));
    }

    @Test
    @DisplayName("8. Cross-Tenant: Denegación de registro de asistencia entre Tenants distintos")
    void testCrossTenantAccessDenied() {
        EnrollmentResponse enrollment = enrollmentService.enrollParticipant(new CreateEnrollmentRequest(eventId), userId);

        String otherTenantId = "tenant-other-999-aaaa-bbbb-cccc";

        RecordAttendanceRequest attendanceReq = new RecordAttendanceRequest(
            enrollment.getId(),
            sessionId,
            true,
            "MANUAL",
            "Intento de registro cross-tenant"
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.recordAttendance(attendanceReq, otherTenantId, userId);
        });

        assertTrue(exception.getMessage().contains("Acceso Denegado"));
    }

    @Test
    @DisplayName("9. Sin permisos: Manejo de autenticación y aislamiento en el contexto")
    void testMissingPermissionsAccessDenied() {
        // Verificar que TenantContextHolder aisladamente responde cuando falta el tenant
        TenantContextHolder.clear();
        assertThrows(com.certidigital.platform.shared.infrastructure.security.TenantAccessDeniedException.class, TenantContextHolder::requireTenantId);
    }
}
