package com.certidigital.platform.participation;

import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.dto.PaymentVerificationResponse;
import com.certidigital.platform.payment.application.dto.SubmitVoucherRequest;
import com.certidigital.platform.payment.application.dto.VerifyPaymentRequest;
import com.certidigital.platform.payment.application.service.PaymentApplicationService;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
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

import com.certidigital.platform.participation.infrastructure.persistence.ParticipantJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.ParticipantJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentVoucherWorkflowTests {

    @Autowired
    private PaymentApplicationService paymentService;

    @Autowired
    private EnrollmentJpaRepository enrollmentRepository;

    @Autowired
    private EventJpaRepository eventRepository;

    @Autowired
    private ParticipantJpaRepository participantRepository;

    private String tenantId = "tenant-001-aaaa-bbbb-cccc-dddddddd";
    private String studentUserId = "user-student-0003-aaaa-bbbb-cccccccc";
    private String adminUserId = "user-admin-0001-aaaa-bbbb-cccccccc";

    private EnrollmentJpaEntity seededEnrollment;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(tenantId);

        EventJpaEntity event = new EventJpaEntity();
        event.setId(UUID.randomUUID().toString());
        event.setTenantId(tenantId);
        event.setName("Curso Yape Voucher Test");
        event.setEventType("COURSE");
        event.setMode("VIRTUAL");
        event.setStartDate(LocalDateTime.now());
        event.setEndDate(LocalDateTime.now().plusDays(5));
        event.setPrice(100.0);
        event.setStatus("PUBLISHED");
        eventRepository.save(event);

        ParticipantJpaEntity participant = new ParticipantJpaEntity();
        participant.setId(UUID.randomUUID().toString());
        participant.setEmail("juan@test.com");
        participant.setFullName("Juan Perez");
        participantRepository.save(participant);

        seededEnrollment = new EnrollmentJpaEntity();
        seededEnrollment.setId(UUID.randomUUID().toString());
        seededEnrollment.setTenantId(tenantId);
        seededEnrollment.setEventId(event.getId());
        seededEnrollment.setParticipant(participant);
        seededEnrollment.setStatus("PENDING");
        seededEnrollment.setPaymentStatus("PENDING_PAYMENT");
        enrollmentRepository.save(seededEnrollment);
    }

    @Test
    @DisplayName("1. El estudiante envía comprobante Yape y pasa a estado WAITING_VERIFICATION")
    void testSubmitVoucher() {
        SubmitVoucherRequest request = new SubmitVoucherRequest(
            seededEnrollment.getId(),
            "YAPE",
            "OP-984210",
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
            BigDecimal.valueOf(100.0)
        );

        PaymentResult result = paymentService.submitVoucher(request, studentUserId);

        assertTrue(result.isSuccess());
        assertNotNull(result.getExternalReference());

        EnrollmentJpaEntity updatedEnr = enrollmentRepository.findById(seededEnrollment.getId()).orElseThrow();
        assertEquals("WAITING_VERIFICATION", updatedEnr.getPaymentStatus());
    }

    @Test
    @DisplayName("2. El administrador consulta comprobantes pendientes y aprueba el pago")
    void testApproveVoucher() {
        // Alumno envía voucher
        SubmitVoucherRequest request = new SubmitVoucherRequest(
            seededEnrollment.getId(),
            "YAPE",
            "OP-984210",
            "data:image/png;base64,voucherImage",
            BigDecimal.valueOf(100.0)
        );
        PaymentResult submitResult = paymentService.submitVoucher(request, studentUserId);

        // Admin lista pendientes
        List<PaymentVerificationResponse> pending = paymentService.getPendingVerificationsForTenant(tenantId);
        assertFalse(pending.isEmpty());

        PaymentVerificationResponse target = pending.stream()
            .filter(p -> p.getPaymentId().equals(submitResult.getExternalReference()))
            .findFirst()
            .orElseThrow();

        assertEquals("OP-984210", target.getOperationNumber());
        assertEquals("YAPE", target.getPaymentMethod());

        // Admin aprueba
        VerifyPaymentRequest approveReq = new VerifyPaymentRequest("APPROVE", "Voucher Yape 984210 verificado correctamente en cuenta BCP.");
        PaymentVerificationResponse response = paymentService.verifyPaymentVoucher(target.getPaymentId(), approveReq, adminUserId);

        assertEquals("CONFIRMED", response.getPaymentStatus());

        EnrollmentJpaEntity finalEnr = enrollmentRepository.findById(seededEnrollment.getId()).orElseThrow();
        assertEquals("CONFIRMED", finalEnr.getStatus());
        assertEquals("COMPLETED", finalEnr.getPaymentStatus());
    }

    @Test
    @DisplayName("3. El administrador rechaza un comprobante inválido")
    void testRejectVoucher() {
        SubmitVoucherRequest request = new SubmitVoucherRequest(
            seededEnrollment.getId(),
            "BANK_TRANSFER",
            "OP-000000",
            "data:image/png;base64,badImage",
            BigDecimal.valueOf(100.0)
        );
        PaymentResult submitResult = paymentService.submitVoucher(request, studentUserId);

        VerifyPaymentRequest rejectReq = new VerifyPaymentRequest("REJECT", "El número de operación no figura en el extracto bancario.");
        PaymentVerificationResponse response = paymentService.verifyPaymentVoucher(submitResult.getExternalReference(), rejectReq, adminUserId);

        assertEquals("REJECTED", response.getPaymentStatus());

        EnrollmentJpaEntity finalEnr = enrollmentRepository.findById(seededEnrollment.getId()).orElseThrow();
        assertEquals("FAILED", finalEnr.getPaymentStatus());
    }
}
