-- ============================================================
-- S1__seed_dev_data.sql
-- CertiDigital — Datos ficticios para desarrollo local
-- Perfil: LOCAL solo
-- ============================================================
-- FLUJO COMPLETO DE PRUEBA:
--   Tenant (Universidad Tech) →
--   Users (Admin + Organizer) →
--   Roles y Permisos →
--   Memberships →
--   Organization Profile →
--   Digital Certificate →
--   Credential Template →
--   Event (Taller Python) →
--   Sessions (2 sesiones) →
--   Participant (Juan Pérez) →
--   Enrollment →
--   Attendance Records →
--   Evaluation Result →
--   Payment Record →
--   IssuancePolicy + Conditions →
--   EligibilityEvaluation →
--   IssuanceRequest →
--   Credential emitida →
--   Blockchain Record →
--   Verification Request →
--   Audit Entries
-- ============================================================

-- ============================================================
-- 1. TENANT — Universidad Tecnológica del Norte
-- ============================================================
INSERT INTO tenants (
    id, legal_name, commercial_name, tax_id, sector, country_code,
    status, contact_name, contact_email, contact_phone, service_plan,
    created_at, updated_at
) VALUES (
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Universidad Tecnológica del Norte',
    'UniTech',
    'RTN-12345-2024',
    'EDUCATION',
    'HND',
    'ACTIVE',
    'Dr. Carlos Mendoza',
    'admin@unitech.edu.hn',
    '+504-2234-5678',
    'STANDARD',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. USERS — Tres usuarios del sistema
-- ============================================================

-- 2a. Super Admin de la plataforma (no pertenece a ningún Tenant)
-- Password: SuperAdmin@2024! → BCrypt hash (generado offline)
INSERT INTO users (
    id, email, full_name, password_hash, status, email_verified,
    created_at, updated_at
) VALUES (
    'user-superadmin-0000-0000-000000000000',
    'superadmin@certidigital.com',
    'Super Administrator',
    '$2a$12$DY.F0PNiJgHUwpSF1RA5Iu4U.i/7.AkHPuJNBa0rZs5m28r5ZJE2', -- SuperAdmin@2024!
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2b. Admin del Tenant (gestiona la institución)
-- Password: Admin@2024! → BCrypt hash
INSERT INTO users (
    id, email, full_name, password_hash, status, email_verified,
    created_at, updated_at
) VALUES (
    'user-admin-0001-aaaa-bbbb-cccccccccccc',
    'admin@unitech.edu.hn',
    'Dr. Carlos Mendoza',
    '$2a$12$KNQaxHg5mJLi5P3k5R8s8OpR0.F0BF7g3v.r4J9lGaLSqS4FRgMhC', -- Admin@2024!
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2c. Organizador de eventos
-- Password: Organizer@2024! → BCrypt hash
INSERT INTO users (
    id, email, full_name, password_hash, status, email_verified,
    created_at, updated_at
) VALUES (
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    'maria.gonzalez@unitech.edu.hn',
    'Lic. María González',
    '$2a$12$0RjgZjmKnHSsE.LS0qAuReKm1F1/R5j7j2.Ufq35Yiw2MRRsXVra', -- Organizer@2024!
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 3. ROLES — Roles del sistema (is_system_role = TRUE)
-- ============================================================

-- Super Admin (nivel plataforma)
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-super-admin-000000000000000000', NULL,
        'SUPER_ADMIN', 'Administrador de la plataforma CertiDigital. Acceso total.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tenant Admin
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-tenant-admin-0000000000000000', NULL,
        'TENANT_ADMIN', 'Administrador de la organización emisora. Acceso total dentro del Tenant.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Organizer
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-organizer-00000000000000000000', NULL,
        'ORGANIZER', 'Organizador de eventos. Crea y gestiona eventos, inscripciones y asistencia.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Credential Approver
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-approver-000000000000000000000', NULL,
        'CREDENTIAL_APPROVER', 'Aprobador de solicitudes de emisión de credenciales.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Facilitator
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-facilitator-00000000000000000', NULL,
        'FACILITATOR', 'Facilitador o docente. Registra asistencia y evaluaciones.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Audit Reviewer
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-audit-reviewer-0000000000000', NULL,
        'AUDIT_REVIEWER', 'Revisor de auditoría. Solo lectura de logs y reportes.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Viewer
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-viewer-000000000000000000000000', NULL,
        'VIEWER', 'Solo lectura de eventos y credenciales.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ============================================================
-- 4. PERMISOS — Permisos por rol
-- ============================================================

-- SUPER_ADMIN — Permisos de plataforma
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-sa-001', 'role-super-admin-000000000000000000', 'tenant', 'create'),
    ('perm-sa-002', 'role-super-admin-000000000000000000', 'tenant', 'read'),
    ('perm-sa-003', 'role-super-admin-000000000000000000', 'tenant', 'update'),
    ('perm-sa-004', 'role-super-admin-000000000000000000', 'tenant', 'suspend'),
    ('perm-sa-005', 'role-super-admin-000000000000000000', 'tenant', 'reactivate'),
    ('perm-sa-006', 'role-super-admin-000000000000000000', 'user', 'create'),
    ('perm-sa-007', 'role-super-admin-000000000000000000', 'user', 'read'),
    ('perm-sa-008', 'role-super-admin-000000000000000000', 'audit', 'read'),
    ('perm-sa-009', 'role-super-admin-000000000000000000', 'audit', 'export');

-- TENANT_ADMIN — Permisos de gestión de organización
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-ta-001', 'role-tenant-admin-0000000000000000', 'user', 'create'),
    ('perm-ta-002', 'role-tenant-admin-0000000000000000', 'user', 'read'),
    ('perm-ta-003', 'role-tenant-admin-0000000000000000', 'user', 'update'),
    ('perm-ta-004', 'role-tenant-admin-0000000000000000', 'user', 'suspend'),
    ('perm-ta-005', 'role-tenant-admin-0000000000000000', 'role', 'assign'),
    ('perm-ta-006', 'role-tenant-admin-0000000000000000', 'org', 'configure'),
    ('perm-ta-007', 'role-tenant-admin-0000000000000000', 'template', 'manage'),
    ('perm-ta-008', 'role-tenant-admin-0000000000000000', 'certificate', 'manage'),
    ('perm-ta-009', 'role-tenant-admin-0000000000000000', 'policy', 'create'),
    ('perm-ta-010', 'role-tenant-admin-0000000000000000', 'policy', 'read'),
    ('perm-ta-011', 'role-tenant-admin-0000000000000000', 'issuance', 'approve'),
    ('perm-ta-012', 'role-tenant-admin-0000000000000000', 'issuance', 'reject'),
    ('perm-ta-013', 'role-tenant-admin-0000000000000000', 'credential', 'read'),
    ('perm-ta-014', 'role-tenant-admin-0000000000000000', 'credential', 'revoke'),
    ('perm-ta-015', 'role-tenant-admin-0000000000000000', 'credential', 'suspend'),
    ('perm-ta-016', 'role-tenant-admin-0000000000000000', 'audit', 'read'),
    ('perm-ta-017', 'role-tenant-admin-0000000000000000', 'audit', 'export'),
    ('perm-ta-018', 'role-tenant-admin-0000000000000000', 'event', 'create'),
    ('perm-ta-019', 'role-tenant-admin-0000000000000000', 'event', 'publish'),
    ('perm-ta-020', 'role-tenant-admin-0000000000000000', 'event', 'close');

-- ORGANIZER — Permisos de gestión de eventos y participación
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-or-001', 'role-organizer-00000000000000000000', 'event', 'create'),
    ('perm-or-002', 'role-organizer-00000000000000000000', 'event', 'read'),
    ('perm-or-003', 'role-organizer-00000000000000000000', 'event', 'update'),
    ('perm-or-004', 'role-organizer-00000000000000000000', 'event', 'publish'),
    ('perm-or-005', 'role-organizer-00000000000000000000', 'event', 'close'),
    ('perm-or-006', 'role-organizer-00000000000000000000', 'enrollment', 'create'),
    ('perm-or-007', 'role-organizer-00000000000000000000', 'enrollment', 'read'),
    ('perm-or-008', 'role-organizer-00000000000000000000', 'attendance', 'record'),
    ('perm-or-009', 'role-organizer-00000000000000000000', 'evaluation', 'record'),
    ('perm-or-010', 'role-organizer-00000000000000000000', 'payment', 'confirm'),
    ('perm-or-011', 'role-organizer-00000000000000000000', 'credential', 'read');

-- FACILITATOR — Permisos mínimos
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-fa-001', 'role-facilitator-00000000000000000', 'event', 'read'),
    ('perm-fa-002', 'role-facilitator-00000000000000000', 'enrollment', 'read'),
    ('perm-fa-003', 'role-facilitator-00000000000000000', 'attendance', 'record'),
    ('perm-fa-004', 'role-facilitator-00000000000000000', 'evaluation', 'record');

-- CREDENTIAL_APPROVER
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-ca-001', 'role-approver-000000000000000000000', 'issuance', 'approve'),
    ('perm-ca-002', 'role-approver-000000000000000000000', 'issuance', 'reject'),
    ('perm-ca-003', 'role-approver-000000000000000000000', 'issuance', 'read'),
    ('perm-ca-004', 'role-approver-000000000000000000000', 'credential', 'read'),
    ('perm-ca-005', 'role-approver-000000000000000000000', 'enrollment', 'read');

-- AUDIT_REVIEWER
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-ar-001', 'role-audit-reviewer-0000000000000', 'audit', 'read'),
    ('perm-ar-002', 'role-audit-reviewer-0000000000000', 'audit', 'export'),
    ('perm-ar-003', 'role-audit-reviewer-0000000000000', 'credential', 'read');

-- VIEWER
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-vi-001', 'role-viewer-000000000000000000000000', 'event', 'read'),
    ('perm-vi-002', 'role-viewer-000000000000000000000000', 'credential', 'read'),
    ('perm-vi-003', 'role-viewer-000000000000000000000000', 'enrollment', 'read');


-- ============================================================
-- 5. MEMBERSHIPS — Vincular usuarios al Tenant
-- ============================================================

-- Admin del Tenant
INSERT INTO memberships (id, user_id, tenant_id, status, created_at, updated_at)
VALUES ('memb-admin-0001-aaaa-bbbb-cccccccccccc',
        'user-admin-0001-aaaa-bbbb-cccccccccccc',
        'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
        'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Organizer del Tenant
INSERT INTO memberships (id, user_id, tenant_id, status, created_at, updated_at)
VALUES ('memb-organizer-002-aaaa-bbbb-cccccccc',
        'user-organizer-0002-aaaa-bbbb-cccccccc',
        'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
        'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ============================================================
-- 6. MEMBERSHIP_ROLES — Asignar roles a membresías
-- ============================================================

-- Admin → TENANT_ADMIN
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
VALUES ('memb-admin-0001-aaaa-bbbb-cccccccccccc',
        'role-tenant-admin-0000000000000000',
        CURRENT_TIMESTAMP);

-- Organizer → ORGANIZER
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
VALUES ('memb-organizer-002-aaaa-bbbb-cccccccc',
        'role-organizer-00000000000000000000',
        CURRENT_TIMESTAMP);


-- ============================================================
-- 7. ORGANIZATION PROFILE
-- ============================================================
INSERT INTO organization_profiles (
    id, tenant_id, legal_name, commercial_name, description,
    website, primary_color, secondary_color, font_family,
    contact_email, country_code, state_province, city,
    profile_status, created_at, updated_at
) VALUES (
    'orgp-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Universidad Tecnológica del Norte',
    'UniTech',
    'Institución de educación superior líder en tecnología e innovación en Honduras.',
    'https://www.unitech.edu.hn',
    '#1A3C5E',
    '#F5A623',
    'Inter',
    'info@unitech.edu.hn',
    'HND',
    'Francisco Morazán',
    'Tegucigalpa',
    'COMPLETE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 8. DIGITAL CERTIFICATE (Simulado para desarrollo)
-- ============================================================
INSERT INTO digital_certificates (
    id, tenant_id, subject_name, issuer_name, serial_number,
    fingerprint, keystore_alias, keystore_path,
    valid_from, valid_to, status, created_at, updated_at
) VALUES (
    'cert-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'CN=Universidad Tecnológica del Norte, O=UniTech, C=HN',
    'CN=CertiDigital Dev CA, O=CertiDigital, C=HN',
    'SN-20240001',
    'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2',
    'unitech-dev',
    './keystore/dev-keystore.p12',
    DATEADD(YEAR, -1, CURRENT_TIMESTAMP),
    DATEADD(YEAR, 2, CURRENT_TIMESTAMP),
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 9. CREDENTIAL TEMPLATE
-- ============================================================
INSERT INTO credential_templates (
    id, tenant_id, name, description, credential_type,
    visual_layout, attribute_schema, status, created_at, updated_at
) VALUES (
    'tmpl-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Certificado de Participación — UniTech',
    'Plantilla estándar para certificados de participación en talleres y cursos.',
    'CERTIFICATE',
    '<html><body><h1>{{participantName}}</h1><p>Ha completado el {{eventName}}</p><p>Emitido el {{issuedAt}}</p></body></html>',
    '{"required":["participantName","eventName","issuedAt"],"optional":["score","attendancePercentage"]}',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 10. ISSUANCE POLICY + CONDITIONS
-- ============================================================
INSERT INTO issuance_policies (
    id, tenant_id, name, description, logical_operator,
    approval_required, credential_validity_days, status, created_at, updated_at
) VALUES (
    'policy-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Política Estándar de Participación',
    'El participante debe tener mínimo 80% de asistencia y nota aprobatoria >= 70.',
    'AND',
    FALSE,
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Condición 1: Asistencia mínima del 80%
INSERT INTO policy_conditions (id, policy_id, condition_type, threshold_value, description)
VALUES ('cond-001-aaaa-bbbb-cccc-dddddddddddd',
        'policy-001-aaaa-bbbb-cccc-dddddddddddd',
        'MIN_ATTENDANCE', 80.00,
        'El participante debe tener mínimo 80% de asistencia sobre las sesiones del evento.');

-- Condición 2: Nota mínima de 70
INSERT INTO policy_conditions (id, policy_id, condition_type, threshold_value, description)
VALUES ('cond-002-aaaa-bbbb-cccc-dddddddddddd',
        'policy-001-aaaa-bbbb-cccc-dddddddddddd',
        'MIN_SCORE', 70.00,
        'El participante debe obtener una calificación igual o superior a 70 puntos.');

-- Condición 3: Pago confirmado
INSERT INTO policy_conditions (id, policy_id, condition_type, threshold_value, description)
VALUES ('cond-003-aaaa-bbbb-cccc-dddddddddddd',
        'policy-001-aaaa-bbbb-cccc-dddddddddddd',
        'PAYMENT_REQUIRED', NULL,
        'El participante debe tener el pago confirmado para recibir el certificado.');


-- ============================================================
-- 11. EVENT — Taller de Python Avanzado
-- ============================================================
INSERT INTO events (
    id, tenant_id, name, description, event_type, mode,
    start_date, end_date, timezone, location_name, location_address,
    max_capacity, issuance_policy_id, credential_template_id,
    organizer_user_id, status, created_at, updated_at, created_by
) VALUES (
    'event-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Taller de Python Avanzado — Edición 2024',
    'Taller intensivo de Python para desarrolladores con experiencia. Cubre programación funcional, asyncio, y APIs REST con FastAPI.',
    'WORKSHOP',
    'IN_PERSON',
    DATEADD(MONTH, -1, CURRENT_TIMESTAMP),
    DATEADD(MONTH, -1, DATEADD(DAY, 3, CURRENT_TIMESTAMP)),
    'America/Tegucigalpa',
    'Campus Principal UniTech — Sala de Cómputo A',
    'Blvd. Morazán, Tegucigalpa, Honduras',
    30,
    'policy-001-aaaa-bbbb-cccc-dddddddddddd',
    'tmpl-001-aaaa-bbbb-cccc-dddddddddddd',
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    'CLOSED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'user-organizer-0002-aaaa-bbbb-cccccccc'
);

-- ============================================================
-- 12. EVENT SESSIONS (2 sesiones del taller)
-- ============================================================

-- Sesión 1
INSERT INTO event_sessions (id, event_id, tenant_id, name, session_date, duration_minutes, session_order, created_at)
VALUES ('sess-001-aaaa-bbbb-cccc-dddddddddddd',
        'event-001-aaaa-bbbb-cccc-dddddddddddd',
        'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
        'Día 1 — Programación Funcional y Asyncio',
        DATEADD(MONTH, -1, CURRENT_TIMESTAMP),
        480,  -- 8 horas
        1,
        CURRENT_TIMESTAMP);

-- Sesión 2
INSERT INTO event_sessions (id, event_id, tenant_id, name, session_date, duration_minutes, session_order, created_at)
VALUES ('sess-002-aaaa-bbbb-cccc-dddddddddddd',
        'event-001-aaaa-bbbb-cccc-dddddddddddd',
        'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
        'Día 2 — APIs REST con FastAPI y Despliegue',
        DATEADD(MONTH, -1, DATEADD(DAY, 1, CURRENT_TIMESTAMP)),
        480,  -- 8 horas
        2,
        CURRENT_TIMESTAMP);

-- Asignar organizer como facilitador
INSERT INTO event_facilitators (event_id, user_id, facilitator_role, assigned_at)
VALUES ('event-001-aaaa-bbbb-cccc-dddddddddddd',
        'user-organizer-0002-aaaa-bbbb-cccccccc',
        'INSTRUCTOR',
        CURRENT_TIMESTAMP);


-- ============================================================
-- 13. PARTICIPANT — Juan Pérez (destinatario de la Credential)
-- ============================================================
INSERT INTO participants (
    id, email, full_name, doc_type, doc_number, doc_country,
    identity_status, phone, created_at, updated_at
) VALUES (
    'part-001-aaaa-bbbb-cccc-dddddddddddd',
    'juan.perez@gmail.com',
    'Juan Carlos Pérez Rodríguez',
    'DNI',
    '0501-1990-01234',
    'HND',
    'UNVERIFIED',
    '+504-9876-5432',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 14. ENROLLMENT
-- ============================================================
INSERT INTO enrollments (
    id, tenant_id, event_id, participant_id, status,
    payment_status, attendance_percentage, overall_score,
    created_at, updated_at, enrolled_by
) VALUES (
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'event-001-aaaa-bbbb-cccc-dddddddddddd',
    'part-001-aaaa-bbbb-cccc-dddddddddddd',
    'COMPLETED',
    'CONFIRMED',
    100.00,      -- Asistió a las 2 sesiones (100%)
    85.00,       -- Nota final: 85 / 100
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'user-organizer-0002-aaaa-bbbb-cccccccc'
);


-- ============================================================
-- 15. ATTENDANCE RECORDS — Asistió a las 2 sesiones
-- ============================================================

INSERT INTO attendance_records (
    id, tenant_id, enrollment_id, session_id, attended,
    recorded_by, recorded_at, notes
) VALUES (
    'attn-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'sess-001-aaaa-bbbb-cccc-dddddddddddd',
    TRUE,
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    CURRENT_TIMESTAMP,
    'Participó activamente en todos los ejercicios.'
);

INSERT INTO attendance_records (
    id, tenant_id, enrollment_id, session_id, attended,
    recorded_by, recorded_at, notes
) VALUES (
    'attn-002-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'sess-002-aaaa-bbbb-cccc-dddddddddddd',
    TRUE,
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    CURRENT_TIMESTAMP,
    'Completó el proyecto final exitosamente.'
);


-- ============================================================
-- 16. EVALUATION RESULT
-- ============================================================
INSERT INTO evaluation_results (
    id, tenant_id, enrollment_id, evaluation_name, evaluation_type,
    score, max_score, passing_score, passed, recorded_by, recorded_at
) VALUES (
    'eval-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'Proyecto Final — API REST con FastAPI',
    'PROJECT',
    85.00,
    100.00,
    70.00,
    TRUE,
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 17. PAYMENT RECORD
-- ============================================================
INSERT INTO payment_records (
    id, tenant_id, enrollment_id, payment_status, amount, currency,
    external_reference, payment_method, payment_date,
    confirmed_at, confirmed_by, created_at, updated_at
) VALUES (
    'pay-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'CONFIRMED',
    500.00,
    'HNL',
    'REF-BANCO-20240101-001',
    'BANK_TRANSFER',
    DATEADD(MONTH, -2, CURRENT_TIMESTAMP),
    DATEADD(MONTH, -2, DATEADD(DAY, 1, CURRENT_TIMESTAMP)),
    'user-organizer-0002-aaaa-bbbb-cccccccc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 18. ELIGIBILITY EVALUATION
-- ============================================================
INSERT INTO eligibility_evaluations (
    id, tenant_id, enrollment_id, policy_id, result,
    condition_results, evidence_snapshot, evaluated_at
) VALUES (
    'elev-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'policy-001-aaaa-bbbb-cccc-dddddddddddd',
    'ELIGIBLE',
    '[{"conditionId":"cond-001-aaaa-bbbb-cccc-dddddddddddd","type":"MIN_ATTENDANCE","passed":true,"value":100.0,"threshold":80.0},{"conditionId":"cond-002-aaaa-bbbb-cccc-dddddddddddd","type":"MIN_SCORE","passed":true,"value":85.0,"threshold":70.0},{"conditionId":"cond-003-aaaa-bbbb-cccc-dddddddddddd","type":"PAYMENT_REQUIRED","passed":true,"value":null,"threshold":null}]',
    '{"enrollmentId":"enrl-001-aaaa-bbbb-cccc-dddddddddddd","attendancePercentage":100.0,"overallScore":85.0,"paymentStatus":"CONFIRMED","evaluatedAt":"2024-07-21T18:00:00Z"}',
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 19. ISSUANCE REQUEST
-- ============================================================
INSERT INTO issuance_requests (
    id, tenant_id, evaluation_id, enrollment_id, participant_id,
    event_id, policy_id, template_id, status,
    created_at, updated_at, completed_at
) VALUES (
    'ireq-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'elev-001-aaaa-bbbb-cccc-dddddddddddd',
    'enrl-001-aaaa-bbbb-cccc-dddddddddddd',
    'part-001-aaaa-bbbb-cccc-dddddddddddd',
    'event-001-aaaa-bbbb-cccc-dddddddddddd',
    'policy-001-aaaa-bbbb-cccc-dddddddddddd',
    'tmpl-001-aaaa-bbbb-cccc-dddddddddddd',
    'COMPLETED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 20. CREDENTIAL EMITIDA
-- ============================================================
INSERT INTO credentials (
    id, tenant_id, credential_type, public_code,
    participant_id, participant_name, participant_email, participant_doc,
    issuer_tenant_id, issuer_name, issuer_country,
    event_id, event_name, policy_id, issuance_request_id, template_id,
    attributes, issued_at, expires_at, status,
    signature_value, signing_algorithm, signed_at, digital_cert_id,
    content_hash,
    blockchain_network, blockchain_tx_id, blockchain_registered_at,
    verification_url, qr_code_url, document_url,
    created_at, updated_at
) VALUES (
    'cred-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'CERTIFICATE',
    'CERT-2024-00001',
    'part-001-aaaa-bbbb-cccc-dddddddddddd',
    'Juan Carlos Pérez Rodríguez',
    'juan.perez@gmail.com',
    'DNI-0501-1990-01234',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'Universidad Tecnológica del Norte',
    'HND',
    'event-001-aaaa-bbbb-cccc-dddddddddddd',
    'Taller de Python Avanzado — Edición 2024',
    'policy-001-aaaa-bbbb-cccc-dddddddddddd',
    'ireq-001-aaaa-bbbb-cccc-dddddddddddd',
    'tmpl-001-aaaa-bbbb-cccc-dddddddddddd',
    '{"attendancePercentage":100.0,"finalScore":85.0,"eventDuration":"16 horas","completionDate":"2024-07-21"}',
    CURRENT_TIMESTAMP,
    NULL,   -- Sin vencimiento
    'ACTIVE',
    'SIMULATED_SIGNATURE_BASE64_VALUE_FOR_DEV_PURPOSES_ONLY_NOT_A_REAL_SIGNATURE',
    'RSA-SHA256',
    CURRENT_TIMESTAMP,
    'cert-001-aaaa-bbbb-cccc-dddddddddddd',
    'a3f5c8d2e9b1f4a7c0e3d6b9f2a5c8e1d4b7f0a3c6e9b2f5a8c1e4d7b0f3a6c9',
    'SIMULATOR',
    'tx-001-aaaa-bbbb-cccc-dddddddddddd',
    CURRENT_TIMESTAMP,
    'http://localhost:8080/api/public/verify/cred-001-aaaa-bbbb-cccc-dddddddddddd',
    'http://localhost:8080/api/public/qr/cred-001-aaaa-bbbb-cccc-dddddddddddd',
    'http://localhost:8080/api/credentials/cred-001-aaaa-bbbb-cccc-dddddddddddd/document',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 21. BLOCKCHAIN RECORD (simulado)
-- ============================================================
INSERT INTO blockchain_records (
    id, tenant_id, credential_id, content_hash, tx_id,
    block_number, network, registered_at, metadata
) VALUES (
    'bcrec-001-aaaa-bbbb-cccc-dddddddddddd',
    'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
    'cred-001-aaaa-bbbb-cccc-dddddddddddd',
    'a3f5c8d2e9b1f4a7c0e3d6b9f2a5c8e1d4b7f0a3c6e9b2f5a8c1e4d7b0f3a6c9',
    'tx-001-aaaa-bbbb-cccc-dddddddddddd',
    1,
    'SIMULATOR',
    CURRENT_TIMESTAMP,
    '{"simulatorVersion":"1.0","note":"Simulated blockchain record for development purposes."}'
);


-- ============================================================
-- 22. VERIFICATION REQUEST (alguien verificó el certificado)
-- ============================================================
INSERT INTO verification_requests (
    id, credential_id, verification_method, result,
    requestor_ip, verified_at
) VALUES (
    'vreq-001-aaaa-bbbb-cccc-dddddddddddd',
    'cred-001-aaaa-bbbb-cccc-dddddddddddd',
    'QR',
    'VALID',
    '127.0.0.1',
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 23. AUDIT ENTRIES — Log de acciones importantes
-- ============================================================
INSERT INTO audit_entries (
    id, actor_id, actor_type, actor_name, tenant_id,
    action_code, resource_type, resource_id, result,
    ip_address, occurred_at, payload
) VALUES
    ('aud-001', 'user-admin-0001-aaaa-bbbb-cccccccccccc', 'USER', 'Dr. Carlos Mendoza',
     'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
     'TENANT_ACTIVATED', 'TENANT', 'tenant-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     '192.168.1.100', CURRENT_TIMESTAMP,
     '{"action":"Tenant activated by Super Admin"}'),

    ('aud-002', 'user-organizer-0002-aaaa-bbbb-cccccccc', 'USER', 'Lic. María González',
     'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
     'EVENT_PUBLISHED', 'EVENT', 'event-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     '192.168.1.101', CURRENT_TIMESTAMP,
     '{"eventName":"Taller de Python Avanzado — Edición 2024"}'),

    ('aud-003', 'user-organizer-0002-aaaa-bbbb-cccccccc', 'USER', 'Lic. María González',
     'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
     'ENROLLMENT_CREATED', 'ENROLLMENT', 'enrl-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     '192.168.1.101', CURRENT_TIMESTAMP,
     '{"participantName":"Juan Carlos Pérez Rodríguez","eventName":"Taller de Python Avanzado"}'),

    ('aud-004', 'SYSTEM', 'SYSTEM', 'EligibilityEvaluator',
     'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
     'ELIGIBILITY_EVALUATED', 'ENROLLMENT', 'enrl-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     NULL, CURRENT_TIMESTAMP,
     '{"result":"ELIGIBLE","conditionsPassed":3,"conditionsTotal":3}'),

    ('aud-005', 'SYSTEM', 'SYSTEM', 'IssuanceEngine',
     'tenant-001-aaaa-bbbb-cccc-dddddddddddd',
     'CREDENTIAL_ISSUED', 'CREDENTIAL', 'cred-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     NULL, CURRENT_TIMESTAMP,
     '{"credentialType":"CERTIFICATE","publicCode":"CERT-2024-00001","participantName":"Juan Carlos Pérez Rodríguez"}'),

    ('aud-006', NULL, 'SYSTEM', 'VerificationPortal',
     NULL,
     'VERIFICATION_PERFORMED', 'CREDENTIAL', 'cred-001-aaaa-bbbb-cccc-dddddddddddd', 'SUCCESS',
     '127.0.0.1', CURRENT_TIMESTAMP,
     '{"method":"QR","result":"VALID"}');
