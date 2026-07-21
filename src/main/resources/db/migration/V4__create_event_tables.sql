-- ============================================================
-- V4__create_event_tables.sql
-- CertiDigital — Módulo: Event Management
-- Bounded Context: Event Management
-- Aggregates: Event (con Session)
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   events    → tiene tenant_id (un evento pertenece a un Tenant)
--   sessions  → NO tiene tenant_id propio (hereda del Event)
--               Pero incluimos tenant_id para optimizar queries
--               sin necesidad de JOIN con events en cada consulta.
-- ============================================================

-- ============================================================
-- TABLA: events
-- Aggregate Root: Event
-- Actividad organizada por un Tenant que origina la
-- emisión de credenciales.
-- ============================================================
CREATE TABLE events (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO: El evento pertenece a un solo Tenant
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Identidad del evento
    name                VARCHAR(500)    NOT NULL,
    description         TEXT,

    -- Tipo de evento (configurable por Tenant, no enum fijo)
    -- Ejemplos: COURSE, WORKSHOP, SEMINAR, CONGRESS, BOOTCAMP, etc.
    event_type          VARCHAR(100)    NOT NULL DEFAULT 'COURSE',

    -- Modalidad: IN_PERSON | VIRTUAL | HYBRID
    mode                VARCHAR(30)     NOT NULL DEFAULT 'IN_PERSON',

    -- Fechas del evento
    start_date          TIMESTAMP       NOT NULL,
    end_date            TIMESTAMP       NOT NULL,
    timezone            VARCHAR(100)    NOT NULL DEFAULT 'UTC',

    -- Ubicación (para eventos presenciales)
    location_name       VARCHAR(500),
    location_address    TEXT,
    virtual_url         VARCHAR(1000),

    -- Capacidad máxima (null = sin límite)
    max_capacity        INTEGER,

    -- Relaciones con módulo Organization (referencias por ID)
    -- issuance_policy_id puede ser NULL hasta que se configure
    issuance_policy_id  VARCHAR(36),                   -- FK → issuance_policies.id (V8)
    credential_template_id VARCHAR(36),                -- FK → credential_templates.id

    -- Organizador principal del evento
    organizer_user_id   VARCHAR(36),                   -- FK → users.id

    -- Estado del evento: DRAFT | PUBLISHED | CLOSED | CANCELLED
    status              VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_events_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_events_template
        FOREIGN KEY (credential_template_id) REFERENCES credential_templates (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_events_organizer
        FOREIGN KEY (organizer_user_id) REFERENCES users (id)
            ON DELETE SET NULL
);

COMMENT ON TABLE  events IS 'Actividades organizadas por un Tenant que originan la emisión de Credentials.';
COMMENT ON COLUMN events.event_type IS 'Configurable: COURSE, WORKSHOP, SEMINAR, CONGRESS, BOOTCAMP, DIPLOMADO, CERTIFICATION, etc.';
COMMENT ON COLUMN events.status IS 'DRAFT | PUBLISHED | CLOSED | CANCELLED';

CREATE INDEX idx_events_tenant_id ON events (tenant_id);
CREATE INDEX idx_events_status    ON events (tenant_id, status);
CREATE INDEX idx_events_dates     ON events (start_date, end_date);


-- ============================================================
-- TABLA: event_sessions
-- Entidad interna del Aggregate Event.
-- Sesiones individuales dentro de un evento multi-jornada.
-- ============================================================
CREATE TABLE event_sessions (
    id                  VARCHAR(36)     NOT NULL,

    -- Referencia al evento padre
    event_id            VARCHAR(36)     NOT NULL,

    -- Denormalización del tenant_id para eficiencia en queries
    -- Elimina JOINs innecesarios en registros de asistencia
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Detalles de la sesión
    name                VARCHAR(500)    NOT NULL,       -- "Día 1 - Introducción"
    session_date        TIMESTAMP       NOT NULL,
    duration_minutes    INTEGER,

    -- Ubicación específica (puede diferir del evento padre)
    location_override   VARCHAR(500),

    -- Número de orden para presentación
    session_order       INTEGER         NOT NULL DEFAULT 1,

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_event_sessions PRIMARY KEY (id),
    CONSTRAINT fk_event_sessions_event
        FOREIGN KEY (event_id) REFERENCES events (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_event_sessions_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  event_sessions IS 'Sesiones individuales dentro de un Evento. Entidad interna del Aggregate Event.';

CREATE INDEX idx_event_sessions_event_id  ON event_sessions (event_id);
CREATE INDEX idx_event_sessions_tenant_id ON event_sessions (tenant_id);


-- ============================================================
-- TABLA: event_facilitators
-- Asignación de facilitadores/docentes a eventos.
-- Tabla de unión entre events y users con contexto de rol.
-- ============================================================
CREATE TABLE event_facilitators (
    event_id            VARCHAR(36)     NOT NULL,
    user_id             VARCHAR(36)     NOT NULL,

    -- Rol del facilitador en este evento específico
    -- FACILITATOR | INSTRUCTOR | COORDINATOR | EVALUATOR
    facilitator_role    VARCHAR(50)     NOT NULL DEFAULT 'FACILITATOR',

    assigned_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by         VARCHAR(36),

    CONSTRAINT pk_event_facilitators PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event_facilitators_event
        FOREIGN KEY (event_id) REFERENCES events (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_event_facilitators_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE event_facilitators IS 'Asignación de facilitadores/docentes a eventos.';

CREATE INDEX idx_event_facilitators_event ON event_facilitators (event_id);
CREATE INDEX idx_event_facilitators_user  ON event_facilitators (user_id);
