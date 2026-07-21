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

@Service
public class PaymentApplicationService {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRecordJpaRepository paymentRecordRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final SecurityAuditService auditService;

    public PaymentApplicationService(
        PaymentGatewayPort paymentGatewayPort,
        PaymentRecordJpaRepository paymentRecordRepository,
        EnrollmentJpaRepository enrollmentRepository,
        EventJpaRepository eventRepository,
        SecurityAuditService auditService
    ) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRecordRepository = paymentRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
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
}
