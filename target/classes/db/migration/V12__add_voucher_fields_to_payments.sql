-- ============================================================
-- V12__add_voucher_fields_to_payments.sql
-- CertiDigital — Módulo: Payment
-- Añadir campos para comprobantes de pago (Yape / Transferencia)
-- y notas de revisión administrativa.
-- ============================================================
ALTER TABLE payment_records ADD COLUMN IF NOT EXISTS operation_number VARCHAR(100);
ALTER TABLE payment_records ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE payment_records ALTER COLUMN receipt_url SET DATA TYPE TEXT;
