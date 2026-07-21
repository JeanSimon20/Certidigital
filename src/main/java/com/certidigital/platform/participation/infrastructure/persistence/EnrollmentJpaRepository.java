package com.certidigital.platform.participation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, String> {

    List<EnrollmentJpaEntity> findAllByParticipantId(String participantId);

    List<EnrollmentJpaEntity> findAllByParticipantIdAndTenantId(String participantId, String tenantId);

    List<EnrollmentJpaEntity> findAllByEventId(String eventId);

    boolean existsByEventIdAndParticipantId(String eventId, String participantId);

    Optional<EnrollmentJpaEntity> findByEventIdAndParticipantId(String eventId, String participantId);
}
