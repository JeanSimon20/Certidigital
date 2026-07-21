-- ============================================================
-- V10__add_teacher_participant_roles.sql
-- CertiDigital — Módulo: IAM (System Roles update)
-- Agrega los roles TEACHER y PARTICIPANT a nivel de sistema.
-- ============================================================

-- Role TEACHER
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-teacher-0000000000000000000000', NULL,
        'TEACHER', 'Profesor/Docente. Imparte cursos, registra asistencia y evalúa.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Role PARTICIPANT
INSERT INTO roles (id, tenant_id, name, description, is_system_role, status, created_at, updated_at)
VALUES ('role-participant-00000000000000000', NULL,
        'PARTICIPANT', 'Estudiante/Participante en eventos de una organización.',
        TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Permisos del TEACHER
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-te-001', 'role-teacher-0000000000000000000000', 'event', 'read'),
    ('perm-te-002', 'role-teacher-0000000000000000000000', 'enrollment', 'read'),
    ('perm-te-003', 'role-teacher-0000000000000000000000', 'attendance', 'record'),
    ('perm-te-004', 'role-teacher-0000000000000000000000', 'evaluation', 'record');

-- Permisos del PARTICIPANT
INSERT INTO role_permissions (id, role_id, resource, action) VALUES
    ('perm-pa-001', 'role-participant-00000000000000000', 'event', 'read'),
    ('perm-pa-002', 'role-participant-00000000000000000', 'credential', 'read');
