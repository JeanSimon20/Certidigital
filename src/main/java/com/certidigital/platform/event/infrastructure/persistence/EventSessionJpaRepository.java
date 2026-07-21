package com.certidigital.platform.event.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventSessionJpaRepository extends JpaRepository<EventSessionJpaEntity, String> {

    List<EventSessionJpaEntity> findAllByEventId(String eventId);

    List<EventSessionJpaEntity> findAllByTenantId(String tenantId);
}
