-- ============================================================
-- V6__create_policy_tables.sql
-- CertiDigital — Módulo: Policy Engine (Core Domain)
-- Bounded Context: Policy Engine
-- Aggregates: IssuancePolicy (con PolicyCondition),
--             EligibilityEvaluation,
--             IssuanceRequest
-- ============================================================
-- ANÁLISIS MULTI-TENANT:
--   issuance_policies    → tiene tenant_id
--   policy_conditions    → hereda via issuance_policy
--   eligibility_evaluations → tiene tenant_id
--   issuance_requests    → tiene tenant_id
-- ============================================================

-- ============================================================
-- TABLA: issuance_policies
-- Aggregate Root: IssuancePolicy
-- Define las reglas que determinan elegibilidad para
-- recibir una Credential.
-- ============================================================
CREATE TABLE issuance_policies (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Identidad de la política
    name                VARCHAR(255)    NOT NULL,
    description         TEXT,

    -- Operador lógico entre condiciones: AND | OR
    logical_operator    VARCHAR(10)     NOT NULL DEFAULT 'AND',

    -- ¿Requiere aprobación humana antes de emitir?
    approval_required   BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Vigencia de la credencial a emitir (null = sin vencimiento)
    credential_validity_days INTEGER,

    -- Estado: DRAFT | ACTIVE | DEPRECATED
    status              VARCHAR(30)     NOT NULL DEFAULT 'DRAFT',

    -- Auditoría
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(36),

    CONSTRAINT pk_issuance_policies PRIMARY KEY (id),
    CONSTRAINT fk_issuance_policies_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  issuance_policies IS 'Políticas de emisión configurables. Define reglas de elegibilidad para recibir una Credential.';
COMMENT ON COLUMN issuance_policies.logical_operator IS 'AND = todas las condiciones deben cumplirse. OR = al menos una.';
COMMENT ON COLUMN issuance_policies.status IS 'DRAFT | ACTIVE | DEPRECATED';

-- Agregar FK diferida desde events hacia issuance_policies
-- (V4 no podía referenciar V6 porque aún no existía)
ALTER TABLE events
    ADD CONSTRAINT fk_events_policy
        FOREIGN KEY (issuance_policy_id) REFERENCES issuance_policies (id)
            ON DELETE SET NULL;

CREATE INDEX idx_issuance_policies_tenant ON issuance_policies (tenant_id);
CREATE INDEX idx_issuance_policies_status ON issuance_policies (tenant_id, status);


-- ============================================================
-- TABLA: policy_conditions
-- Entidad interna del Aggregate IssuancePolicy.
-- Cada condición representa una regla individual que
-- el Participant debe cumplir.
-- ============================================================
CREATE TABLE policy_conditions (
    id                  VARCHAR(36)     NOT NULL,

    -- Referencia al aggregate raíz
    policy_id           VARCHAR(36)     NOT NULL,

    -- Tipo de condición:
    --   PAYMENT_REQUIRED    → payment_status = CONFIRMED
    --   MIN_ATTENDANCE      → attendance_percentage >= threshold_value
    --   MIN_SCORE           → overall_score >= threshold_value
    --   IDENTITY_VERIFIED   → identity_status = VERIFIED
    --   PROFILE_COMPLETE    → todos los campos del participant completos
    condition_type      VARCHAR(100)    NOT NULL,

    -- Valor umbral numérico (para condiciones que lo requieren)
    -- Ej: 80.00 para asistencia mínima del 80%
    threshold_value     DECIMAL(10,2),

    -- Descripción legible de la condición
    description         VARCHAR(500),

    CONSTRAINT pk_policy_conditions PRIMARY KEY (id),
    CONSTRAINT fk_policy_conditions_policy
        FOREIGN KEY (policy_id) REFERENCES issuance_policies (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  policy_conditions IS 'Condiciones individuales dentro de una IssuancePolicy.';
COMMENT ON COLUMN policy_conditions.condition_type IS 'PAYMENT_REQUIRED | MIN_ATTENDANCE | MIN_SCORE | IDENTITY_VERIFIED | PROFILE_COMPLETE';

CREATE INDEX idx_policy_conditions_policy ON policy_conditions (policy_id);


-- ============================================================
-- TABLA: eligibility_evaluations
-- Aggregate Root: EligibilityEvaluation
-- Resultado de aplicar una IssuancePolicy sobre un Enrollment.
-- Es INMUTABLE una vez completada (append-only).
-- ============================================================
CREATE TABLE eligibility_evaluations (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencias
    enrollment_id       VARCHAR(36)     NOT NULL,
    policy_id           VARCHAR(36)     NOT NULL,

    -- Resultado global: ELIGIBLE | INELIGIBLE | PENDING
    result              VARCHAR(30)     NOT NULL,

    -- Resultados detallados por condición (JSON array)
    -- Formato: [{"conditionId":"...", "type":"MIN_ATTENDANCE", "passed":true, "value":85.0, "threshold":80.0}]
    condition_results   TEXT            NOT NULL,           -- JSON

    -- Snapshot de los datos evaluados en el momento de la evaluación
    -- Inmutable. Permite reconstruir por qué se tomó la decisión.
    evidence_snapshot   TEXT            NOT NULL,           -- JSON

    -- Timestamp preciso de la evaluación
    evaluated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_eligibility_evaluations PRIMARY KEY (id),
    CONSTRAINT fk_eligibility_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_eligibility_policy
        FOREIGN KEY (policy_id) REFERENCES issuance_policies (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_eligibility_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE  eligibility_evaluations IS 'Resultado inmutable de evaluar una IssuancePolicy sobre un Enrollment. Append-only.';
COMMENT ON COLUMN eligibility_evaluations.result IS 'ELIGIBLE | INELIGIBLE | PENDING';
COMMENT ON COLUMN eligibility_evaluations.evidence_snapshot IS 'Snapshot JSON inmutable de los datos en el momento de la evaluación (asistencia, pago, nota).';

CREATE INDEX idx_eligibility_enrollment ON eligibility_evaluations (enrollment_id);
CREATE INDEX idx_eligibility_tenant     ON eligibility_evaluations (tenant_id);
CREATE INDEX idx_eligibility_result     ON eligibility_evaluations (result);


-- ============================================================
-- TABLA: issuance_requests
-- Aggregate Root: IssuanceRequest
-- Solicitud formal de emisión generada tras evaluación
-- positiva de elegibilidad.
-- Tiene su propia máquina de estados, separada de Credential.
-- ============================================================
CREATE TABLE issuance_requests (
    id                  VARCHAR(36)     NOT NULL,

    -- AISLAMIENTO
    tenant_id           VARCHAR(36)     NOT NULL,

    -- Referencias al contexto de la solicitud
    evaluation_id       VARCHAR(36)     NOT NULL,       -- FK → eligibility_evaluations.id
    enrollment_id       VARCHAR(36)     NOT NULL,
    participant_id      VARCHAR(36)     NOT NULL,
    event_id            VARCHAR(36)     NOT NULL,
    policy_id           VARCHAR(36)     NOT NULL,
    template_id         VARCHAR(36),                    -- FK → credential_templates.id

    -- Máquina de estados de la IssuanceRequest:
    -- PENDING | UNDER_REVIEW | APPROVED | REJECTED | PROCESSING | COMPLETED | FAILED
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- Registro de revisión humana (si approval_required = true)
    reviewer_user_id    VARCHAR(36),
    reviewed_at         TIMESTAMP,
    review_decision     VARCHAR(30),                    -- APPROVED | REJECTED
    review_notes        TEXT,

    -- Control de reintentos para errores de procesamiento
    retry_count         INTEGER         NOT NULL DEFAULT 0,
    max_retries         INTEGER         NOT NULL DEFAULT 3,
    last_error          TEXT,

    -- Log de procesamiento (JSON array append-only)
    processing_log      TEXT,                           -- JSON

    -- Timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        TIMESTAMP,

    CONSTRAINT pk_issuance_requests PRIMARY KEY (id),
    CONSTRAINT fk_issuance_req_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_issuance_req_evaluation
        FOREIGN KEY (evaluation_id) REFERENCES eligibility_evaluations (id),
    CONSTRAINT fk_issuance_req_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments (id),
    CONSTRAINT fk_issuance_req_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id),
    CONSTRAINT fk_issuance_req_event
        FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_issuance_req_policy
        FOREIGN KEY (policy_id) REFERENCES issuance_policies (id),
    CONSTRAINT fk_issuance_req_template
        FOREIGN KEY (template_id) REFERENCES credential_templates (id)
            ON DELETE SET NULL
);

COMMENT ON TABLE  issuance_requests IS 'Solicitudes de emisión de Credential. Máquina de estados separada de Credential.';
COMMENT ON COLUMN issuance_requests.status IS 'PENDING | UNDER_REVIEW | APPROVED | REJECTED | PROCESSING | COMPLETED | FAILED';

CREATE INDEX idx_issuance_requests_tenant  ON issuance_requests (tenant_id);
CREATE INDEX idx_issuance_requests_status  ON issuance_requests (tenant_id, status);
CREATE INDEX idx_issuance_requests_event   ON issuance_requests (event_id);
CREATE INDEX idx_issuance_requests_participant ON issuance_requests (participant_id);
