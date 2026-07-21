package com.certidigital.platform.credential.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialJpaRepository extends JpaRepository<CredentialJpaEntity, String> {

    List<CredentialJpaEntity> findAllByTenantId(String tenantId);

    List<CredentialJpaEntity> findAllByParticipantIdAndTenantId(String participantId, String tenantId);

    Optional<CredentialJpaEntity> findByIdAndTenantId(String id, String tenantId);

    Optional<CredentialJpaEntity> findByPublicCode(String publicCode);

    Optional<CredentialJpaEntity> findByContentHash(String contentHash);

    List<CredentialJpaEntity> findByIssuanceRequestIdAndTenantIdAndStatus(String issuanceRequestId, String tenantId, String status);

    Optional<CredentialJpaEntity> findFirstByParticipantIdAndEventIdAndTenantIdAndStatus(
            String participantId, String eventId, String tenantId, String status
    );
}
