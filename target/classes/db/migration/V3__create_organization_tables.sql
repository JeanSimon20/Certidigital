-- ============================================================
-- V3__create_organization_tables.sql
-- CertiDigital — Módulo: Organization Configuration
-- Bounded Context: Organization
-- Aggregates: OrganizationProfile, DigitalCertificate, CredentialTemplate
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   organization_profiles   → tiene tenant_id (1:1 con tenant)
--   digital_certificates    → tiene tenant_id
--   credential_templates    → tiene tenant_id
-- ============================================================

-- ============================================================
-- TABLA: organization_profiles
-- Aggregate Root: OrganizationProfile
-- Identidad visual e institucional del Tenant.
-- Relación 1:1 con tenants.
-- ============================================================
CREATE TABLE organization_profiles (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO: Cada perfil pertenece a exactamente un Tenant
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Identidad institucional
    legal_name          VARCHAR(255)    NOT NULL,
    commercial_name     VARCHAR(255),
    description         TEXT,
    website             VARCHAR(500),
    logo_url            VARCHAR(1000),

    -- Branding
    primary_color       VARCHAR(7),                    -- HEX color: #RRGGBB
    secondary_color     VARCHAR(7),
    font_family         VARCHAR(100),

    -- Datos de contacto institucional
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(50),
    contact_address     TEXT,

    -- Jurisdicción
    country_code        VARCHAR(3)      NOT NULL,
    state_province      VARCHAR(100),
    city                VARCHAR(100),

    -- Estado del perfil: DRAFT | COMPLETE
    -- Un Tenant no puede emitir credenciales si está en DRAFT
    profile_status      VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(36),

    CONSTRAINT pk_org_profiles PRIMARY KEY (id),
    CONSTRAINT fk_org_profiles_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    -- Un Tenant solo puede tener un OrganizationProfile
    CONSTRAINT uq_org_profiles_tenant UNIQUE (tenant_id)
);

COMMENT ON TABLE  organization_profiles IS 'Perfil institucional del Tenant. Relación 1:1 con tenants.';
COMMENT ON COLUMN organization_profiles.profile_status IS 'DRAFT | COMPLETE. Solo COMPLETE permite emitir credenciales.';

CREATE INDEX idx_org_profiles_tenant_id ON organization_profiles (tenant_id);


-- ============================================================
-- TABLA: digital_certificates
-- Aggregate Root: DigitalCertificate
-- Certificado criptográfico usado para firmar Credentials.
-- Solo un certificado ACTIVE por Tenant en un momento dado.
-- ============================================================
CREATE TABLE digital_certificates (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Información del certificado
    subject_name        VARCHAR(500)    NOT NULL,       -- CN del certificado
    issuer_name         VARCHAR(500),                   -- CA que lo emitió
    serial_number       VARCHAR(255),
    fingerprint         VARCHAR(255)    NOT NULL,       -- SHA-256 fingerprint del cert

    -- Para MVP: almacenamos referencia al archivo del keystore
    -- En producción: integrar con HSM o PKI externa
    keystore_alias      VARCHAR(100),
    keystore_path       VARCHAR(1000),                  -- Ruta al archivo keystore (dev only)

    -- Vigencia
    valid_from          TIMESTAMP       NOT NULL,
    valid_to            TIMESTAMP       NOT NULL,

    -- Estado: ACTIVE | EXPIRING | EXPIRED | REVOKED
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_digital_certificates PRIMARY KEY (id),
    CONSTRAINT fk_digital_certs_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  digital_certificates IS 'Certificados criptográficos para firma de Credentials. Solo uno ACTIVE por Tenant.';
COMMENT ON COLUMN digital_certificates.status IS 'ACTIVE | EXPIRING | EXPIRED | REVOKED';

CREATE INDEX idx_digital_certs_tenant_id ON digital_certificates (tenant_id);
CREATE INDEX idx_digital_certs_status    ON digital_certificates (tenant_id, status);


-- ============================================================
-- TABLA: credential_templates
-- Aggregate Root: CredentialTemplate
-- Define la estructura visual y esquema de atributos de
-- una Credential para un Tenant específico.
-- ============================================================
CREATE TABLE credential_templates (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Identidad de la plantilla
    name                VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),

    -- Tipo de credencial que usa esta plantilla
    -- CERTIFICATE | DIPLOMA | BADGE | TITLE | CONSTANCIA | VERIFIABLE_CREDENTIAL
    credential_type     VARCHAR(50)     NOT NULL DEFAULT 'CERTIFICATE',

    -- Diseño visual: HTML/CSS template con placeholders
    -- Almacenado como texto para MVP; en producción: referencia a archivo
    visual_layout       TEXT,

    -- Esquema de atributos en formato JSON Schema
    -- Define qué campos son requeridos y opcionales
    attribute_schema    TEXT,                           -- JSON

    -- Estado: DRAFT | ACTIVE | DEPRECATED
    status              VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_credential_templates PRIMARY KEY (id),
    CONSTRAINT fk_credential_templates_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  credential_templates IS 'Plantillas visuales y esquemas de atributos para Credentials por Tenant.';
COMMENT ON COLUMN credential_templates.credential_type IS 'CERTIFICATE | DIPLOMA | BADGE | TITLE | CONSTANCIA | VERIFIABLE_CREDENTIAL';
COMMENT ON COLUMN credential_templates.status IS 'DRAFT | ACTIVE | DEPRECATED. No puede eliminarse si existen Credentials emitidas.';

CREATE INDEX idx_cred_templates_tenant_id ON credential_templates (tenant_id);
CREATE INDEX idx_cred_templates_status    ON credential_templates (tenant_id, status);
