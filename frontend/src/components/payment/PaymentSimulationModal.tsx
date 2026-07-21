import React, { useState } from 'react';
import { CreditCard, CheckCircle2, XCircle, ShieldCheck } from 'lucide-react';
import { paymentService } from '../../features/events/services/payment.service';
import { PaymentResultResponse } from '../../types/payment.types';
import { Spinner } from '../feedback/Spinner';

interface PaymentSimulationModalProps {
  isOpen: boolean;
  onClose: () => void;
  enrollmentId: string;
  tenantId: string;
  amount: number;
  eventName: string;
  onPaymentComplete: () => void;
}

export const PaymentSimulationModal: React.FC<PaymentSimulationModalProps> = ({
  isOpen,
  onClose,
  enrollmentId,
  tenantId,
  amount,
  eventName,
  onPaymentComplete,
}) => {
  const [paymentMethod, setPaymentMethod] = useState('SIMULATED_CARD');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentResult, setPaymentResult] = useState<PaymentResultResponse | null>(null);

  if (!isOpen) return null;

  const handleProcessPayment = async (simulateFailure: boolean) => {
    setIsProcessing(true);
    setPaymentResult(null);

    try {
      const result = await paymentService.processPayment({
        tenantId,
        enrollmentId,
        amount,
        currency: 'USD',
        paymentMethod,
        simulateFailure,
        failureReason: simulateFailure ? 'Tarjeta de crédito rechazada por la entidad bancaria' : undefined,
      });

      setPaymentResult(result);
      if (result.success) {
        onPaymentComplete();
      }
    } catch (err: any) {
      console.error(err);
      setPaymentResult({
        success: false,
        status: 'REJECTED',
        errorMessage: err.response?.data?.message || 'Error de conexión con la pasarela de pago.',
        timestamp: new Date().toISOString(),
      });
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
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
          maxWidth: '520px',
          width: '100%',
          padding: '2rem',
          backgroundColor: 'var(--bg-surface)',
          boxShadow: 'var(--shadow-xl)',
          borderRadius: 'var(--radius-lg)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ padding: '0.5rem', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', borderRadius: 'var(--radius-md)' }}>
              <CreditCard size={24} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Pasarela de Pago Simulada</h3>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Procesamiento Real de Elegibilidad en Backend</span>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem' }}>
            ✕
          </button>
        </div>

        {paymentResult ? (
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            {paymentResult.success ? (
              <>
                <CheckCircle2 size={56} style={{ color: 'var(--success)', margin: '0 auto 1rem' }} />
                <h3 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>¡Pago Confirmado!</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
                  El pago para el evento <strong>{eventName}</strong> fue procesado exitosamente.
                </p>

                <div className="glass-card" style={{ padding: '1rem', textAlign: 'left', marginBottom: '1.5rem', fontSize: '0.813rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <div><strong>Referencia Transacción:</strong> {paymentResult.externalReference}</div>
                  <div><strong>Monto Procesado:</strong> ${amount} USD</div>
                  <div><strong>Estado:</strong> <span className="badge badge-success">{paymentResult.status}</span></div>
                  <div><strong>Fecha/Hora:</strong> {new Date(paymentResult.timestamp).toLocaleString('es-ES')}</div>
                </div>

                <button onClick={onClose} className="btn btn-primary" style={{ width: '100%', padding: '0.75rem' }}>
                  Aceptar y Continuar
                </button>
              </>
            ) : (
              <>
                <XCircle size={56} style={{ color: 'var(--danger)', margin: '0 auto 1rem' }} />
                <h3 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>Pago Rechazado</h3>
                <p style={{ color: 'var(--danger)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
                  {paymentResult.errorMessage || 'No se pudo completar la transacción.'}
                </p>

                <div style={{ display: 'flex', gap: '1rem' }}>
                  <button onClick={() => setPaymentResult(null)} className="btn btn-primary" style={{ flex: 1 }}>
                    Reintentar Pago
                  </button>
                  <button onClick={onClose} className="btn btn-secondary">
                    Cerrar
                  </button>
                </div>
              </>
            )}
          </div>
        ) : (
          <div>
            <div className="glass-card" style={{ padding: '1.25rem', marginBottom: '1.5rem', backgroundColor: 'var(--bg-surface-secondary)' }}>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Resumen de Pago</div>
              <div style={{ fontSize: '1.125rem', fontWeight: 700, margin: '0.25rem 0' }}>{eventName}</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)' }}>${amount} USD</div>
            </div>

            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label">Método de Pago</label>
              <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} className="form-select">
                <option value="SIMULATED_CARD">Tarjeta de Crédito / Débito (Visa/Mastercard)</option>
                <option value="BANK_TRANSFER">Transferencia Bancaria Directa</option>
                <option value="NIUBIZ_SIMULATED">Pasarela Niubiz (Simulado)</option>
                <option value="CULQI_SIMULATED">Pasarela Culqi (Simulado)</option>
                <option value="STRIPE_SIMULATED">Stripe Gateway (Simulado)</option>
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
              <ShieldCheck size={16} className="text-success" />
              <span>Transacción encriptada y procesada vía puerto abstracto PaymentGatewayPort.</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <button
                onClick={() => handleProcessPayment(false)}
                disabled={isProcessing}
                className="btn btn-primary"
                style={{ width: '100%', padding: '0.875rem', fontSize: '1rem' }}
              >
                {isProcessing ? <Spinner size={20} /> : 'Confirmar Pago ($' + amount + ' USD)'}
              </button>

              <button
                onClick={() => handleProcessPayment(true)}
                disabled={isProcessing}
                className="btn btn-secondary"
                style={{ width: '100%', padding: '0.625rem', fontSize: '0.875rem', color: 'var(--danger)' }}
              >
                Rechazar Pago (Simular Error de Tarjeta)
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
