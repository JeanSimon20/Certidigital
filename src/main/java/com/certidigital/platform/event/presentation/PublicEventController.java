package com.certidigital.platform.event.presentation;

import com.certidigital.platform.event.application.dto.EventResponse;
import com.certidigital.platform.event.application.service.EventApplicationService;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/public")
public class PublicEventController {

    private final EventApplicationService eventService;

    public PublicEventController(EventApplicationService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getPublicEvents(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String eventType,
        @RequestParam(required = false) String mode
    ) {
        List<EventResponse> events = eventService.getPublicEvents(query, eventType, mode);
        return ResponseEntity.ok(ApiResponse.success(events, "Catálogo de eventos cargado exitosamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getPublicEventDetail(@PathVariable String id) {
        EventResponse event = eventService.getPublicEventDetail(id);
        return ResponseEntity.ok(ApiResponse.success(event, "Detalle de evento obtenido"));
    }
}
