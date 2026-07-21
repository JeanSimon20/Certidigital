package com.certidigital.platform.event.application.service;

import com.certidigital.platform.audit.application.service.SecurityAuditService;
import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaEntity;
import com.certidigital.platform.event.infrastructure.persistence.EventJpaRepository;
import com.certidigital.platform.participation.infrastructure.persistence.EnrollmentJpaRepository;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaEntity;
import com.certidigital.platform.tenant.infrastructure.persistence.TenantJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventApplicationService {

    private final EventJpaRepository eventRepository;
    private final EnrollmentJpaRepository enrollmentRepository;
    private final TenantJpaRepository tenantRepository;
    private final SecurityAuditService auditService;

    public EventApplicationService(
        EventJpaRepository eventRepository,
        EnrollmentJpaRepository enrollmentRepository,
        TenantJpaRepository tenantRepository,
        SecurityAuditService auditService
    ) {
        this.eventRepository = eventRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getPublicEvents(String query, String eventType, String mode) {
        List<EventJpaEntity> events = eventRepository.searchPublicEvents(
            (query == null || query.isBlank()) ? null : query.trim(),
            (eventType == null || eventType.isBlank() || "ALL".equalsIgnoreCase(eventType)) ? null : eventType,
            (mode == null || mode.isBlank() || "ALL".equalsIgnoreCase(mode)) ? null : mode
        );

        return events.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getPublicEventDetail(String eventId) {
        EventJpaEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + eventId));
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getTenantEvents(String tenantId) {
        return eventRepository.findAllByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String tenantId, String userId) {
        EventJpaEntity event = new EventJpaEntity();
        event.setId(UUID.randomUUID().toString());
        event.setTenantId(tenantId);
        event.setName(request.getName().trim());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setMode(request.getMode());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
        event.setLocationName(request.getLocationName());
        event.setLocationAddress(request.getLocationAddress());
        event.setVirtualUrl(request.getVirtualUrl());
        event.setMaxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 50);
        event.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        event.setOrganizerUserId(userId);
        event.setStatus("DRAFT");
        event.setCreatedBy(userId);

        EventJpaEntity saved = eventRepository.save(event);

        auditService.logSecurityEvent(
            "EVENT_CREATED",
            userId,
            "EVENT",
            saved.getName(),
            tenantId,
            "EVENT",
            saved.getId(),
            "SUCCESS",
            null,
            "{\"name\":\"" + saved.getName() + "\"}"
        );

        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(String eventId, CreateEventRequest request, String tenantId) {
        EventJpaEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + eventId));

        if (!event.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("No tiene permisos para modificar eventos de otra organización.");
        }

        event.setName(request.getName().trim());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setMode(request.getMode());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocationName(request.getLocationName());
        event.setLocationAddress(request.getLocationAddress());
        event.setVirtualUrl(request.getVirtualUrl());
        event.setMaxCapacity(request.getMaxCapacity());
        if (request.getPrice() != null) {
            event.setPrice(request.getPrice());
        }

        EventJpaEntity saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse publishEvent(String eventId, String tenantId, String userId) {
        EventJpaEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + eventId));

        if (!event.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso denegado: El evento no pertenece a la organización activa.");
        }

        event.setStatus("PUBLISHED");
        EventJpaEntity saved = eventRepository.save(event);

        auditService.logSecurityEvent(
            "EVENT_PUBLISHED",
            userId,
            "EVENT",
            saved.getName(),
            tenantId,
            "EVENT",
            saved.getId(),
            "SUCCESS",
            null,
            null
        );

        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse cancelEvent(String eventId, String tenantId, String userId) {
        EventJpaEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + eventId));

        if (!event.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Acceso denegado.");
        }

        event.setStatus("CANCELLED");
        EventJpaEntity saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    private EventResponse mapToResponse(EventJpaEntity entity) {
        String tenantName = tenantRepository.findById(entity.getTenantId())
            .map(TenantJpaEntity::getCommercialName)
            .orElse("Organización");

        int enrolledCount = enrollmentRepository.findAllByEventId(entity.getId()).size();

        Double price = entity.getPrice() != null ? entity.getPrice() : 0.0;
        Boolean isFree = price == 0.0;

        return new EventResponse(
            entity.getId(),
            entity.getTenantId(),
            tenantName,
            entity.getName(),
            entity.getDescription(),
            entity.getEventType(),
            entity.getMode(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getTimezone(),
            entity.getLocationName(),
            entity.getLocationAddress(),
            entity.getVirtualUrl(),
            entity.getMaxCapacity(),
            enrolledCount,
            price,
            isFree,
            entity.getStatus(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
