-- ============================================================
-- V11__add_price_to_events.sql
-- CertiDigital — Módulo: Event Management
-- Añadir columna price a la tabla events
-- ============================================================
ALTER TABLE events ADD COLUMN IF NOT EXISTS price DOUBLE PRECISION DEFAULT 0.0;
