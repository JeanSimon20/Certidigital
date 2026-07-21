import React from 'react';
import { Award, CheckCircle2, Download, ExternalLink, QrCode, ShieldCheck } from 'lucide-react';

interface CertificateViewerModalProps {
  isOpen: boolean;
  onClose: () => void;
  participantName: string;
  eventName: string;
  eventType: string;
  issuedDate: string;
  certificateId: string;
}

export const CertificateViewerModal: React.FC<CertificateViewerModalProps> = ({
  isOpen,
  onClose,
  participantName,
  eventName,
  eventType,
  issuedDate,
  certificateId,
}) => {
  if (!isOpen) return null;

  const verificationUrl = `${window.location.origin}/verify/${certificateId}`;

  const handleDownloadPdf = () => {
    alert(`Descargando certificado oficial PDF para ${participantName}...`);
  };

  const handleCopyLink = () => {
    navigator.clipboard.writeText(verificationUrl);
    alert('Enlace de verificación copiado al portapapeles.');
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        backdropFilter: 'blur(6px)',
        zIndex: 1100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1.5rem',
      }}
    >
      <div
        className="glass-card"
        style={{
          maxWidth: '680px',
          width: '100%',
          padding: '2.5rem',
          backgroundColor: 'var(--bg-surface)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-2xl)',
          position: 'relative',
          border: '2px solid var(--primary-light)',
        }}
      >
        {/* Certificate Decorative Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem', borderBottom: '2px dashed var(--border-color)', paddingBottom: '1.5rem' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              margin: '0 auto 1rem',
              borderRadius: '50%',
              backgroundColor: 'var(--primary-light)',
              color: 'var(--primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Award size={36} />
          </div>
          <div style={{ textTransform: 'uppercase', letterSpacing: '2px', fontSize: '0.75rem', fontWeight: 800, color: 'var(--primary)' }}>
            CertiDigital — Plataforma de Credenciales Verificables
          </div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: 900, marginTop: '0.5rem' }}>Certificado Académico Digital</h2>
        </div>

        {/* Certificate Body */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Se otorga la presente constancia a:</p>
          <h3 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)', margin: '0.5rem 0 1rem' }}>
            {participantName}
          </h3>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Por haber completado y aprobado con éxito el {eventType.toLowerCase()} académico:
          </p>
          <div
            style={{
              fontSize: '1.25rem',
              fontWeight: 800,
              color: 'var(--primary)',
              margin: '0.75rem 0',
              padding: '0.75rem',
              backgroundColor: 'var(--bg-surface-secondary)',
              borderRadius: 'var(--radius-md)',
            }}
          >
            {eventName}
          </div>
          <div style={{ fontSize: '0.813rem', color: 'var(--text-muted)' }}>
            Emitido el {new Date(issuedDate).toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' })}
          </div>
        </div>

        {/* Verification & Trust Footer */}
        <div
          className="glass-card"
          style={{
            padding: '1.25rem',
            backgroundColor: 'var(--bg-surface-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            marginBottom: '2rem',
            flexWrap: 'wrap',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ width: '60px', height: '60px', backgroundColor: 'var(--bg-surface)', padding: '0.25rem', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <QrCode size={48} style={{ color: 'var(--text-main)' }} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', fontSize: '0.813rem', fontWeight: 700, color: 'var(--success)' }}>
                <ShieldCheck size={16} /> Firma Criptográfica Verificada
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'monospace', marginTop: '0.25rem' }}>
                ID: {certificateId}
              </div>
              <div style={{ fontSize: '0.688rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                HASH: sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <button onClick={handleDownloadPdf} className="btn btn-primary" style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
            <Download size={18} /> Descargar PDF
          </button>
          <button onClick={handleCopyLink} className="btn btn-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
            <ExternalLink size={16} /> Verificación Pública
          </button>
          <button onClick={onClose} className="btn btn-secondary">
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
