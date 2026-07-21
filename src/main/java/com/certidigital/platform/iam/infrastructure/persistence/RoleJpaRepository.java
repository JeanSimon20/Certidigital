package com.certidigital.platform.iam.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String> {

    Optional<RoleJpaEntity> findByNameAndTenantId(String name, String tenantId);

    Optional<RoleJpaEntity> findByNameAndIsSystemRoleTrue(String name);
}
