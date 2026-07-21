-- ============================================================
-- V1__create_tenant_tables.sql
-- CertiDigital — Módulo: Platform Management
-- Bounded Context: Platform Management
-- Aggregates: Tenant, TenantRegistrationRequest
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   tenants          → NO tiene tenant_id (ES el tenant)
--   tenant_reg_requests → NO tiene tenant_id (pre-creación)
-- ============================================================

-- ============================================================
-- TABLA: tenants
-- Aggregate Root: Tenant
-- Representa la organización emisora de credenciales.
-- Es el pivote central del aislamiento de datos.
-- ============================================================
CREATE TABLE tenants (
    -- Clave primaria: UUID para evitar enumeración
    id                  VARCHAR(36)     NOT NULL,

    -- Identidad legal del tenant
    legal_name          VARCHAR(255)    NOT NULL,
    commercial_name     VARCHAR(255),
    tax_id              VARCHAR(100),                    -- RIF, NIT, RFC, etc.
    sector              VARCHAR(100)    NOT NULL,        -- EDUCATION, GOVERNMENT, CORPORATE, etc.
    country_code        VARCHAR(3)      NOT NULL,        -- ISO 3166-1 alpha-2/3

    -- Estado del ciclo de vida del tenant
    -- PENDING | ACTIVE | SUSPENDED | TERMINATED
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- Información de contacto principal
    contact_name        VARCHAR(255)    NOT NULL,
    contact_email       VARCHAR(255)    NOT NULL,
    contact_phone       VARCHAR(50),

    -- Plan de servicio (referencia simple para MVP)
    service_plan        VARCHAR(50)     NOT NULL DEFAULT 'STANDARD',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),                     -- userId del creador (Super Admin)

    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uq_tenants_contact_email UNIQUE (contact_email)
);

COMMENT ON TABLE  tenants IS 'Organizaciones emisoras de credenciales. Pivote del aislamiento multi-tenant.';
COMMENT ON COLUMN tenants.id IS 'UUID globalmente único. Nunca reutilizable.';
COMMENT ON COLUMN tenants.status IS 'PENDING | ACTIVE | SUSPENDED | TERMINATED';
COMMENT ON COLUMN tenants.sector IS 'Sector de la organización: EDUCATION, GOVERNMENT, CORPORATE, NGO, OTHER';
COMMENT ON COLUMN tenants.service_plan IS 'Plan de servicio contratado: FREE, STANDARD, ENTERPRISE';

-- Índice para búsquedas por estado (listados de Super Admin)
CREATE INDEX idx_tenants_status ON tenants (status);


-- ============================================================
-- TABLA: tenant_registration_requests
-- Aggregate Root: TenantRegistrationRequest
-- Gestiona el proceso de solicitud antes de crear el Tenant.
-- ============================================================
CREATE TABLE tenant_registration_requests (
    id                  VARCHAR(36)     NOT NULL,

    -- Datos del solicitante
    applicant_name      VARCHAR(255)    NOT NULL,
    applicant_email     VARCHAR(255)    NOT NULL,
    applicant_phone     VARCHAR(50),

    -- Datos de la organización solicitante
    org_legal_name      VARCHAR(255)    NOT NULL,
    org_commercial_name VARCHAR(255),
    org_tax_id          VARCHAR(100),
    org_sector          VARCHAR(100)    NOT NULL,
    org_country_code    VARCHAR(3)      NOT NULL,
    org_website         VARCHAR(500),

    -- Descripción del caso de uso
    use_case_description TEXT,

    -- Estado: SUBMITTED | UNDER_REVIEW | APPROVED | REJECTED
    status              VARCHAR(30)     NOT NULL DEFAULT 'SUBMITTED',

    -- Revisión por Super Admin
    reviewed_by         VARCHAR(36),
    reviewed_at         TIMESTAMP,
    review_notes        TEXT,

    -- Si fue aprobada, referencia al Tenant creado
    tenant_id           VARCHAR(36),                     -- FK → tenants.id (null hasta aprobación)

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tenant_reg_requests PRIMARY KEY (id),
    CONSTRAINT fk_reg_request_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE SET NULL
);

COMMENT ON TABLE  tenant_registration_requests IS 'Solicitudes de registro de nuevas organizaciones. Pre-creación de Tenant.';
COMMENT ON COLUMN tenant_registration_requests.status IS 'SUBMITTED | UNDER_REVIEW | APPROVED | REJECTED';

CREATE INDEX idx_tenant_reg_requests_status ON tenant_registration_requests (status);
CREATE INDEX idx_tenant_reg_requests_email  ON tenant_registration_requests (applicant_email);
