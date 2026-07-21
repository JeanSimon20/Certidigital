package com.certidigital.platform.issuance.infrastructure.adapter;

import com.certidigital.platform.issuance.domain.port.BlockchainPort;
import com.certidigital.platform.issuance.infrastructure.persistence.BlockchainRecordJpaEntity;
import com.certidigital.platform.issuance.infrastructure.persistence.BlockchainRecordJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class BlockchainSimulatorAdapter implements BlockchainPort {

    private final BlockchainRecordJpaRepository repository;

    public BlockchainSimulatorAdapter(BlockchainRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public BlockchainAnchorResult anchorCredentialHash(String tenantId, String credentialId, String contentHash) {
        String txId = "0x" + UUID.randomUUID().toString().replace("-", "") + Long.toHexString(System.currentTimeMillis());
        long blockNumber = 10_000_000L + (long) (Math.random() * 500_000);
        String network = "SIMULATOR";
        LocalDateTime now = LocalDateTime.now();
        String metadataJson = "{\"simulated\":true,\"gasUsed\":21000,\"consensus\":\"ProofOfAuthority\"}";

        BlockchainRecordJpaEntity entity = new BlockchainRecordJpaEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantId(tenantId);
        entity.setCredentialId(credentialId);
        entity.setContentHash(contentHash);
        entity.setTxId(txId);
        entity.setBlockNumber(blockNumber);
        entity.setNetwork(network);
        entity.setRegisteredAt(now);
        entity.setMetadata(metadataJson);

        repository.save(entity);

        return new BlockchainAnchorResult(txId, blockNumber, network, contentHash, now, metadataJson);
    }
}
