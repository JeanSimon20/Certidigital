package com.certidigital.platform.tenant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, String> {

    Optional<TenantJpaEntity> findByIdAndStatus(String id, String status);
}
