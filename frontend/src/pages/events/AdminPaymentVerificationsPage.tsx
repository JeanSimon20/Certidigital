import React, { useState, useEffect } from 'react';
import { CheckCircle2, XCircle, Clock, Eye, AlertCircle, RefreshCw, FileText } from 'lucide-react';
import { paymentService, PaymentVerificationItem } from '../../features/events/services/payment.service';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';

export const AdminPaymentVerificationsPage: React.FC = () => {
  const [items, setItems] = useState<PaymentVerificationItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedVoucher, setSelectedVoucher] = useState<PaymentVerificationItem | null>(null);
  const [actionNotes, setActionNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedbackMessage, setFeedbackMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchVerifications = async () => {
    setIsLoading(true);
    try {
      const data = await paymentService.getPendingVerifications();
      setItems(data);
    } catch (err) {
      console.error('Error al obtener comprobantes pendientes:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchVerifications();
  }, []);

  const handleVerify = async (action: 'APPROVE' | 'REJECT') => {
    if (!selectedVoucher) return;
    setIsSubmitting(true);
    setFeedbackMessage(null);

    try {
      await paymentService.verifyPayment(selectedVoucher.paymentId, {
        action,
        notes: actionNotes.trim() || (action === 'APPROVE' ? 'Comprobante verificado y aprobado por administración' : 'Comprobante no válido'),
      });

      setFeedbackMessage({
        type: 'success',
        text: action === 'APPROVE' ? `✓ Pago de ${selectedVoucher.participantName} APROBADO e inscripción confirmada.` : `❌ Comprobante RECHAZADO correctamente.`,
      });

      setSelectedVoucher(null);
      setActionNotes('');
      fetchVerifications();
    } catch (err: any) {
      setFeedbackMessage({
        type: 'error',
        text: err.response?.data?.message || 'Error al procesar la verificación.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '1rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Validación de Comprobantes de Pago</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Revisa los comprobantes (Yape, Plin, Transferencia BCP) enviados por los estudiantes y aprueba sus inscripciones.
          </p>
        </div>

        <button onClick={fetchVerifications} className="btn btn-secondary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <RefreshCw size={16} /> Actualizar Lista
        </button>
      </div>

      {feedbackMessage && (
        <div
          style={{
            padding: '1rem',
            borderRadius: 'var(--radius-md)',
            marginBottom: '1.5rem',
            backgroundColor: feedbackMessage.type === 'success' ? 'var(--success-light)' : 'var(--danger-light)',
            color: feedbackMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
            fontWeight: 600,
          }}
        >
          {feedbackMessage.text}
        </div>
      )}

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={36} />
        </div>
      ) : items.length === 0 ? (
        <EmptyState
          title="Sin comprobantes pendientes"
          description="Todos los comprobantes de pago de Yape y transferencia han sido verificados."
        />
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {items.map((item) => (
            <div
              key={item.paymentId}
              className="glass-card"
              style={{
                padding: '1.25rem 1.5rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div
                  style={{
                    width: '44px',
                    height: '44px',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--warning-light)',
                    color: 'var(--warning)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Clock size={22} />
                </div>
                <div>
                  <h4 style={{ fontWeight: 700, fontSize: '1rem', marginBottom: '0.125rem' }}>{item.participantName}</h4>
                  <div style={{ fontSize: '0.813rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
                    {item.participantEmail} • <strong>{item.eventName}</strong>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <span className="badge badge-warning">
                      {item.paymentMethod === 'YAPE' ? '📱 Yape / Plin' : item.paymentMethod === 'BANK_TRANSFER' ? '🏦 Transferencia BCP' : item.paymentMethod}
                    </span>
                    <span style={{ fontSize: '0.813rem', fontWeight: 600 }}>N° Op: {item.operationNumber || 'S/N'}</span>
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary)' }}>${item.amount} USD</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{new Date(item.paymentDate).toLocaleString('es-ES')}</div>
                </div>

                <button
                  onClick={() => setSelectedVoucher(item)}
                  className="btn btn-primary"
                  style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                >
                  <Eye size={16} /> Revisar Comprobante
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal de Revisión del Administrador */}
      {selectedVoucher && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            backdropFilter: 'blur(4px)',
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '1rem',
          }}
        >
          <div
            className="glass-card"
            style={{
              maxWidth: '600px',
              width: '100%',
              padding: '2rem',
              backgroundColor: 'var(--bg-surface)',
              borderRadius: 'var(--radius-lg)',
              maxHeight: '90vh',
              overflowY: 'auto',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Revisión de Comprobante de Pago</h3>
              <button onClick={() => setSelectedVoucher(null)} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem' }}>✕</button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              <div><strong>Estudiante:</strong> {selectedVoucher.participantName}</div>
              <div><strong>Email:</strong> {selectedVoucher.participantEmail}</div>
              <div><strong>Evento:</strong> {selectedVoucher.eventName}</div>
              <div><strong>Monto a Validar:</strong> ${selectedVoucher.amount} USD</div>
              <div><strong>Método:</strong> {selectedVoucher.paymentMethod}</div>
              <div><strong>N° Operación:</strong> <span style={{ fontFamily: 'monospace', fontWeight: 700, color: 'var(--primary)' }}>{selectedVoucher.operationNumber}</span></div>
            </div>

            {/* Imagen del Voucher */}
            <div style={{ marginBottom: '1.5rem', textAlign: 'center', backgroundColor: 'var(--bg-surface-secondary)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '0.813rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>Imagen Adjunta del Comprobante (Voucher):</div>
              {selectedVoucher.voucherUrl ? (
                <img
                  src={selectedVoucher.voucherUrl}
                  alt="Voucher de Pago"
                  style={{ maxWidth: '100%', maxHeight: '280px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}
                />
              ) : (
                <div style={{ color: 'var(--text-muted)', padding: '2rem' }}>No se adjuntó archivo de imagen</div>
              )}
            </div>

            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label">Notas u Observación de la Verificación (Opcional)</label>
              <input
                type="text"
                placeholder="ej. Voucher verificado en extracto BCP a las 15:30h"
                value={actionNotes}
                onChange={(e) => setActionNotes(e.target.value)}
                className="form-input"
              />
            </div>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                onClick={() => handleVerify('APPROVE')}
                disabled={isSubmitting}
                className="btn btn-primary"
                style={{ flex: 1, padding: '0.875rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', backgroundColor: 'var(--success)' }}
              >

                {isSubmitting ? <Spinner size={20} /> : <><CheckCircle2 size={18} /> Aprobar Pago e Inscribir</>}
              </button>

              <button
                onClick={() => handleVerify('REJECT')}
                disabled={isSubmitting}
                className="btn btn-secondary"
                style={{ flex: 1, padding: '0.875rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', color: 'var(--danger)' }}
              >
                {isSubmitting ? <Spinner size={20} /> : <><XCircle size={18} /> Rechazar Comprobante</>}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
