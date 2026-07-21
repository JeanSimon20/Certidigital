-- ============================================================
-- V8__create_blockchain_tables.sql
-- CertiDigital — Módulo: Blockchain Simulator
-- Infraestructura de issuance
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   blockchain_records → tiene tenant_id
--     Cada registro está asociado a un Tenant (para auditoría)
--     La tabla es APPEND-ONLY (no UPDATE, no DELETE)
-- ============================================================

-- ============================================================
-- TABLA: blockchain_records
-- Registro INMUTABLE del hash de una Credential
-- en la blockchain (o en el simulador).
--
-- DISEÑO: Esta tabla simula una blockchain local.
-- - Solo INSERT está permitido.
-- - No existe UPDATE ni DELETE.
-- - Garantiza inmutabilidad a nivel de aplicación.
-- - La columna 'network' permite distinguir simulador de real.
-- ============================================================
CREATE TABLE blockchain_records (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia a la Credential cuyo hash fue registrado
    credential_id       VARCHAR(36)     NOT NULL,

    -- El hash SHA-256 del contenido canónico de la Credential
    content_hash        VARCHAR(64)     NOT NULL,

    -- Identificador de transacción en la blockchain/simulador
    tx_id               VARCHAR(255)    NOT NULL,

    -- Número de bloque (simulado o real)
    block_number        BIGINT          NOT NULL DEFAULT 0,

    -- Red donde se registró: SIMULATOR | ETHEREUM | POLYGON | HYPERLEDGER
    network             VARCHAR(100)    NOT NULL DEFAULT 'SIMULATOR',

    -- Momento exacto de registro
    registered_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Datos adicionales del registro (metadata de la blockchain)
    metadata            TEXT,                           -- JSON (gas used, confirmations, etc.)

    CONSTRAINT pk_blockchain_records PRIMARY KEY (id),
    CONSTRAINT fk_blockchain_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_blockchain_credential
        FOREIGN KEY (credential_id) REFERENCES credentials (id),
    -- El mismo hash no puede registrarse dos veces en la misma red
    CONSTRAINT uq_blockchain_hash_network UNIQUE (content_hash, network),
    -- El mismo tx_id no puede repetirse en la misma red
    CONSTRAINT uq_blockchain_txid_network UNIQUE (tx_id, network)
);

COMMENT ON TABLE  blockchain_records IS 'Registro inmutable de hashes de Credentials en blockchain. APPEND-ONLY. No UPDATE, no DELETE.';
COMMENT ON COLUMN blockchain_records.network IS 'SIMULATOR | ETHEREUM | POLYGON | HYPERLEDGER | ALGORAND';
COMMENT ON COLUMN blockchain_records.tx_id IS 'Transaction ID en la red. UUID para simulador, hash de TX para blockchain real.';

CREATE INDEX idx_blockchain_tenant      ON blockchain_records (tenant_id);
CREATE INDEX idx_blockchain_credential  ON blockchain_records (credential_id);
CREATE INDEX idx_blockchain_hash        ON blockchain_records (content_hash);
CREATE INDEX idx_blockchain_network     ON blockchain_records (network);


-- ============================================================
-- TABLA: notification_jobs
-- Módulo: Notification
-- Trabajos de notificación pendientes/procesados.
-- ============================================================
CREATE TABLE notification_jobs (
    id                  VARCHAR(36)     NOT NULL,

    -- Tipo de evento que disparó la notificación
    -- CREDENTIAL_ISSUED | CREDENTIAL_REVOKED | ELIGIBILITY_MET | etc.
    event_type          VARCHAR(100)    NOT NULL,

    -- Canal de envío: EMAIL | SMS | WHATSAPP
    channel             VARCHAR(30)     NOT NULL DEFAULT 'EMAIL',

    -- Destinatario
    recipient_email     VARCHAR(255),
    recipient_name      VARCHAR(255),

    -- Contenido
    subject             VARCHAR(500),
    body                TEXT,

    -- Referencia al contexto que disparó la notificación
    reference_type      VARCHAR(100),                   -- CREDENTIAL, ISSUANCE_REQUEST, etc.
    reference_id        VARCHAR(36),

    -- Estado: PENDING | PROCESSING | SENT | FAILED
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- Control de errores
    retry_count         INTEGER         NOT NULL DEFAULT 0,
    max_retries         INTEGER         NOT NULL DEFAULT 3,
    last_error          TEXT,

    -- Timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        TIMESTAMP,
    sent_at             TIMESTAMP,

    CONSTRAINT pk_notification_jobs PRIMARY KEY (id)
);

COMMENT ON TABLE  notification_jobs IS 'Cola de notificaciones pendientes. Procesadas asíncronamente.';
COMMENT ON COLUMN notification_jobs.status IS 'PENDING | PROCESSING | SENT | FAILED';

CREATE INDEX idx_notification_status  ON notification_jobs (status);
CREATE INDEX idx_notification_created ON notification_jobs (created_at);
