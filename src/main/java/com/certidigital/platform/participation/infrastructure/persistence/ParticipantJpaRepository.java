package com.certidigital.platform.participation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantJpaRepository extends JpaRepository<ParticipantJpaEntity, String> {

    Optional<ParticipantJpaEntity> findByIdentityUserId(String identityUserId);

    Optional<ParticipantJpaEntity> findByEmail(String email);
}
