package com.certidigital.platform.organization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationProfileJpaRepository extends JpaRepository<OrganizationProfileJpaEntity, String> {
    Optional<OrganizationProfileJpaEntity> findByTenantId(String tenantId);
}
