package com.certidigital.platform.iam.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.memberships m WHERE u.id = :userId")
    Optional<UserJpaEntity> findByIdWithMemberships(@Param("userId") String userId);
}
