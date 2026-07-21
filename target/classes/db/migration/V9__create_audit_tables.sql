-- ============================================================
-- V9__create_audit_tables.sql
-- CertiDigital — Módulo: Audit & Compliance
-- Bounded Context: Audit & Compliance
-- Aggregate: AuditEntry
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   audit_entries → tiene tenant_id (puede ser NULL para
--                   acciones de Super Admin a nivel de plataforma)
-- ============================================================

-- ============================================================
-- TABLA: audit_entries
-- Aggregate Root: AuditEntry
-- Registro INMUTABLE de acciones significativas del sistema.
--
-- DISEÑO CRÍTICO:
-- - Esta tabla es APPEND-ONLY.
-- - No existe UPDATE ni DELETE.
-- - Almacena una fotografía de la acción en el momento exacto.
-- - Es la evidencia de compliance y auditoría.
-- ============================================================
CREATE TABLE audit_entries (
    id                  VARCHAR(36)     NOT NULL,

    -- ============================================================
    -- ACTOR: ¿Quién realizó la acción?
    -- ============================================================
    actor_id            VARCHAR(36),                    -- userId o 'SYSTEM' para acciones automatizadas
    actor_type          VARCHAR(30)     NOT NULL,       -- USER | SYSTEM
    actor_name          VARCHAR(255),                   -- Snapshot del nombre del actor

    -- ============================================================
    -- CONTEXTO TENANT
    -- NULL para acciones de Super Admin (nivel plataforma)
    -- ============================================================
    tenant_id           VARCHAR(36),

    -- ============================================================
    -- ACCIÓN: ¿Qué se hizo?
    -- ============================================================
    -- Código de acción estandarizado:
    --   USER_LOGGED_IN | USER_SUSPENDED | ROLE_ASSIGNED
    --   EVENT_PUBLISHED | EVENT_CLOSED | EVENT_CANCELLED
    --   ENROLLMENT_CREATED | ATTENDANCE_RECORDED | PAYMENT_CONFIRMED
    --   ELIGIBILITY_EVALUATED | ISSUANCE_APPROVED | ISSUANCE_REJECTED
    --   CREDENTIAL_ISSUED | CREDENTIAL_REVOKED | CREDENTIAL_SUSPENDED
    --   VERIFICATION_PERFORMED | TENANT_CREATED | TENANT_SUSPENDED
    action_code         VARCHAR(100)    NOT NULL,

    -- ============================================================
    -- RECURSO AFECTADO
    -- ============================================================
    resource_type       VARCHAR(100),                   -- CREDENTIAL | USER | EVENT | ENROLLMENT | TENANT | etc.
    resource_id         VARCHAR(36),                    -- ID del recurso afectado

    -- ============================================================
    -- RESULTADO
    -- ============================================================
    result              VARCHAR(30)     NOT NULL DEFAULT 'SUCCESS',  -- SUCCESS | FAILURE

    -- ============================================================
    -- CONTEXTO DE RED
    -- ============================================================
    ip_address          VARCHAR(50),
    user_agent          VARCHAR(1000),
    request_id          VARCHAR(36),                    -- ID del request HTTP

    -- ============================================================
    -- DATOS ADICIONALES
    -- JSON con contexto relevante (sin datos sensibles)
    -- ============================================================
    payload             TEXT,                           -- JSON: datos adicionales de la acción

    -- ============================================================
    -- TIMESTAMP (ultra-preciso, UTC)
    -- ============================================================
    occurred_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_entries PRIMARY KEY (id)
    -- NOTA: No FK hacia tenants intencionalmente.
    -- Si un tenant es terminado, sus audit entries deben preservarse.
    -- NOTA: No FK hacia users intencionalmente.
    -- Si un usuario es eliminado, las audit entries deben preservarse.
);

COMMENT ON TABLE  audit_entries IS 'Log de auditoría inmutable. Registra todas las acciones significativas. APPEND-ONLY.';
COMMENT ON COLUMN audit_entries.actor_type IS 'USER (acción de usuario) | SYSTEM (acción automatizada del sistema).';
COMMENT ON COLUMN audit_entries.tenant_id IS 'NULL para acciones de Super Admin a nivel plataforma.';
COMMENT ON COLUMN audit_entries.action_code IS 'Código estandarizado: CREDENTIAL_REVOKED, USER_SUSPENDED, etc.';
COMMENT ON COLUMN audit_entries.result IS 'SUCCESS | FAILURE (si la acción fue rechazada o falló)';

-- Índices para consultas frecuentes de auditoría
CREATE INDEX idx_audit_tenant_id    ON audit_entries (tenant_id);
CREATE INDEX idx_audit_actor_id     ON audit_entries (actor_id);
CREATE INDEX idx_audit_action_code  ON audit_entries (action_code);
CREATE INDEX idx_audit_resource     ON audit_entries (resource_type, resource_id);
CREATE INDEX idx_audit_occurred_at  ON audit_entries (occurred_at);
CREATE INDEX idx_audit_result       ON audit_entries (result);
