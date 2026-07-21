package com.certidigital.platform.participation.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.participation.application.dto.AttendanceRecordResponse;
import com.certidigital.platform.participation.application.dto.RecordAttendanceRequest;
import com.certidigital.platform.participation.infrastructure.persistence.AttendanceRecordJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.AttendanceRecordJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceApplicationService {

    private final AttendanceRecordJpaRepository attendanceRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final SecurityAuditService auditService;

    public AttendanceApplicationService(
        AttendanceRecordJpaRepository attendanceRepository,
        EnrollmentJpaRepository enrollmentRepository,
        EventJpaRepository eventRepository,
        SecurityAuditService auditService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AttendanceRecordResponse recordAttendance(RecordAttendanceRequest request, String tenantId, String recordedByUserId) {
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(request.getEnrollmentId())
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada para el ID: " + request.getEnrollmentId()));

        // Validación de Tenant Isolation
        if (!enrollment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso Denegado: La inscripción pertenece a otra organización.");
        }

        // Validación de Inscripción Activa/Confirmada
        if (!"CONFIRMED".equalsIgnoreCase(enrollment.getStatus()) && !"ACTIVE".equalsIgnoreCase(enrollment.getStatus())) {
            throw new IllegalArgumentException("No se puede registrar asistencia. La inscripción no se encuentra confirmada o activa.");
        }

        // Validación de Duplicados
        if (attendanceRepository.existsByEnrollmentIdAndSessionId(enrollment.getId(), request.getSessionId())) {
            throw new IllegalStateException("Asistencia duplicada: Ya existe un registro de asistencia para esta sesión.");
        }

        EventJpaEntity event = eventRepository.findById(enrollment.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        AttendanceRecordJpaEntity attendance = new AttendanceRecordJpaEntity();
        attendance.setId(UUID.randomUUID().toString());
        attendance.setTenantId(tenantId);
        attendance.setEnrollment(enrollment);
        attendance.setSessionId(request.getSessionId());
        attendance.setAttended(request.getAttended() != null ? request.getAttended() : true);
        attendance.setRecordedBy(recordedByUserId);
        attendance.setRecordedAt(LocalDateTime.now());
        attendance.setNotes(request.getNotes() != null ? request.getNotes() : ("Método: " + (request.getMethod() != null ? request.getMethod() : "MANUAL")));

        AttendanceRecordJpaEntity saved = attendanceRepository.save(attendance);

        // Recalcular porcentaje de asistencia
        recalculateAttendancePercentage(enrollment, event);

        auditService.logSecurityEvent(
            "ATTENDANCE_RECORDED",
            recordedByUserId,
            "ATTENDANCE",
            event.getName(),
            tenantId,
            "ATTENDANCE",
            saved.getId(),
            "SUCCESS",
            null,
            "{\"sessionId\":\"" + request.getSessionId() + "\",\"method\":\"" + request.getMethod() + "\"}"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getEnrollmentAttendance(String enrollmentId, String tenantId) {
        EnrollmentJpaEntity enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (!enrollment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso Denegado");
        }

        return attendanceRepository.findAllByEnrollmentId(enrollmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getSessionAttendance(String sessionId, String tenantId) {
        return attendanceRepository.findAllByTenantIdAndSessionId(tenantId, sessionId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private void recalculateAttendancePercentage(EnrollmentJpaEntity enrollment, EventJpaEntity event) {
        List<AttendanceRecordJpaEntity> records = attendanceRepository.findAllByEnrollmentId(enrollment.getId());
        long attendedCount = records.stream().filter(r -> Boolean.TRUE.equals(r.getAttended())).count();
        int totalSessions = (event.getSessions() != null && !event.getSessions().isEmpty()) ? event.getSessions().size() : 1;

        BigDecimal percentage = BigDecimal.valueOf(attendedCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP);

        enrollment.setAttendancePercentage(percentage);
        enrollmentRepository.save(enrollment);
    }

    private AttendanceRecordResponse mapToResponse(AttendanceRecordJpaEntity entity) {
        String participantId = entity.getEnrollment() != null && entity.getEnrollment().getParticipant() != null
            ? entity.getEnrollment().getParticipant().getId()
            : "";

        String participantName = entity.getEnrollment() != null && entity.getEnrollment().getParticipant() != null
            ? entity.getEnrollment().getParticipant().getFullName()
            : "Participante";

        String method = "MANUAL";
        if (entity.getNotes() != null && entity.getNotes().contains("Método: ")) {
            method = entity.getNotes().replace("Método: ", "");
        }

        return new AttendanceRecordResponse(
            entity.getId(),
            entity.getTenantId(),
            entity.getEnrollment() != null ? entity.getEnrollment().getId() : "",
            participantId,
            participantName,
            entity.getSessionId(),
            entity.getAttended(),
            method,
            entity.getRecordedBy(),
            entity.getRecordedAt(),
            entity.getNotes()
        );
    }
}
