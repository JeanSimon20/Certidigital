-- ============================================================
-- V5__create_participation_tables.sql
-- CertiDigital — Módulo: Participation
-- Bounded Context: Participation
-- Aggregates: Participant, Enrollment (con AttendanceRecord,
--             EvaluationResult, PaymentRecord)
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--
--   participants      → SEMI-GLOBAL
--     Un participante puede asistir a eventos de múltiples
--     Tenants. Su identidad (email + documento) es global.
--     NO tiene tenant_id propio.
--
--   enrollments       → tiene tenant_id
--     La inscripción ocurre dentro de un Event de un Tenant.
--
--   attendance_records → tiene tenant_id (denormalizado)
--     Podría heredarlo via enrollment→event→tenant,
--     pero lo incluimos para eficiencia.
--
--   evaluation_results → tiene tenant_id (denormalizado)
--
--   payment_records    → tiene tenant_id (denormalizado)
-- ============================================================

-- ============================================================
-- TABLA: participants
-- Aggregate Root: Participant
-- Persona natural que es sujeto de una Credential.
-- Su identidad es global — puede participar en múltiples Tenants.
--
-- DIFERENCIA IMPORTANTE vs. User:
--   User = colaborador interno del Tenant (admin, organizador, etc.)
--   Participant = destinatario externo de la Credential
--
-- Un Participant PUEDE estar vinculado a un User si tiene
-- cuenta en la plataforma (identity_user_id), pero puede
-- existir sin ningún User asociado.
-- ============================================================
CREATE TABLE participants (
    id                  VARCHAR(36)     NOT NULL,

    -- Datos de identidad (globales)
    email               VARCHAR(255)    NOT NULL,
    full_name           VARCHAR(500)    NOT NULL,

    -- Documento de identidad
    -- Combinación {doc_type + doc_number + doc_country} es única
    doc_type            VARCHAR(50),                    -- DNI, PASAPORTE, CEDULA, NIT, etc.
    doc_number          VARCHAR(100),
    doc_country         VARCHAR(3),                     -- ISO 3166-1

    -- Estado de verificación de identidad
    -- UNVERIFIED | PENDING | VERIFIED | FAILED
    identity_status     VARCHAR(30)     NOT NULL DEFAULT 'UNVERIFIED',

    -- Enlace opcional a User (si el participante tiene cuenta)
    identity_user_id    VARCHAR(36),                    -- FK → users.id (nullable)

    -- Datos de contacto adicionales
    phone               VARCHAR(50),

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_participants PRIMARY KEY (id),
    CONSTRAINT fk_participants_user
        FOREIGN KEY (identity_user_id) REFERENCES users (id)
            ON DELETE SET NULL,
    CONSTRAINT uq_participants_email UNIQUE (email)
);

COMMENT ON TABLE  participants IS 'Personas naturales destinatarias de Credentials. Identidad global. Diferente de User (colaborador interno).';
COMMENT ON COLUMN participants.identity_user_id IS 'Vinculo opcional a un User si el Participant tiene cuenta en la plataforma.';
COMMENT ON COLUMN participants.identity_status IS 'UNVERIFIED | PENDING | VERIFIED | FAILED';

CREATE INDEX idx_participants_email       ON participants (email);
CREATE INDEX idx_participants_doc         ON participants (doc_type, doc_number, doc_country);
CREATE INDEX idx_participants_user_id     ON participants (identity_user_id);


-- ============================================================
-- TABLA: enrollments
-- Aggregate Root: Enrollment
-- Inscripción de un Participant en un Event.
-- Es la unidad central de seguimiento de elegibilidad.
-- ============================================================
CREATE TABLE enrollments (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencias
    event_id            VARCHAR(36)     NOT NULL,
    participant_id      VARCHAR(36)     NOT NULL,

    -- Estado de la inscripción: ACTIVE | WITHDRAWN | COMPLETED
    status              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',

    -- Estado de pago: NOT_REQUIRED | PENDING | CONFIRMED | REFUNDED
    payment_status      VARCHAR(30)     NOT NULL DEFAULT 'NOT_REQUIRED',

    -- Porcentaje de asistencia calculado (se actualiza al registrar asistencia)
    -- Es un campo calculado pero lo desnormalizamos para queries eficientes
    attendance_percentage DECIMAL(5,2)  NOT NULL DEFAULT 0.00,

    -- Puntaje general de evaluación (calculado)
    overall_score       DECIMAL(6,2),

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enrolled_by         VARCHAR(36),                    -- userId del que inscribió

    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    CONSTRAINT fk_enrollments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_event
        FOREIGN KEY (event_id) REFERENCES events (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id)
            ON DELETE CASCADE,
    -- Un participante solo puede tener una inscripción activa por evento
    CONSTRAINT uq_enrollment_event_participant UNIQUE (event_id, participant_id)
);

COMMENT ON TABLE  enrollments IS 'Inscripción de Participant en Event. Unidad central de seguimiento de elegibilidad.';
COMMENT ON COLUMN enrollments.status IS 'ACTIVE | WITHDRAWN | COMPLETED';
COMMENT ON COLUMN enrollments.payment_status IS 'NOT_REQUIRED | PENDING | CONFIRMED | FAILED | REFUNDED';
COMMENT ON COLUMN enrollments.attendance_percentage IS 'Campo calculado denormalizado. Se actualiza al registrar AttendanceRecords.';

CREATE INDEX idx_enrollments_tenant_id      ON enrollments (tenant_id);
CREATE INDEX idx_enrollments_event_id       ON enrollments (event_id);
CREATE INDEX idx_enrollments_participant_id ON enrollments (participant_id);
CREATE INDEX idx_enrollments_status         ON enrollments (tenant_id, status);


-- ============================================================
-- TABLA: attendance_records
-- Entidad interna del Aggregate Enrollment.
-- Registro de asistencia por sesión para un enrollment dado.
-- ============================================================
CREATE TABLE attendance_records (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO (denormalizado del enrollment)
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia al enrollment padre
    enrollment_id       VARCHAR(36)     NOT NULL,

    -- Sesión específica a la que asistió
    session_id          VARCHAR(36)     NOT NULL,

    -- ¿Asistió? true = presente, false = ausente
    attended            BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Quién registró la asistencia
    recorded_by         VARCHAR(36),                    -- userId del facilitador
    recorded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Observaciones
    notes               VARCHAR(500),

    CONSTRAINT pk_attendance_records PRIMARY KEY (id),
    CONSTRAINT fk_attendance_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_attendance_session
        FOREIGN KEY (session_id) REFERENCES event_sessions (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_attendance_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    -- Solo un registro de asistencia por enrollment+sesión
    CONSTRAINT uq_attendance_enrollment_session UNIQUE (enrollment_id, session_id)
);

COMMENT ON TABLE attendance_records IS 'Registro de asistencia por sesión. Entidad interna del Aggregate Enrollment.';

CREATE INDEX idx_attendance_enrollment ON attendance_records (enrollment_id);
CREATE INDEX idx_attendance_tenant     ON attendance_records (tenant_id);


-- ============================================================
-- TABLA: evaluation_results
-- Entidad interna del Aggregate Enrollment.
-- Resultado de una evaluación para un Enrollment específico.
-- Un Enrollment puede tener múltiples evaluaciones (examen final,
-- parciales, tareas, etc.).
-- ============================================================
CREATE TABLE evaluation_results (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia al enrollment padre
    enrollment_id       VARCHAR(36)     NOT NULL,

    -- Tipo y nombre de la evaluación
    evaluation_name     VARCHAR(255)    NOT NULL,       -- "Examen Final", "Proyecto"
    evaluation_type     VARCHAR(100)    NOT NULL DEFAULT 'EXAM',  -- EXAM | ASSIGNMENT | PROJECT

    -- Puntajes
    score               DECIMAL(6,2)    NOT NULL,
    max_score           DECIMAL(6,2)    NOT NULL DEFAULT 100.00,
    passing_score       DECIMAL(6,2)    NOT NULL DEFAULT 60.00,

    -- Resultado: ¿Aprobó?
    passed              BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Quién registró
    recorded_by         VARCHAR(36),
    recorded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_evaluation_results PRIMARY KEY (id),
    CONSTRAINT fk_evaluation_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE evaluation_results IS 'Resultados de evaluaciones por Enrollment. Entidad interna del Aggregate Enrollment.';

CREATE INDEX idx_evaluation_enrollment ON evaluation_results (enrollment_id);
CREATE INDEX idx_evaluation_tenant     ON evaluation_results (tenant_id);


-- ============================================================
-- TABLA: payment_records
-- Entidad interna del Aggregate Enrollment.
-- Registro de pago asociado a una inscripción.
-- CertiDigital NO procesa pagos directamente.
-- Solo registra el estado del pago como condición de elegibilidad.
-- ============================================================
CREATE TABLE payment_records (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencia al enrollment
    enrollment_id       VARCHAR(36)     NOT NULL,

    -- Estado del pago: PENDING | CONFIRMED | FAILED | REFUNDED
    payment_status      VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- Monto y moneda
    amount              DECIMAL(10,2),
    currency            VARCHAR(3)      DEFAULT 'USD',  -- ISO 4217

    -- Referencia externa del pago (de la pasarela de pago)
    external_reference  VARCHAR(500),
    payment_method      VARCHAR(100),                   -- CREDIT_CARD | BANK_TRANSFER | CASH | etc.

    -- Comprobante
    receipt_url         VARCHAR(1000),

    -- Timestamps
    payment_date        TIMESTAMP,
    confirmed_at        TIMESTAMP,
    confirmed_by        VARCHAR(36),                    -- userId del que confirmó

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_records PRIMARY KEY (id),
    CONSTRAINT fk_payment_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_payment_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE payment_records IS 'Registros de pago de Enrollments. CertiDigital no procesa pagos, solo registra su estado.';
COMMENT ON COLUMN payment_records.payment_status IS 'PENDING | CONFIRMED | FAILED | REFUNDED';

CREATE INDEX idx_payment_enrollment ON payment_records (enrollment_id);
CREATE INDEX idx_payment_tenant     ON payment_records (tenant_id);
CREATE INDEX idx_payment_status     ON payment_records (payment_status);
