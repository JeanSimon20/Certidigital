package com.certidigital.platform.participation.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.notification.application.service.NotificationApplicationService;
import com.certidigital.platform.participation.application.dto.EvaluationResultResponse;
import com.certidigital.platform.participation.application.dto.RecordEvaluationRequest;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EvaluationResultJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EvaluationResultJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EvaluationApplicationService {

    private final EvaluationResultJpaRepository evaluationRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final SecurityAuditService auditService;
    private final NotificationApplicationService notificationService;

    public EvaluationApplicationService(
        EvaluationResultJpaRepository evaluationRepository,
        EnrollmentJpaRepository enrollmentRepository,
        EventJpaRepository eventRepository,
        SecurityAuditService auditService,
        NotificationApplicationService notificationService
    ) {
        this.evaluationRepository = evaluationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public EvaluationResultResponse recordEvaluation(RecordEvaluationRequest request, String tenantId, String recordedByUserId) {
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada: " + request.getEnrollmentId()));

        if (!enrollment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso Denegado: La inscripción pertenece a otra organización.");
        }

        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        BigDecimal maxScore = request.getMaxScore() != null ? request.getMaxScore() : new BigDecimal("20.00");
        BigDecimal passingScore = request.getPassingScore() != null ? request.getPassingScore() : new BigDecimal("14.00");
        boolean passed = request.getScore().compareTo(passingScore) >= 0;

        EvaluationResultJpaEntity entity = new EvaluationResultJpaEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantId(tenantId);
        entity.setEnrollment(enrollment);
        entity.setEvaluationName(request.getEvaluationName() != null ? request.getEvaluationName() : "Evaluación Final / Nota General");
        entity.setEvaluationType(request.getEvaluationType() != null ? request.getEvaluationType() : "EXAM");
        entity.setScore(request.getScore());
        entity.setMaxScore(maxScore);
        entity.setPassingScore(passingScore);
        entity.setPassed(passed);
        entity.setRecordedBy(recordedByUserId);
        entity.setRecordedAt(LocalDateTime.now());

        EvaluationResultJpaEntity saved = evaluationRepository.save(entity);

        // Actualizar nota general del Enrollment
        enrollment.setOverallScore(request.getScore());
        enrollmentRepository.save(enrollment);

        auditService.logSecurityEvent(
            "EVALUATION_RECORDED",
            recordedByUserId,
            "EVALUATION",
            event.getName(),
            tenantId,
            "EVALUATION",
            saved.getId(),
            "SUCCESS",
            null,
            "{\"score\":" + request.getScore() + ",\"passed\":" + passed + "}"
        );

        // Notificar al estudiante
        String studentUserId = enrollment.getParticipant() != null ? enrollment.getParticipant().getIdentityUserId() : "user-student-0003-aaaa-bbbb-cccccccc";
        notificationService.createNotification(
            studentUserId,
            tenantId,
            "EVALUATION_RECORDED",
            "🎓 Calificación Publicada",
            "Tu nota para " + event.getName() + " ha sido registrada (" + request.getScore() + "/" + maxScore + "). Estado: " + (passed ? "APROBADO" : "REPROBADO"),
            "/my-credentials"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResultResponse> getEnrollmentEvaluations(String enrollmentId, String tenantId) {
        return evaluationRepository.findAllByEnrollmentId(enrollmentId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private EvaluationResultResponse mapToResponse(EvaluationResultJpaEntity entity) {
        return new EvaluationResultResponse(
            entity.getId(),
            entity.getTenantId(),
            entity.getEnrollment() != null ? entity.getEnrollment().getId() : "",
            entity.getEvaluationName(),
            entity.getEvaluationType(),
            entity.getScore(),
            entity.getMaxScore(),
            entity.getPassingScore(),
            entity.getPassed(),
            entity.getRecordedAt()
        );
    }
}
