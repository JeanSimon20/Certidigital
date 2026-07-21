package com.certidigital.platform.issuance.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "blockchain_records",
    indexes = {
        @Index(name = "idx_blockchain_tenant",     columnList = "tenant_id"),
        @Index(name = "idx_blockchain_credential", columnList = "credential_id"),
        @Index(name = "idx_blockchain_hash",       columnList = "content_hash"),
        @Index(name = "idx_blockchain_network",    columnList = "network")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_blockchain_hash_network", columnNames = {"content_hash", "network"}),
        @UniqueConstraint(name = "uq_blockchain_txid_network", columnNames = {"tx_id", "network"})
    }
)
public class BlockchainRecordJpaEntity {

    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "tenant_id", length = 36, nullable = false)
    private String tenantId;

    @Column(name = "credential_id", length = 36, nullable = false)
    private String credentialId;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "tx_id", length = 255, nullable = false)
    private String txId;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber = 0L;

    @Column(name = "network", length = 100, nullable = false)
    private String network = "SIMULATOR";

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    protected void onPrePersist() {
        if (this.registeredAt == null) this.registeredAt = LocalDateTime.now();
    }

    public BlockchainRecordJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }

    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }

    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
