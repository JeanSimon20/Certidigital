package com.certidigital.platform.policy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssuanceRequestJpaRepository extends JpaRepository<IssuanceRequestJpaEntity, String> {
    Optional<IssuanceRequestJpaEntity> findByEnrollmentIdAndTenantId(String enrollmentId, String tenantId);
}
