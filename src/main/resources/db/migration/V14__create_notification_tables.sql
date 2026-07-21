-- ============================================================
-- V14__create_notification_tables.sql
-- CertiDigital — Módulo: Notification System
-- ============================================================
CREATE TABLE notifications (
    id          VARCHAR(36)     NOT NULL,
    user_id     VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    type        VARCHAR(50)     NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    message     TEXT            NOT NULL,
    link        VARCHAR(500),
    is_read     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_tenant_id ON notifications (tenant_id);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);
