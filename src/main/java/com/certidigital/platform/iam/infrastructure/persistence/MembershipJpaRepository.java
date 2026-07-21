package com.certidigital.platform.iam.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, String> {

    List<MembershipJpaEntity> findByUserIdAndStatus(String userId, String status);

    List<MembershipJpaEntity> findByUserId(String userId);

    @Query("SELECT m FROM MembershipJpaEntity m LEFT JOIN FETCH m.roles r LEFT JOIN FETCH r.permissions WHERE m.user.id = :userId AND m.tenantId = :tenantId AND m.status = 'ACTIVE'")
    Optional<MembershipJpaEntity> findActiveMembershipWithRolesAndPermissions(
        @Param("userId") String userId,
        @Param("tenantId") String tenantId
    );

    boolean existsByUserIdAndTenantIdAndStatus(String userId, String tenantId, String status);
}
