package com.certidigital.platform.event.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, String> {

    List<EventJpaEntity> findAllByTenantId(String tenantId);

    List<EventJpaEntity> findAllByStatus(String status);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.status = 'PUBLISHED' " +
           "AND (:query IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:eventType IS NULL OR e.eventType = :eventType) " +
           "AND (:mode IS NULL OR e.mode = :mode)")
    List<EventJpaEntity> searchPublicEvents(
        @Param("query") String query,
        @Param("eventType") String eventType,
        @Param("mode") String mode
    );
}
