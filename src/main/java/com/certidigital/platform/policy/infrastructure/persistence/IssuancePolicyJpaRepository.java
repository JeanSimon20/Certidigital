package com.certidigital.platform.policy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IssuancePolicyJpaRepository extends JpaRepository<IssuancePolicyJpaEntity, String> {

    List<IssuancePolicyJpaEntity> findAllByTenantId(String tenantId);

    Optional<IssuancePolicyJpaEntity> findByIdAndTenantId(String id, String tenantId);
}
