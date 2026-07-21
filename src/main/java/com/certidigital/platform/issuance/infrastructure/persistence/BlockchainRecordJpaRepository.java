package com.certidigital.platform.issuance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainRecordJpaRepository extends JpaRepository<BlockchainRecordJpaEntity, String> {
    Optional<BlockchainRecordJpaEntity> findByCredentialIdAndTenantId(String credentialId, String tenantId);
    Optional<BlockchainRecordJpaEntity> findByContentHashAndNetwork(String contentHash, String network);
}
