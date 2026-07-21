package com.certidigital.platform.issuance.domain.port;

import java.time.LocalDateTime;

public interface BlockchainPort {

    record BlockchainAnchorResult(
            String txId,
            Long blockNumber,
            String network,
            String contentHash,
            LocalDateTime registeredAt,
            String metadataJson
    ) {}

    BlockchainAnchorResult anchorCredentialHash(String tenantId, String credentialId, String contentHash);
}
