package com.certidigital.platform.credential.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RevocationRecordJpaRepository extends JpaRepository<RevocationRecordJpaEntity, String> {
    Optional<RevocationRecordJpaEntity> findByCredentialIdAndTenantId(String credentialId, String tenantId);
    Optional<RevocationRecordJpaEntity> findByCredentialId(String credentialId);
}
