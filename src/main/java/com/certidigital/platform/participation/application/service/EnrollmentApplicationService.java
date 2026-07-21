package com.certidigital.platform.participation.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.iam.infrastructure.persistence.UserJpaEntity;
import com.certidigital.platform.iam.infrastructure.persistence.UserJpaRepository;
import com.certidigital.platform.participation.application.dto.CreateEnrollmentRequest;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.ParticipantJpaEntity;
import com.certidigital.platform.participation.infrastructure.persistence.ParticipantJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnrollmentApplicationService {

    private final EnrollmentJpaRepository enrollmentRepository;
    private final EventJpaRepository eventRepository;
    private final ParticipantJpaRepository participantRepository;
    private final UserJpaRepository userRepository;
    private final SecurityAuditService auditService;

    public EnrollmentApplicationService(
        EnrollmentJpaRepository enrollmentRepository,
        EventJpaRepository eventRepository,
        ParticipantJpaRepository participantRepository,
        UserJpaRepository userRepository,
        SecurityAuditService auditService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public EnrollmentResponse enrollParticipant(CreateEnrollmentRequest request, String userId) {
        EventJpaEntity event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + request.getEventId()));

        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new IllegalArgumentException("El evento no se encuentra disponible para inscripciones.");
        }

        // Obtener o crear entidad Participant para el usuario
        ParticipantJpaEntity participant = participantRepository.findByIdentityUserId(userId)
            .orElseGet(() -> {
                UserJpaEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                ParticipantJpaEntity p = new ParticipantJpaEntity();
                p.setId(UUID.randomUUID().toString());
                p.setIdentityUserId(user.getId());
                p.setFullName(user.getFullName());
                p.setEmail(user.getEmail());
                return participantRepository.save(p);
            });

        if (enrollmentRepository.existsByEventIdAndParticipantId(event.getId(), participant.getId())) {
            throw new IllegalArgumentException("El usuario ya cuenta con una inscripción registrada en este evento.");
        }

        // Verificar capacidad disponible
        List<EnrollmentJpaEntity> existingEnrollments = enrollmentRepository.findAllByEventId(event.getId());
        if (event.getMaxCapacity() != null && existingEnrollments.size() >= event.getMaxCapacity()) {
            throw new IllegalArgumentException("La capacidad máxima del evento ha sido alcanzada.");
        }

        EnrollmentJpaEntity enrollment = new EnrollmentJpaEntity();
        enrollment.setId(UUID.randomUUID().toString());
        enrollment.setEventId(event.getId());
        enrollment.setParticipant(participant);
        enrollment.setTenantId(event.getTenantId());
        enrollment.setStatus("CONFIRMED"); // Eventos base sin costo
        enrollment.setPaymentStatus("COMPLETED");
        enrollment.setEnrolledBy(userId);

        EnrollmentJpaEntity saved = enrollmentRepository.save(enrollment);

        auditService.logSecurityEvent(
            "ENROLLMENT_CREATED",
            userId,
            "ENROLLMENT",
            event.getName(),
            event.getTenantId(),
            "ENROLLMENT",
            saved.getId(),
            "SUCCESS",
            null,
            "{\"eventId\":\"" + event.getId() + "\"}"
        );

        return mapToResponse(saved, event, participant);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getUserEnrollments(String userId) {
        ParticipantJpaEntity participant = participantRepository.findByIdentityUserId(userId)
            .orElse(null);

        if (participant == null) {
            return List.of();
        }

        return enrollmentRepository.findAllByParticipantId(participant.getId())
            .stream()
            .map(e -> {
                EventJpaEntity event = eventRepository.findById(e.getEventId()).orElse(null);
                return mapToResponse(e, event, participant);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEventEnrollments(String eventId, String tenantId) {
        EventJpaEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + eventId));

        if (!event.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso denegado: El evento pertenece a otra organización.");
        }

        return enrollmentRepository.findAllByEventId(eventId)
            .stream()
            .map(e -> mapToResponse(e, event, e.getParticipant()))
            .collect(Collectors.toList());
    }

    private EnrollmentResponse mapToResponse(EnrollmentJpaEntity entity, EventJpaEntity event, ParticipantJpaEntity participant) {
        String pName = participant != null ? participant.getFullName() : "Participante";
        String pEmail = participant != null ? participant.getEmail() : "";
        String pId = participant != null ? participant.getId() : "";

        return new EnrollmentResponse(
            entity.getId(),
            event != null ? event.getId() : entity.getEventId(),
            event != null ? event.getName() : "Evento",
            event != null ? event.getEventType() : "COURSE",
            event != null ? event.getMode() : "IN_PERSON",
            entity.getTenantId(),
            pId,
            pName.trim(),
            pEmail,
            entity.getStatus(),
            entity.getPaymentStatus(),
            entity.getCreatedAt()
        );
    }
}
