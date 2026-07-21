-- ============================================================
-- V2__create_iam_tables.sql
-- CertiDigital — Módulo: Identity & Access Management (IAM)
-- Bounded Context: Identity & Access
-- Aggregates: User (con Membership), Role (con Permission)
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   users        → GLOBAL (no tiene tenant_id)
--                  Un usuario puede pertenecer a varios tenants
--   memberships  → Tiene tenant_id (el vínculo ES contextual)
--   roles        → Tiene tenant_id (null = rol del sistema global)
--   role_permissions → No tiene tenant_id (sigue al rol)
--   membership_roles → No tiene tenant_id (tabla de unión)
--   refresh_tokens → GLOBAL (por userId, no por tenant)
-- ============================================================

-- ============================================================
-- TABLA: users
-- Aggregate Root: User
-- Identidad global del actor en la plataforma.
-- Un usuario puede pertenecer a múltiples Tenants.
-- ============================================================
CREATE TABLE users (
    id                  VARCHAR(36)     NOT NULL,

    -- Identidad del usuario
    email               VARCHAR(255)    NOT NULL,       -- Único globalmente
    full_name           VARCHAR(255)    NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,       -- BCrypt hash

    -- Estado del usuario: ACTIVE | SUSPENDED | DELETED
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- Metadata de cuenta
    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    last_login_at       TIMESTAMP,

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

COMMENT ON TABLE  users IS 'Identidades globales de actores en la plataforma. Un usuario puede pertenecer a múltiples Tenants via Memberships.';
COMMENT ON COLUMN users.email IS 'Email único en todo el sistema (no por Tenant).';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt con factor de costo 12. Nunca texto plano.';
COMMENT ON COLUMN users.status IS 'ACTIVE | SUSPENDED | DELETED';

CREATE INDEX idx_users_email  ON users (email);
CREATE INDEX idx_users_status ON users (status);


-- ============================================================
-- TABLA: roles
-- Aggregate Root: Role
-- Conjunto nombrado de permisos asignable a usuarios.
-- tenant_id = NULL → Rol del sistema (global, no modificable).
-- tenant_id = value → Rol personalizado del Tenant.
-- ============================================================
CREATE TABLE roles (
    id                  VARCHAR(36)     NOT NULL,

    -- Tenant al que pertenece (NULL = rol de sistema global)
    tenant_id           VARCHAR(36),

    -- Nombre del rol: TENANT_ADMIN, ORGANIZER, FACILITATOR, etc.
    name                VARCHAR(100)    NOT NULL,

    -- Descripción legible del propósito del rol
    description         VARCHAR(500),

    -- true = Rol del sistema. No puede modificarse ni eliminarse.
    is_system_role      BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Estado: ACTIVE | DEPRECATED
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT fk_roles_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    -- Un rol con el mismo nombre solo puede existir una vez por Tenant
    -- (para roles de sistema, tenant_id es NULL, pero el nombre sigue siendo único)
    CONSTRAINT uq_roles_name_tenant UNIQUE (name, tenant_id)
);

COMMENT ON TABLE  roles IS 'Roles asignables a usuarios. is_system_role=true son roles globales no modificables.';
COMMENT ON COLUMN roles.tenant_id IS 'NULL = Rol de sistema global. Un tenant_id = Rol personalizado del Tenant.';

CREATE INDEX idx_roles_tenant_id    ON roles (tenant_id);
CREATE INDEX idx_roles_is_system    ON roles (is_system_role);


-- ============================================================
-- TABLA: role_permissions
-- Entidad interna del Aggregate Role.
-- Permisos atómicos del formato {resource}:{action}.
-- ============================================================
CREATE TABLE role_permissions (
    id                  VARCHAR(36)     NOT NULL,
    role_id             VARCHAR(36)     NOT NULL,

    -- Formato: {resource}:{action} — ej: "event:publish", "credential:revoke"
    resource            VARCHAR(100)    NOT NULL,       -- event, credential, enrollment, etc.
    action              VARCHAR(100)    NOT NULL,       -- create, read, update, publish, revoke, etc.

    CONSTRAINT pk_role_permissions PRIMARY KEY (id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_role_permissions_role_resource_action
        UNIQUE (role_id, resource, action)
);

COMMENT ON TABLE  role_permissions IS 'Permisos atómicos por rol. Formato resource:action.';
COMMENT ON COLUMN role_permissions.resource IS 'Recurso: tenant, user, role, event, enrollment, attendance, credential, policy, issuance, audit, org, template, certificate';
COMMENT ON COLUMN role_permissions.action IS 'Acción: create, read, update, delete, publish, close, approve, reject, revoke, suspend, export';

CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);


-- ============================================================
-- TABLA: memberships
-- Entidad interna del Aggregate User.
-- Vínculo entre un User y un Tenant con roles asignados.
-- Un mismo usuario puede tener membresías en varios Tenants.
-- ============================================================
CREATE TABLE memberships (
    id                  VARCHAR(36)     NOT NULL,

    -- Referencias
    user_id             VARCHAR(36)     NOT NULL,
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Estado del vínculo: ACTIVE | SUSPENDED | EXPIRED
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_memberships PRIMARY KEY (id),
    CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_memberships_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    -- Un usuario no puede tener dos membresías activas en el mismo Tenant
    CONSTRAINT uq_memberships_user_tenant UNIQUE (user_id, tenant_id)
);

COMMENT ON TABLE  memberships IS 'Vínculo User-Tenant. Define pertenencia de un usuario a una organización.';
COMMENT ON COLUMN memberships.status IS 'ACTIVE | SUSPENDED | EXPIRED';

CREATE INDEX idx_memberships_user_id   ON memberships (user_id);
CREATE INDEX idx_memberships_tenant_id ON memberships (tenant_id);
CREATE INDEX idx_memberships_status    ON memberships (status);


-- ============================================================
-- TABLA: membership_roles
-- Tabla de unión: relación N:M entre Membership y Role.
-- Un usuario puede tener múltiples roles dentro de un Tenant.
-- ============================================================
CREATE TABLE membership_roles (
    membership_id       VARCHAR(36)     NOT NULL,
    role_id             VARCHAR(36)     NOT NULL,

    -- Fecha en que se asignó el rol
    assigned_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         VARCHAR(36),

    CONSTRAINT pk_membership_roles
        PRIMARY KEY (membership_id, role_id),
    CONSTRAINT fk_membership_roles_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_membership_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE membership_roles IS 'Asignación de Roles a Membresías. Un usuario puede tener múltiples roles en un Tenant.';

CREATE INDEX idx_membership_roles_membership ON membership_roles (membership_id);
CREATE INDEX idx_membership_roles_role       ON membership_roles (role_id);


-- ============================================================
-- TABLA: refresh_tokens
-- Gestión de Refresh Tokens para autenticación JWT.
-- Es una tabla global (no tiene tenant_id).
-- El token de refresh es por usuario, no por tenant.
-- ============================================================
CREATE TABLE refresh_tokens (
    id                  VARCHAR(36)     NOT NULL,

    -- Referencia al usuario propietario del token
    user_id             VARCHAR(36)     NOT NULL,

    -- El token en sí (UUID o hash del token)
    token_hash          VARCHAR(255)    NOT NULL,       -- Hash del refresh token

    -- Cuándo expira
    expires_at          TIMESTAMP       NOT NULL,

    -- Si fue consumido (Refresh Token Rotation)
    used                BOOLEAN         NOT NULL DEFAULT FALSE,

    -- IP de origen del request que generó el token
    created_from_ip     VARCHAR(50),

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);

COMMENT ON TABLE  refresh_tokens IS 'Refresh Tokens para renovación de Access Tokens JWT. Usa Refresh Token Rotation.';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'Hash SHA-256 del refresh token real. El token en sí nunca se almacena.';

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);
