package com.certidigital.platform.event.presentation;

import com.certidigital.platform.event.application.dto.CreateEventRequest;
import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
import com.certidigital.platform.participation.application.dto.EnrollmentResponse;
import com.certidigital.platform.participation.application.service.EnrollmentApplicationService;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventApplicationService eventService;
    private final EnrollmentApplicationService enrollmentService;

    public EventController(EventApplicationService eventService, EnrollmentApplicationService enrollmentService) {
        this.eventService = eventService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getTenantEvents() {
        String tenantId = TenantContextHolder.requireTenantId();
        List<EventResponse> events = eventService.getTenantEvents(tenantId);
        return ResponseEntity.ok(ApiResponse.success(events, "Eventos de la organización obtenidos"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
        @RequestBody CreateEventRequest request,
        Authentication authentication
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        String userId = authentication.getName();
        EventResponse created = eventService.createEvent(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Evento creado exitosamente en borrador"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
        @PathVariable String id,
        @RequestBody CreateEventRequest request
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        EventResponse updated = eventService.updateEvent(id, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Evento actualizado exitosamente"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(
        @PathVariable String id,
        Authentication authentication
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        String userId = authentication.getName();
        EventResponse published = eventService.publishEvent(id, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(published, "Evento publicado exitosamente en el catálogo"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(
        @PathVariable String id,
        Authentication authentication
    ) {
        String tenantId = TenantContextHolder.requireTenantId();
        String userId = authentication.getName();
        EventResponse cancelled = eventService.cancelEvent(id, tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Evento cancelado"));
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEventEnrollments(@PathVariable String id) {
        String tenantId = TenantContextHolder.requireTenantId();
        List<EnrollmentResponse> enrollments = enrollmentService.getEventEnrollments(id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(enrollments, "Lista de inscritos del evento obtenida"));
    }
}
