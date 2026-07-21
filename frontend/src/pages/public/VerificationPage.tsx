import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ShieldCheck, XCircle, Search, Award, Calendar, Building, BookOpen, QrCode, ExternalLink, CheckCircle2, AlertTriangle, HelpCircle } from 'lucide-react';
import { verificationService, CredentialVerificationResultDTO } from '../../features/credentials/services/verification.service';
import { Spinner } from '../../components/feedback/Spinner';

export const VerificationPage: React.FC = () => {
  const { codeOrHash: routeParam } = useParams<{ codeOrHash?: string }>();
  const navigate = useNavigate();

  const [searchInput, setSearchInput] = useState(routeParam || '');
  const [result, setResult] = useState<CredentialVerificationResultDTO | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [showQrSim, setShowQrSim] = useState(false);

  const performVerification = async (code: string) => {
    if (!code.trim()) return;
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const data = await verificationService.verifyCredential(code.trim());
      setResult(data);
    } catch (err: any) {
      console.error(err);
      setErrorMsg('No se pudo conectar con el servicio de verificación oficial.');
      setResult({
        status: 'NOT_FOUND',
        isValid: false,
        publicCode: code,
        message: 'Credencial no encontrada o no registrada en el sistema.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (routeParam) {
      setSearchInput(routeParam);
      performVerification(routeParam);
    }
  }, [routeParam]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      navigate(`/verify/${encodeURIComponent(searchInput.trim())}`);
      performVerification(searchInput.trim());
    }
  };

  return (
    <div style={{ maxWidth: '840px', margin: '2rem auto', padding: '0 1rem' }}>
      {/* Header */}
      <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
        <div
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.375rem 1rem',
            borderRadius: '999px',
            backgroundColor: 'var(--primary-light)',
            color: 'var(--primary)',
            fontSize: '0.813rem',
            fontWeight: 800,
            textTransform: 'uppercase',
            letterSpacing: '1px',
            marginBottom: '1rem',
          }}
        >
          <ShieldCheck size={18} /> Portal Público de Verificación
        </div>
        <h1 style={{ fontSize: '2.25rem', fontWeight: 900 }}>Verificación de Credenciales Digitales</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.938rem', marginTop: '0.5rem', maxWidth: '600px', margin: '0.5rem auto 0' }}>
          Consulta la autenticidad e integridad de certificados digitales emitidos en CertiDigital mediante su código único o hash SHA-256.
        </p>
      </div>

      {/* Search & QR Form */}
      <div
        className="glass-card"
        style={{
          padding: '1.75rem',
          backgroundColor: 'var(--bg-surface)',
          borderRadius: 'var(--radius-lg)',
          marginBottom: '2rem',
          boxShadow: 'var(--shadow-lg)',
        }}
      >
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, position: 'relative', minWidth: '260px' }}>
            <input
              type="text"
              className="input-field"
              placeholder="Ingresa el Código (ej: CDIG-2026-X8K9L2M1) o Hash SHA-256"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              style={{ width: '100%', paddingLeft: '2.75rem' }}
            />
            <Search
              size={18}
              style={{
                position: 'absolute',
                left: '1rem',
                top: '50%',
                transform: 'translateY(-50%)',
                color: 'var(--text-muted)',
              }}
            />
          </div>
          <button type="submit" disabled={isLoading} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
            {isLoading ? <Spinner size={18} /> : <Search size={18} />} Verificar
          </button>
          <button
            type="button"
            onClick={() => setShowQrSim(!showQrSim)}
            className="btn btn-secondary"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
          >
            <QrCode size={18} /> Escanear QR
          </button>
        </form>

        {/* Modal/Box Escáner QR Simulador */}
        {showQrSim && (
          <div
            style={{
              marginTop: '1.25rem',
              padding: '1.25rem',
              backgroundColor: 'var(--bg-surface-secondary)',
              borderRadius: 'var(--radius-md)',
              textAlign: 'center',
              border: '2px dashed var(--primary)',
            }}
          >
            <QrCode size={48} style={{ color: 'var(--primary)', marginBottom: '0.5rem' }} />
            <div style={{ fontWeight: '700', fontSize: '0.875rem' }}>Simulador de Escaneo QR</div>
            <p style={{ fontSize: '0.813rem', color: 'var(--text-muted)', margin: '0.25rem 0 1rem' }}>
              En un dispositivo móvil, enfocar el código QR redirigirá automáticamente a esta pantalla de verificación.
            </p>
            <button
              onClick={() => {
                const sampleCode = 'CDIG-2026-DEMO0001';
                setSearchInput(sampleCode);
                setShowQrSim(false);
                performVerification(sampleCode);
              }}
              className="btn btn-secondary"
              style={{ fontSize: '0.813rem' }}
            >
              Simular Lectura de QR (CDIG-2026-DEMO0001)
            </button>
          </div>
        )}
      </div>

      {/* Loading State */}
      {isLoading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={40} />
        </div>
      )}

      {/* Result Display */}
      {!isLoading && result && (
        <div
          className="glass-card"
          style={{
            padding: '2.25rem',
            backgroundColor: 'var(--bg-surface)',
            borderRadius: 'var(--radius-lg)',
            borderTop: `6px solid ${
              result.status === 'VALID'
                ? 'var(--success)'
                : result.status === 'REVOKED'
                ? 'var(--danger)'
                : 'var(--border-color)'
            }`,
            boxShadow: 'var(--shadow-xl)',
          }}
        >
          {/* Status Banner */}
          <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
            {result.status === 'VALID' && (
              <div style={{ color: 'var(--success)' }}>
                <ShieldCheck size={56} style={{ margin: '0 auto 0.75rem' }} />
                <h2 style={{ fontSize: '1.75rem', fontWeight: 900, textTransform: 'uppercase' }}>
                  CREDENCIAL VÁLIDA
                </h2>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Esta credencial es auténtica, está activa y se encuentra verificada formalmente en la infraestructura CertiDigital.
                </p>
              </div>
            )}

            {result.status === 'REVOKED' && (
              <div style={{ color: 'var(--danger)' }}>
                <AlertTriangle size={56} style={{ margin: '0 auto 0.75rem' }} />
                <h2 style={{ fontSize: '1.75rem', fontWeight: 900, textTransform: 'uppercase' }}>
                  CREDENCIAL REVOCADA
                </h2>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Esta credencial fue REVOCADA formalmente por la institución emisora y carece de validez.
                </p>
              </div>
            )}

            {(result.status === 'NOT_FOUND' || !result.isValid && result.status !== 'REVOKED') && (
              <div style={{ color: 'var(--text-muted)' }}>
                <HelpCircle size={56} style={{ margin: '0 auto 0.75rem' }} />
                <h2 style={{ fontSize: '1.75rem', fontWeight: 900, textTransform: 'uppercase' }}>
                  CREDENCIAL NO ENCONTRADA
                </h2>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  No se hallaron registros oficiales asociados al identificador proporcionado.
                </p>
              </div>
            )}
          </div>

          {/* Details Table */}
          {result.status !== 'NOT_FOUND' && (
            <div style={{ display: 'grid', gap: '1.25rem', marginBottom: '2rem' }}>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                  gap: '1.25rem',
                  padding: '1.25rem',
                  backgroundColor: 'var(--bg-surface-secondary)',
                  borderRadius: 'var(--radius-md)',
                }}
              >
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Titular Acreditado
                  </div>
                  <div style={{ fontSize: '1.125rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '0.25rem' }}>
                    {result.participantName || 'N/A'}
                  </div>
                  {result.participantDocMasked && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Doc: {result.participantDocMasked}</div>
                  )}
                </div>

                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Institución Emisora
                  </div>
                  <div style={{ fontSize: '1.125rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '0.25rem' }}>
                    {result.issuerName || 'CertiDigital Authority'}
                  </div>
                </div>

                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Evento Académico
                  </div>
                  <div style={{ fontSize: '1.125rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.25rem' }}>
                    {result.eventName || 'N/A'}
                  </div>
                  {result.eventType && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Tipo: {result.eventType}</div>
                  )}
                </div>

                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Fecha de Emisión
                  </div>
                  <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '0.25rem' }}>
                    {result.issuedAt ? new Date(result.issuedAt).toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' }) : 'N/A'}
                  </div>
                </div>
              </div>

              {/* Revocation Specific Info */}
              {result.status === 'REVOKED' && (
                <div
                  style={{
                    padding: '1.25rem',
                    backgroundColor: 'var(--danger-light)',
                    borderRadius: 'var(--radius-md)',
                    color: 'var(--danger)',
                  }}
                >
                  <div style={{ fontWeight: 800, marginBottom: '0.25rem' }}>Detalle de Revocación:</div>
                  <div><strong>Motivo:</strong> {result.revocationReason || 'Revocación por administración'}</div>
                  {result.revokedAt && <div><strong>Fecha de Revocación:</strong> {new Date(result.revokedAt).toLocaleString('es-ES')}</div>}
                </div>
              )}

              {/* Blockchain & Hash Cryptographic Verification Panel */}
              <div
                style={{
                  padding: '1.25rem',
                  backgroundColor: 'var(--bg-surface-secondary)',
                  borderRadius: 'var(--radius-md)',
                  borderLeft: '4px solid var(--primary)',
                }}
              >
                <div style={{ fontSize: '0.813rem', fontWeight: 800, color: 'var(--text-main)', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <ShieldCheck size={18} style={{ color: 'var(--primary)' }} /> Prueba de Integridad Criptográfica y Anclaje Blockchain
                </div>

                <div style={{ display: 'grid', gap: '0.5rem', fontSize: '0.813rem', fontFamily: 'monospace' }}>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Código Público:</span> <strong>{result.publicCode}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Hash SHA-256:</span> <span style={{ wordBreak: 'break-all', color: 'var(--primary)' }}>{result.contentHash}</span>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Red Blockchain:</span> <strong>{result.blockchainNetwork || 'SIMULATOR'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Transaction ID:</span> <span style={{ wordBreak: 'break-all', color: 'var(--success)' }}>{result.blockchainTxId || '0x...'}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Privacy Note */}
          <div style={{ textAlign: 'center', fontSize: '0.75rem', color: 'var(--text-muted)', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
            🔒 <strong>Seguridad y Privacidad:</strong> Este portal público solo expone la validez formal del certificado y datos académicos del título. No se almacena ni revela información sensible o personal del usuario.
          </div>
        </div>
      )}
    </div>
  );
};
