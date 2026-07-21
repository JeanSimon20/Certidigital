package com.certidigital.platform.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, String> {

    List<NotificationJpaEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<NotificationJpaEntity> findAllByTenantIdOrderByCreatedAtDesc(String tenantId);
}
