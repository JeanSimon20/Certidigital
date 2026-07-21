package com.certidigital.platform.payment.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.PaymentRecordJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.PaymentRecordJpaRepository;
import com.certidigital.platform.payment.application.dto.PaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentResult;
import com.certidigital.platform.payment.application.port.PaymentGatewayPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.certidigital.platform.payment.application.dto.SubmitVoucherRequest;
import com.certidigital.platform.payment.application.dto.VerifyPaymentRequest;
import com.certidigital.platform.payment.application.dto.PaymentVerificationResponse;

import com.certidigital.platform.notification.application.service.NotificationApplicationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentApplicationService {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRecordJpaRepository paymentRecordRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final SecurityAuditService auditService;
    private final NotificationApplicationService notificationService;

    public PaymentApplicationService(
        PaymentGatewayPort paymentGatewayPort,
        PaymentRecordJpaRepository paymentRecordRepository,
        EnrollmentJpaRepository enrollmentRepository,
        EventJpaRepository eventRepository,
        SecurityAuditService auditService,
        NotificationApplicationService notificationService
    ) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRecordRepository = paymentRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResult processEnrollmentPayment(PaymentRequest request, String userId) {
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada: " + request.getEnrollmentId()));

        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Procesamiento vía pasarela de pago (puerto de abstracción)
        PaymentResult result = paymentGatewayPort.processPayment(request);

        PaymentRecordJpaEntity record = new PaymentRecordJpaEntity();
        record.setId(UUID.randomUUID().toString());
        record.setTenantId(enrollment.getTenantId());
        record.setEnrollment(enrollment);
        record.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        record.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        record.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "SIMULATED_CARD");

        if (result.isSuccess()) {
            record.setPaymentStatus("CONFIRMED");
            record.setExternalReference(result.getExternalReference());
            record.setReceiptUrl(result.getReceiptUrl());
            record.setPaymentDate(LocalDateTime.now());
            record.setConfirmedAt(LocalDateTime.now());
            record.setConfirmedBy(userId);

            // Actualizar estado de la inscripción
            enrollment.setStatus("CONFIRMED");
            enrollment.setPaymentStatus("COMPLETED");

            auditService.logSecurityEvent(
                "PAYMENT_CONFIRMED",
                userId,
                "PAYMENT",
                event.getName(),
                enrollment.getTenantId(),
                "PAYMENT",
                record.getId(),
                "SUCCESS",
                null,
                "{\"amount\":" + record.getAmount() + "}"
            );
        } else {
            record.setPaymentStatus("REJECTED");
            enrollment.setPaymentStatus("FAILED");

            auditService.logSecurityEvent(
                "PAYMENT_REJECTED",
                userId,
                "PAYMENT",
                event.getName(),
                enrollment.getTenantId(),
                "PAYMENT",
                record.getId(),
                "FAILURE",
                result.getErrorMessage(),
                null
            );
        }

        paymentRecordRepository.save(record);
        enrollmentRepository.save(enrollment);

        return result;
    }

    @Transactional
    public PaymentResult submitVoucher(SubmitVoucherRequest request, String userId) {
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada: " + request.getEnrollmentId()));

        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        PaymentRecordJpaEntity record = new PaymentRecordJpaEntity();
        record.setId(UUID.randomUUID().toString());
        record.setTenantId(enrollment.getTenantId());
        record.setEnrollment(enrollment);
        record.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(event.getPrice() != null ? event.getPrice() : 0.0));
        record.setCurrency("USD");
        record.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "YAPE");
        record.setOperationNumber(request.getOperationNumber());
        record.setReceiptUrl(request.getVoucherUrl());
        record.setPaymentStatus("WAITING_VERIFICATION");
        record.setPaymentDate(LocalDateTime.now());

        enrollment.setPaymentStatus("WAITING_VERIFICATION");

        paymentRecordRepository.save(record);
        enrollmentRepository.save(enrollment);

        auditService.logSecurityEvent(
            "VOUCHER_SUBMITTED",
            userId,
            "PAYMENT",
            event.getName(),
            enrollment.getTenantId(),
            "PAYMENT",
            record.getId(),
            "SUCCESS",
            null,
            "{\"operationNumber\":\"" + request.getOperationNumber() + "\",\"method\":\"" + request.getPaymentMethod() + "\"}"
        );

        // Notificar al estudiante y a la administración
        notificationService.createNotification(
            userId,
            enrollment.getTenantId(),
            "VOUCHER_SUBMITTED",
            "⌛ Comprobante en Revisión",
            "Tu comprobante N° " + request.getOperationNumber() + " para " + event.getName() + " ha sido recibido y está en verificación.",
            "/my-enrollments"
        );

        notificationService.createNotification(
            "user-admin-0001-aaaa-bbbb-cccccccc",
            enrollment.getTenantId(),
            "VOUCHER_SUBMITTED",
            "💸 Comprobante Yape/BCP Recibido",
            "Se ha recibido el comprobante N° " + request.getOperationNumber() + " para el evento " + event.getName() + ".",
            "/admin/payment-verifications"
        );

        return new PaymentResult(true, "WAITING_VERIFICATION", record.getId(), request.getVoucherUrl(), null);
    }

    @Transactional(readOnly = true)
    public List<PaymentVerificationResponse> getPendingVerificationsForTenant(String tenantId) {
        return paymentRecordRepository.findAll().stream()
            .filter(p -> p.getTenantId().equals(tenantId) && ("WAITING_VERIFICATION".equalsIgnoreCase(p.getPaymentStatus()) || "PENDING".equalsIgnoreCase(p.getPaymentStatus())))
            .map(this::mapToVerificationResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PaymentVerificationResponse verifyPaymentVoucher(String paymentId, VerifyPaymentRequest request, String adminUserId) {
        PaymentRecordJpaEntity record = paymentRecordRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Registro de pago no encontrado con ID: " + paymentId));

        EnrollmentJpaEntity enrollment = record.getEnrollment();
        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        String studentUserId = enrollment.getParticipant() != null ? enrollment.getParticipant().getIdentityUserId() : "user-student-0003-aaaa-bbbb-cccccccc";

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            record.setPaymentStatus("CONFIRMED");
            record.setConfirmedAt(LocalDateTime.now());
            record.setConfirmedBy(adminUserId);
            record.setNotes(request.getNotes() != null ? request.getNotes() : "Comprobante verificado y aprobado por administración");

            enrollment.setStatus("CONFIRMED");
            enrollment.setPaymentStatus("COMPLETED");

            auditService.logSecurityEvent(
                "PAYMENT_VOUCHER_APPROVED",
                adminUserId,
                "PAYMENT",
                event.getName(),
                enrollment.getTenantId(),
                "PAYMENT",
                record.getId(),
                "SUCCESS",
                null,
                "{\"operationNumber\":\"" + record.getOperationNumber() + "\"}"
            );

            notificationService.createNotification(
                studentUserId,
                enrollment.getTenantId(),
                "PAYMENT_APPROVED",
                "✅ ¡Pago Aprobado!",
                "Tu pago para " + event.getName() + " fue verificado exitosamente. Tu matrícula está confirmada.",
                "/my-credentials"
            );
        } else {
            record.setPaymentStatus("REJECTED");
            record.setNotes(request.getNotes() != null ? request.getNotes() : "Comprobante no válido o ilegible");

            enrollment.setPaymentStatus("FAILED");

            auditService.logSecurityEvent(
                "PAYMENT_VOUCHER_REJECTED",
                adminUserId,
                "PAYMENT",
                event.getName(),
                enrollment.getTenantId(),
                "PAYMENT",
                record.getId(),
                "FAILURE",
                request.getNotes(),
                null
            );

            notificationService.createNotification(
                studentUserId,
                enrollment.getTenantId(),
                "PAYMENT_REJECTED",
                "❌ Comprobante Rechazado",
                "Tu comprobante de pago para " + event.getName() + " fue rechazado: " + record.getNotes(),
                "/my-enrollments"
            );
        }

        paymentRecordRepository.save(record);
        enrollmentRepository.save(enrollment);

        return mapToVerificationResponse(record);
    }

    private PaymentVerificationResponse mapToVerificationResponse(PaymentRecordJpaEntity p) {
        EnrollmentJpaEntity enr = p.getEnrollment();
        EventJpaEntity event = eventRepository.findById(enr.getEventId()).orElse(null);
        String eventName = event != null ? event.getName() : "Evento Académico";

        String participantName = "Participante";
        String participantEmail = "email@participante.com";
        if (enr.getParticipant() != null) {
            participantName = enr.getParticipant().getFullName();
            participantEmail = enr.getParticipant().getEmail();
        }

        return new PaymentVerificationResponse(
            p.getId(),
            enr.getId(),
            participantName,
            participantEmail,
            eventName,
            p.getAmount(),
            p.getCurrency(),
            p.getPaymentMethod(),
            p.getOperationNumber(),
            p.getReceiptUrl(),
            p.getPaymentStatus(),
            p.getPaymentDate(),
            p.getNotes()
        );
    }
}
