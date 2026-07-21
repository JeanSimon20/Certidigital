-- ============================================================
-- V13__alter_receipt_url_to_text.sql
-- CertiDigital — Módulo: Payment
-- Ampliar receipt_url a TEXT para soportar vouchers en Data URL Base64
-- ============================================================
ALTER TABLE payment_records ALTER COLUMN receipt_url SET DATA TYPE TEXT;
