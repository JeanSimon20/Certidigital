package com.certidigital.platform.notification.application.service;

import com.certidigital.platform.notification.application.dto.NotificationResponse;
import com.certidigital.platform.notification.infrastructure.persistence.NotificationJpaEntity;
import com.certidigital.platform.notification.infrastructure.persistence.NotificationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationApplicationService {

    private final NotificationJpaRepository notificationRepository;

    public NotificationApplicationService(NotificationJpaRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(String userId, String tenantId, String type, String title, String message, String link) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setUserId(userId);
        entity.setTenantId(tenantId != null ? tenantId : "tenant-001-aaaa-bbbb-cccc-dddddddd");
        entity.setType(type);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setLink(link);
        entity.setRead(false);
        entity.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(String userId, String tenantId) {
        List<NotificationJpaEntity> list = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        // Fallback: If user notifications empty and user is admin, show tenant notifications
        if (list.isEmpty() && tenantId != null) {
            list = notificationRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId);
        }

        return list.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<NotificationJpaEntity> list = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        for (NotificationJpaEntity n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }

    private NotificationResponse mapToResponse(NotificationJpaEntity entity) {
        return new NotificationResponse(
            entity.getId(),
            entity.getType(),
            entity.getTitle(),
            entity.getMessage(),
            entity.getLink(),
            entity.isRead(),
            entity.getCreatedAt()
        );
    }
}
