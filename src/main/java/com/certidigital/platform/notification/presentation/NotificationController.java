package com.certidigital.platform.notification.presentation;

import com.certidigital.platform.notification.application.dto.NotificationResponse;
import com.certidigital.platform.notification.application.service.NotificationApplicationService;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.certidigital.platform.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationApplicationService notificationService;

    public NotificationController(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "user-student-0003-aaaa-bbbb-cccccccc";
        String tenantId = TenantContextHolder.getTenantId();
        List<NotificationResponse> list = notificationService.getUserNotifications(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(list, "Notificaciones del usuario obtenidas"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "user-student-0003-aaaa-bbbb-cccccccc";
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Todas las notificaciones fueron marcadas como leídas"));
    }
}
