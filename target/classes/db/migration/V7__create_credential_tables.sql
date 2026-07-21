-- ============================================================
-- V7__create_credential_tables.sql
-- CertiDigital — Módulo: Credential (Core Domain)
-- Bounded Context: Credential
-- Aggregates: Credential (con IntegrityProof, RevocationRecord),
--             CredentialLineage
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   credentials         → tiene tenant_id
--   revocation_records  → tiene tenant_id (denormalizado)
--   credential_lineages → tiene tenant_id (denormalizado)
-- ============================================================

-- ============================================================
-- TABLA: credentials
-- Aggregate Root: Credential
-- La entidad más importante del sistema.
-- INVARIANTE PRINCIPAL: Una Credential emitida (ACTIVE) es
-- INMUTABLE en su contenido. Solo su status puede cambiar.
-- ============================================================
CREATE TABLE credentials (
    id                  VARCHAR(36)     NOT NULL,       -- CredentialId: UUID globalmente único, NUNCA reutilizable

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Tipo y clasificación
    -- CERTIFICATE | DIPLOMA | BADGE | TITLE | CONSTANCIA | VERIFIABLE_CREDENTIAL
    credential_type     VARCHAR(50)     NOT NULL DEFAULT 'CERTIFICATE',

    -- Identificador público (código legible, ej: CERT-2024-00001)
    public_code         VARCHAR(100),

    -- ============================================================
    -- SUJETO (Participant) — INMUTABLE post-emisión
    -- Snapshot de los datos del participante en el momento de emisión
    -- ============================================================
    participant_id      VARCHAR(36)     NOT NULL,       -- FK → participants.id
    participant_name    VARCHAR(500)    NOT NULL,       -- Snapshot del nombre
    participant_email   VARCHAR(255)    NOT NULL,       -- Snapshot del email
    participant_doc     VARCHAR(200),                   -- Snapshot del documento

    -- ============================================================
    -- EMISOR (Tenant/Organization) — INMUTABLE post-emisión
    -- ============================================================
    issuer_tenant_id    VARCHAR(36)     NOT NULL,       -- Redundante con tenant_id, para claridad semántica
    issuer_name         VARCHAR(500)    NOT NULL,       -- Snapshot del nombre institucional
    issuer_country      VARCHAR(3)      NOT NULL,

    -- ============================================================
    -- CONTEXTO DE EMISIÓN — INMUTABLE post-emisión
    -- ============================================================
    event_id            VARCHAR(36)     NOT NULL,
    event_name          VARCHAR(500)    NOT NULL,       -- Snapshot
    policy_id           VARCHAR(36)     NOT NULL,
    issuance_request_id VARCHAR(36)     NOT NULL,
    template_id         VARCHAR(36),

    -- ============================================================
    -- ATRIBUTOS DE LA CREDENCIAL (INMUTABLES)
    -- JSON con los datos específicos del tipo de credencial
    -- ============================================================
    attributes          TEXT,                           -- JSON Schema validated

    -- ============================================================
    -- FECHAS (INMUTABLES)
    -- ============================================================
    issued_at           TIMESTAMP       NOT NULL,
    expires_at          TIMESTAMP,                      -- null = sin vencimiento

    -- ============================================================
    -- ESTADO (ÚNICO CAMPO MUTABLE de una Credential activa)
    -- ACTIVE | SUSPENDED | REVOKED | EXPIRED | SUPERSEDED
    -- ============================================================
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- ============================================================
    -- INTEGRIDAD — IntegrityProof (Value Object embebido)
    -- ============================================================
    -- Firma digital
    signature_value     TEXT,                           -- Firma digital (Base64)
    signing_algorithm   VARCHAR(100),                   -- RSA-SHA256, ECDSA-SHA256
    signed_at           TIMESTAMP,
    digital_cert_id     VARCHAR(36),                    -- FK → digital_certificates.id

    -- Hash del contenido canónico
    content_hash        VARCHAR(64),                    -- SHA-256 hex (64 chars)

    -- Registro en Blockchain
    blockchain_network  VARCHAR(100),                   -- SIMULATOR | ETHEREUM | POLYGON
    blockchain_tx_id    VARCHAR(255),                   -- TX ID en la blockchain
    blockchain_registered_at TIMESTAMP,

    -- ============================================================
    -- PUNTO DE ACCESO
    -- ============================================================
    -- URL permanente para verificación pública
    verification_url    VARCHAR(1000),
    -- QR code como imagen almacenada o datos del QR
    qr_code_url         VARCHAR(1000),

    -- URL del PDF generado
    document_url        VARCHAR(1000),

    -- ============================================================
    -- LINAJE (para renovaciones y rectificaciones)
    -- ============================================================
    predecessor_credential_id VARCHAR(36),             -- FK → credentials.id (self-ref)
    lineage_id          VARCHAR(36),                    -- FK → credential_lineages.id

    -- ============================================================
    -- AUDITORÍA
    -- ============================================================
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_credentials PRIMARY KEY (id),
    CONSTRAINT fk_credentials_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_credentials_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id),
    CONSTRAINT fk_credentials_event
        FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_credentials_policy
        FOREIGN KEY (policy_id) REFERENCES issuance_policies (id),
    CONSTRAINT fk_credentials_request
        FOREIGN KEY (issuance_request_id) REFERENCES issuance_requests (id),
    CONSTRAINT fk_credentials_template
        FOREIGN KEY (template_id) REFERENCES credential_templates (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_credentials_digital_cert
        FOREIGN KEY (digital_cert_id) REFERENCES digital_certificates (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_credentials_predecessor
        FOREIGN KEY (predecessor_credential_id) REFERENCES credentials (id),
    CONSTRAINT uq_credentials_public_code UNIQUE (tenant_id, public_code)
);

COMMENT ON TABLE  credentials IS 'Credentials emitidas. La entidad más importante del sistema. Contenido INMUTABLE post-emisión.';
COMMENT ON COLUMN credentials.id IS 'UUID globalmente único. NUNCA reutilizable, incluso si se revoca.';
COMMENT ON COLUMN credentials.status IS 'ACTIVE | SUSPENDED | REVOKED | EXPIRED | SUPERSEDED';
COMMENT ON COLUMN credentials.content_hash IS 'SHA-256 del contenido canónico JSON. Garantiza integridad.';

CREATE INDEX idx_credentials_tenant_id      ON credentials (tenant_id);
CREATE INDEX idx_credentials_participant    ON credentials (participant_id);
CREATE INDEX idx_credentials_event_id      ON credentials (event_id);
CREATE INDEX idx_credentials_status        ON credentials (tenant_id, status);
CREATE INDEX idx_credentials_content_hash  ON credentials (content_hash);
CREATE INDEX idx_credentials_public_code   ON credentials (public_code);
CREATE INDEX idx_credentials_issued_at     ON credentials (issued_at);


-- ============================================================
-- TABLA: revocation_records
-- Entidad interna del Aggregate Credential.
-- Registro INMUTABLE de la revocación de una Credential.
-- Solo existe si la Credential fue revocada.
-- ============================================================
CREATE TABLE revocation_records (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia a la Credential revocada
    credential_id       VARCHAR(36)     NOT NULL,

    -- Razón de la revocación (CATEGORÍA)
    revocation_reason   VARCHAR(100)    NOT NULL,       -- ERROR_IN_ISSUANCE | FRAUD | DISCIPLINARY | EXPIRED_CERT | OTHER

    -- Descripción detallada
    revocation_notes    TEXT,

    -- Quién y cuándo
    revoked_by          VARCHAR(36)     NOT NULL,       -- userId del Admin que revocó
    revoked_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_revocation_records PRIMARY KEY (id),
    CONSTRAINT fk_revocation_credential
        FOREIGN KEY (credential_id) REFERENCES credentials (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_revocation_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    -- Solo puede haber un revocation_record por Credential
    CONSTRAINT uq_revocation_credential UNIQUE (credential_id)
);

COMMENT ON TABLE  revocation_records IS 'Registro inmutable de revocación. Solo una por Credential. Estado terminal.';
COMMENT ON COLUMN revocation_records.revocation_reason IS 'ERROR_IN_ISSUANCE | FRAUD | DISCIPLINARY | EXPIRED_CERT | DUPLICATE | OTHER';

CREATE INDEX idx_revocation_credential ON revocation_records (credential_id);
CREATE INDEX idx_revocation_tenant     ON revocation_records (tenant_id);


-- ============================================================
-- TABLA: credential_lineages
-- Aggregate Root: CredentialLineage
-- Rastrea el historial de versiones de una Credential.
-- Permite que renovaciones y rectificaciones mantengan
-- trazabilidad histórica.
-- ============================================================
CREATE TABLE credential_lineages (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia a la Credential original (raíz del linaje)
    original_credential_id VARCHAR(36)  NOT NULL,

    -- Descripción del linaje
    notes               TEXT,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_credential_lineages PRIMARY KEY (id),
    CONSTRAINT fk_lineage_original
        FOREIGN KEY (original_credential_id) REFERENCES credentials (id),
    CONSTRAINT fk_lineage_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

COMMENT ON TABLE credential_lineages IS 'Historial de versiones de Credentials (renovaciones, rectificaciones).';

CREATE INDEX idx_lineage_original ON credential_lineages (original_credential_id);
CREATE INDEX idx_lineage_tenant   ON credential_lineages (tenant_id);

-- Ahora que credential_lineages existe, podemos agregar la FK desde credentials
ALTER TABLE credentials
    ADD CONSTRAINT fk_credentials_lineage
        FOREIGN KEY (lineage_id) REFERENCES credential_lineages (id)
            ON DELETE SET NULL;


-- ============================================================
-- TABLA: verification_requests
-- Aggregate Root: VerificationRequest
-- Registra cada consulta de verificación pública.
-- ============================================================
CREATE TABLE verification_requests (
    id                  VARCHAR(36)     NOT NULL,

    -- Referencia a la Credential verificada
    credential_id       VARCHAR(36)     NOT NULL,

    -- Método de verificación: QR | URL | CODE | HASH | API
    verification_method VARCHAR(30)     NOT NULL DEFAULT 'QR',

    -- Resultado: VALID | REVOKED | EXPIRED | SUSPENDED | NOT_FOUND | INTEGRITY_FAILURE
    result              VARCHAR(30)     NOT NULL,

    -- Datos del verificador (opcionales — si está autenticado)
    requestor_ip        VARCHAR(50),
    requestor_user_agent VARCHAR(500),

    -- Timestamp de la verificación
    verified_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_verification_requests PRIMARY KEY (id),
    CONSTRAINT fk_verification_credential
        FOREIGN KEY (credential_id) REFERENCES credentials (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  verification_requests IS 'Registro de verificaciones públicas de Credentials.';
COMMENT ON COLUMN verification_requests.result IS 'VALID | REVOKED | EXPIRED | SUSPENDED | NOT_FOUND | INTEGRITY_FAILURE';

CREATE INDEX idx_verification_credential ON verification_requests (credential_id);
CREATE INDEX idx_verification_method     ON verification_requests (verification_method);
CREATE INDEX idx_verification_at         ON verification_requests (verified_at);
