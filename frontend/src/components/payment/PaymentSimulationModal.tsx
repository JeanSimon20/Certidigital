import React, { useState } from 'react';
import { CreditCard, CheckCircle2, XCircle, ShieldCheck, QrCode, Building2, Upload, AlertCircle, Clock } from 'lucide-react';
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
  const [activeTab, setActiveTab] = useState<'YAPE' | 'BANK_TRANSFER' | 'CARD'>('YAPE');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentResult, setPaymentResult] = useState<PaymentResultResponse | null>(null);
  const [isWaitingVerification, setIsWaitingVerification] = useState(false);

  // Form campos Yape / Transferencia
  const [operationNumber, setOperationNumber] = useState('');
  const [voucherBase64, setVoucherBase64] = useState<string | null>(null);
  const [fileName, setFileName] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFileName(file.name);
    setFormError(null);

    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const MAX_WIDTH = 800;
        let width = img.width;
        let height = img.height;

        if (width > MAX_WIDTH) {
          height = Math.round((height * MAX_WIDTH) / width);
          width = MAX_WIDTH;
        }

        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0, width, height);
          const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.75);
          setVoucherBase64(compressedDataUrl);
        } else {
          setVoucherBase64(event.target?.result as string);
        }
      };
      img.src = event.target?.result as string;
    };
    reader.readAsDataURL(file);
  };

  const handleSubmitVoucher = async () => {
    if (!operationNumber.trim()) {
      setFormError('Por favor ingresa el número de operación del pago.');
      return;
    }

    setIsProcessing(true);
    setFormError(null);

    // Mock voucher predeterminado si no sube imagen
    const finalVoucher = voucherBase64 || `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="100"><rect width="200" height="100" fill="%236366f1"/><text x="20" y="50" fill="white" font-family="sans-serif">Voucher YAPE ${operationNumber}</text></svg>`;

    try {
      const result = await paymentService.submitVoucher({
        enrollmentId,
        paymentMethod: activeTab,
        operationNumber: operationNumber.trim(),
        voucherUrl: finalVoucher,
        amount,
      });

      setPaymentResult(result);
      setIsWaitingVerification(true);
      onPaymentComplete();
    } catch (err: any) {
      console.error(err);
      setFormError(err.response?.data?.message || 'Error al enviar comprobante de pago.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleProcessInstantCard = async (simulateFailure: boolean) => {
    setIsProcessing(true);
    setPaymentResult(null);

    try {
      const result = await paymentService.processPayment({
        tenantId,
        enrollmentId,
        amount,
        currency: 'USD',
        paymentMethod: 'SIMULATED_CARD',
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
          maxWidth: '560px',
          width: '100%',
          padding: '2rem',
          backgroundColor: 'var(--bg-surface)',
          boxShadow: 'var(--shadow-xl)',
          borderRadius: 'var(--radius-lg)',
          maxHeight: '90vh',
          overflowY: 'auto',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ padding: '0.5rem', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', borderRadius: 'var(--radius-md)' }}>
              <CreditCard size={24} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Módulo de Pago e Inscripción</h3>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Yape, Transferencia Bancaria o Tarjeta</span>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem' }}>
            ✕
          </button>
        </div>

        {paymentResult ? (
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            {isWaitingVerification ? (
              <>
                <Clock size={56} style={{ color: 'var(--warning)', margin: '0 auto 1rem' }} />
                <h3 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>Comprobante en Verificación</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
                  Tu comprobante de pago para <strong>{eventName}</strong> ha sido enviado exitosamente a la Administración.
                </p>

                <div className="glass-card" style={{ padding: '1rem', textAlign: 'left', marginBottom: '1.5rem', fontSize: '0.813rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <div><strong>Método:</strong> {activeTab === 'YAPE' ? 'Yape / Plin' : 'Transferencia Bancaria'}</div>
                  <div><strong>N° Operación:</strong> {operationNumber}</div>
                  <div><strong>Monto:</strong> ${amount} USD</div>
                  <div><strong>Estado:</strong> <span className="badge badge-warning">⌛ En Verificación por Administración</span></div>
                </div>

                <button onClick={onClose} className="btn btn-primary" style={{ width: '100%', padding: '0.75rem' }}>
                  Entendido y Continuar
                </button>
              </>
            ) : paymentResult.success ? (
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
            <div className="glass-card" style={{ padding: '1rem 1.25rem', marginBottom: '1.25rem', backgroundColor: 'var(--bg-surface-secondary)' }}>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Resumen del Pago</div>
              <div style={{ fontSize: '1.125rem', fontWeight: 700, margin: '0.25rem 0' }}>{eventName}</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)' }}>${amount} USD <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>(S/. {(amount * 3.8).toFixed(2)} Soles aprox)</span></div>
            </div>

            {/* Tabs de Selección de Método */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.5rem', marginBottom: '1.25rem' }}>
              <button
                type="button"
                onClick={() => setActiveTab('YAPE')}
                style={{
                  padding: '0.625rem 0.5rem',
                  borderRadius: 'var(--radius-md)',
                  border: `2px solid ${activeTab === 'YAPE' ? 'var(--primary)' : 'var(--border-color)'}`,
                  backgroundColor: activeTab === 'YAPE' ? 'var(--primary-light)' : 'transparent',
                  color: activeTab === 'YAPE' ? 'var(--primary)' : 'var(--text-main)',
                  fontWeight: 600,
                  fontSize: '0.813rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  cursor: 'pointer',
                }}
              >
                <QrCode size={16} /> Yape / Plin
              </button>

              <button
                type="button"
                onClick={() => setActiveTab('BANK_TRANSFER')}
                style={{
                  padding: '0.625rem 0.5rem',
                  borderRadius: 'var(--radius-md)',
                  border: `2px solid ${activeTab === 'BANK_TRANSFER' ? 'var(--primary)' : 'var(--border-color)'}`,
                  backgroundColor: activeTab === 'BANK_TRANSFER' ? 'var(--primary-light)' : 'transparent',
                  color: activeTab === 'BANK_TRANSFER' ? 'var(--primary)' : 'var(--text-main)',
                  fontWeight: 600,
                  fontSize: '0.813rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  cursor: 'pointer',
                }}
              >
                <Building2 size={16} /> Transferencia BCP
              </button>

              <button
                type="button"
                onClick={() => setActiveTab('CARD')}
                style={{
                  padding: '0.625rem 0.5rem',
                  borderRadius: 'var(--radius-md)',
                  border: `2px solid ${activeTab === 'CARD' ? 'var(--primary)' : 'var(--border-color)'}`,
                  backgroundColor: activeTab === 'CARD' ? 'var(--primary-light)' : 'transparent',
                  color: activeTab === 'CARD' ? 'var(--primary)' : 'var(--text-main)',
                  fontWeight: 600,
                  fontSize: '0.813rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  cursor: 'pointer',
                }}
              >
                <CreditCard size={16} /> Tarjeta / Directo
              </button>
            </div>

            {formError && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--danger-light)', color: 'var(--danger)', marginBottom: '1rem', fontSize: '0.813rem' }}>
                <AlertCircle size={16} /> {formError}
              </div>
            )}

            {/* TAB 1: YAPE / PLIN */}
            {activeTab === 'YAPE' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div className="glass-card" style={{ padding: '1rem', textAlign: 'center', backgroundColor: 'rgba(124, 58, 237, 0.05)', border: '1px dashed var(--primary)' }}>
                  <div style={{ display: 'inline-flex', padding: '0.75rem', backgroundColor: '#fff', borderRadius: 'var(--radius-md)', marginBottom: '0.5rem' }}>
                    <QrCode size={96} style={{ color: '#7c3aed' }} />
                  </div>
                  <div style={{ fontWeight: 700, fontSize: '0.938rem' }}>Yapea al: <span style={{ color: '#7c3aed' }}>987 654 321</span></div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Titular: CertiDigital / Instituto Valle Grande</div>
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Número de Operación Yape / Plin *</label>
                  <input
                    type="text"
                    placeholder="ej. 849201 o N° de Operación"
                    value={operationNumber}
                    onChange={(e) => setOperationNumber(e.target.value)}
                    className="form-input"
                  />
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Adjuntar Foto / Imagen del Voucher (Comprobante)</label>
                  <label
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      padding: '1rem',
                      border: '2px dashed var(--border-color)',
                      borderRadius: 'var(--radius-md)',
                      cursor: 'pointer',
                      backgroundColor: 'var(--bg-surface-secondary)',
                    }}
                  >
                    <Upload size={24} style={{ color: 'var(--primary)', marginBottom: '0.25rem' }} />
                    <span style={{ fontSize: '0.813rem', fontWeight: 600 }}>{fileName || 'Haz clic para seleccionar imagen de voucher'}</span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Formatos: PNG, JPG, WEBP (Máx 5MB)</span>
                    <input type="file" accept="image/*" onChange={handleFileUpload} style={{ display: 'none' }} />
                  </label>
                </div>

                {voucherBase64 && (
                  <div style={{ textAlign: 'center' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Previsualización del Voucher:</span>
                    <img src={voucherBase64} alt="Voucher" style={{ maxHeight: '120px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', margin: '0.5rem auto 0', display: 'block' }} />
                  </div>
                )}

                <button
                  type="button"
                  onClick={handleSubmitVoucher}
                  disabled={isProcessing}
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.875rem', marginTop: '0.5rem' }}
                >
                  {isProcessing ? <Spinner size={20} /> : 'Enviar Comprobante Yape a Verificación'}
                </button>
              </div>
            )}

            {/* TAB 2: TRANSFERENCIA BANCARIA BCP */}
            {activeTab === 'BANK_TRANSFER' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div className="glass-card" style={{ padding: '1rem', fontSize: '0.813rem', backgroundColor: 'var(--bg-surface-secondary)', display: 'flex', flexDirection: 'column', gap: '0.375rem' }}>
                  <div style={{ fontWeight: 700, color: 'var(--primary)' }}>Banco de Crédito del Perú (BCP)</div>
                  <div><strong>Cuenta Corriente Soles:</strong> 193-9876543-0-12</div>
                  <div><strong>CCI (Código Interbancario):</strong> 00219300987654301214</div>
                  <div><strong>Titular:</strong> Instituto Valle Grande S.A.C.</div>
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Número de Operación Bancaria *</label>
                  <input
                    type="text"
                    placeholder="ej. 492019"
                    value={operationNumber}
                    onChange={(e) => setOperationNumber(e.target.value)}
                    className="form-input"
                  />
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Adjuntar Voucher o Comprobante BCP</label>
                  <label
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      padding: '1rem',
                      border: '2px dashed var(--border-color)',
                      borderRadius: 'var(--radius-md)',
                      cursor: 'pointer',
                      backgroundColor: 'var(--bg-surface-secondary)',
                    }}
                  >
                    <Upload size={24} style={{ color: 'var(--primary)', marginBottom: '0.25rem' }} />
                    <span style={{ fontSize: '0.813rem', fontWeight: 600 }}>{fileName || 'Seleccionar comprobante de transferencia'}</span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Formatos: PNG, JPG, WEBP (Máx 5MB)</span>
                    <input type="file" accept="image/*" onChange={handleFileUpload} style={{ display: 'none' }} />
                  </label>
                </div>

                {voucherBase64 && (
                  <div style={{ textAlign: 'center' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Previsualización del Voucher:</span>
                    <img src={voucherBase64} alt="Voucher" style={{ maxHeight: '120px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', margin: '0.5rem auto 0', display: 'block' }} />
                  </div>
                )}

                <button
                  type="button"
                  onClick={handleSubmitVoucher}
                  disabled={isProcessing}
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.875rem', marginTop: '0.5rem' }}
                >
                  {isProcessing ? <Spinner size={20} /> : 'Enviar Comprobante Bancario a Verificación'}
                </button>
              </div>
            )}

            {/* TAB 3: TARJETA / SIMULADO */}
            {activeTab === 'CARD' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <ShieldCheck size={16} className="text-success" />
                  <span>Procesamiento inmediato con aprobación instantánea de prueba.</span>
                </div>

                <button
                  onClick={() => handleProcessInstantCard(false)}
                  disabled={isProcessing}
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.875rem', fontSize: '1rem' }}
                >
                  {isProcessing ? <Spinner size={20} /> : 'Confirmar Pago Instantáneo ($' + amount + ' USD)'}
                </button>

                <button
                  onClick={() => handleProcessInstantCard(true)}
                  disabled={isProcessing}
                  className="btn btn-secondary"
                  style={{ width: '100%', padding: '0.625rem', fontSize: '0.875rem', color: 'var(--danger)' }}
                >
                  Simular Error de Tarjeta Rechazada
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

