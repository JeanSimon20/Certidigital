import React, { useEffect, useState } from 'react';
import { Award, CheckCircle2, ChevronRight, Clock, CreditCard, Download, ExternalLink, ShieldCheck, XCircle, AlertCircle, RefreshCw } from 'lucide-react';
import { enrollmentService } from '../../features/events/services/enrollment.service';
import { eligibilityService, EligibilityResultDTO } from '../../features/credentials/services/eligibility.service';
import { Enrollment } from '../../types/enrollment.types';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';
import { PaymentSimulationModal } from '../../components/payment/PaymentSimulationModal';
import { CertificateViewerModal } from '../../components/credentials/CertificateViewerModal';
import { useTenantStore } from '../../store/useTenantStore';

interface EnrichedProcedureItem {
  enrollment: Enrollment;
  eligibilityResult?: EligibilityResultDTO;
  overallStatus: 'EMITIDO' | 'EN EMISIÓN' | 'ELIGIBLE' | 'NO ELEGIBLE' | 'PENDIENTE DE EVALUACIÓN' | 'PENDIENTE DE ASISTENCIA' | 'PENDIENTE DE PAGO' | 'EN PROCESO' | 'REVOCADO';
}

export const MyCredentialsPage: React.FC = () => {
  const { activeTenant } = useTenantStore();
  const [items, setItems] = useState<EnrichedProcedureItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [evaluatingId, setEvaluatingId] = useState<string | null>(null);

  // Modals state
  const [paymentEnrollment, setPaymentEnrollment] = useState<Enrollment | null>(null);
  const [selectedCertificateItem, setSelectedCertificateItem] = useState<EnrichedProcedureItem | null>(null);

  const fetchProcedureData = async () => {
    setIsLoading(true);
    try {
      const enrollments = await enrollmentService.getMyEnrollments();

      const enrichedList: EnrichedProcedureItem[] = await Promise.all(
        enrollments.map(async (enr) => {
          let eligibilityResult: EligibilityResultDTO | undefined;
          try {
            // Attempt to get cached or evaluate eligibility
            const evals = await eligibilityService.getEnrollmentEvaluations(enr.id);
            if (evals && evals.length > 0) {
              const last = evals[evals.length - 1];
              eligibilityResult = {
                enrollmentId: last.enrollmentId,
                policyId: last.policyId,
                status: last.result,
                summaryReason: last.result === 'ELIGIBLE' ? 'Cumple con los requisitos para emisión' : 'Requisitos insuficientes',
                ruleResults: JSON.parse(last.conditionResults || '[]'),
                evaluatedAt: last.evaluatedAt,
              };
            }
          } catch (e) {
            // No evaluation trace yet
          }

          // Determine overall procedure state
          let overallStatus: EnrichedProcedureItem['overallStatus'] = 'EN PROCESO';

          const isPaymentComplete = enr.paymentStatus === 'COMPLETED' || enr.paymentStatus === 'NOT_REQUIRED';
          const attendancePct = enr.attendancePercentage ?? 85.0;

          if (!isPaymentComplete) {
            overallStatus = 'PENDIENTE DE PAGO';
          } else if (attendancePct < 80.0) {
            overallStatus = 'PENDIENTE DE ASISTENCIA';
          } else if (!eligibilityResult) {
            overallStatus = 'PENDIENTE DE EVALUACIÓN';
          } else if (eligibilityResult.status === 'NOT_ELIGIBLE') {
            overallStatus = 'NO ELEGIBLE';
          } else if (eligibilityResult.status === 'ELIGIBLE') {
            // Simular emisión aprobada si es eligible
            overallStatus = enr.eventType === 'DIPLOMA' || enr.eventType === 'WORKSHOP' ? 'EMITIDO' : 'EN EMISIÓN';
          }

          return {
            enrollment: enr,
            eligibilityResult,
            overallStatus,
          };
        })
      );

      setItems(enrichedList);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProcedureData();
  }, []);

  const handleEvaluateEligibilityClick = async (enrollmentId: string) => {
    setEvaluatingId(enrollmentId);
    try {
      await eligibilityService.evaluateEligibility(enrollmentId, 16.0);
      await fetchProcedureData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al evaluar elegibilidad contra el backend.');
    } finally {
      setEvaluatingId(null);
    }
  };

  return (
    <div style={{ maxWidth: '1080px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Mis Certificados y Seguimiento de Trámites</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
          Seguimiento transparente del progreso académico, pago, asistencia, evaluación de elegibilidad y certificados emitidos.
        </p>
      </div>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={36} />
        </div>
      ) : items.length === 0 ? (
        <EmptyState
          title="Sin trámites o certificados registrados"
          description="Aún no tienes trámites de credencialización en curso."
        />
      ) : (
        <div style={{ display: 'grid', gap: '1.75rem' }}>
          {items.map((item) => {
            const enr = item.enrollment;
            const isPaymentDone = enr.paymentStatus === 'COMPLETED' || enr.paymentStatus === 'NOT_REQUIRED';
            const attPct = enr.attendancePercentage ?? 85.0;
            const hasPassedAtt = attPct >= 80.0;
            const isEligible = item.eligibilityResult?.status === 'ELIGIBLE' || item.overallStatus === 'EMITIDO';
            const isEmitted = item.overallStatus === 'EMITIDO';

            return (
              <div
                key={enr.id}
                className="glass-card"
                style={{
                  padding: '1.75rem',
                  backgroundColor: 'var(--bg-surface)',
                  borderRadius: 'var(--radius-lg)',
                }}
              >
                {/* Header Row */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      <span className="badge badge-primary">{enr.eventType}</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        Inscrito el {new Date(enr.enrolledAt).toLocaleDateString('es-ES')}
                      </span>
                    </div>
                    <h3 style={{ fontSize: '1.375rem', fontWeight: 800 }}>{enr.eventName}</h3>
                  </div>

                  {/* Badge de Estado General */}
                  <div>
                    <span
                      className={`badge ${
                        item.overallStatus === 'EMITIDO'
                          ? 'badge-success'
                          : item.overallStatus === 'NO ELEGIBLE' || item.overallStatus === 'REVOCADO'
                          ? 'badge-danger'
                          : item.overallStatus === 'EN EMISIÓN' || item.overallStatus === 'ELIGIBLE'
                          ? 'badge-primary'
                          : 'badge-warning'
                      }`}
                      style={{ padding: '0.5rem 1rem', fontSize: '0.875rem', fontWeight: 700 }}
                    >
                      {item.overallStatus}
                    </span>
                  </div>
                </div>

                {/* TIMELINE VISUAL DE 7 PASOS */}
                <div style={{ margin: '1.5rem 0', padding: '1.25rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '0.813rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '1rem', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                    Línea de Tiempo del Trámite Académico
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '0.75rem', textAlign: 'center' }}>
                    {/* 1. Inscripción */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: '3px solid var(--success)' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>1. Inscripción</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--success)', marginTop: '0.25rem' }}>
                        ✓ Confirmada
                      </div>
                    </div>

                    {/* 2. Pago */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: `3px solid ${isPaymentDone ? 'var(--success)' : 'var(--warning)'}` }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>2. Pago</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: isPaymentDone ? 'var(--success)' : 'var(--warning)', marginTop: '0.25rem' }}>
                        {isPaymentDone ? '✓ Confirmado' : '⏳ Pendiente'}
                      </div>
                    </div>

                    {/* 3. Asistencia */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: `3px solid ${hasPassedAtt ? 'var(--success)' : 'var(--warning)'}` }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>3. Asistencia</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: hasPassedAtt ? 'var(--success)' : 'var(--warning)', marginTop: '0.25rem' }}>
                        {hasPassedAtt ? `✓ ${attPct}%` : `⏳ ${attPct}%`}
                      </div>
                    </div>

                    {/* 4. Evaluación */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: '3px solid var(--success)' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>4. Evaluación</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--success)', marginTop: '0.25rem' }}>
                        ✓ Nota 16.00
                      </div>
                    </div>

                    {/* 5. Elegibilidad */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: `3px solid ${isEligible ? 'var(--success)' : 'var(--warning)'}` }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>5. Elegibilidad</div>
                      <div style={{ fontSize: '0.875rem', fontWeight 700, color: isEligible ? 'var(--success)' : 'var(--warning)', marginTop: '0.25rem' }}>
                        {isEligible ? '✓ Elegible' : item.eligibilityResult?.status === 'NOT_ELIGIBLE' ? '❌ No Elegible' : '⏳ Pendiente'}
                      </div>
                    </div>

                    {/* 6. Emisión */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: `3px solid ${isEmitted ? 'var(--success)' : 'var(--primary)'}` }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>6. Emisión</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: isEmitted ? 'var(--success)' : 'var(--primary)', marginTop: '0.25rem' }}>
                        {isEmitted ? '✓ Aprobado' : '⏳ En Emisión'}
                      </div>
                    </div>

                    {/* 7. Certificado */}
                    <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-sm)', borderLeft: `3px solid ${isEmitted ? 'var(--success)' : 'var(--border-color)'}` }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>7. Certificado</div>
                      <div style={{ fontSize: '0.875rem', fontWeight: 700, color: isEmitted ? 'var(--success)' : 'var(--text-muted)', marginTop: '0.25rem' }}>
                        {isEmitted ? '✓ Emitido' : '○ Pendiente'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Detalle de Elegibilidad y Trazabilidad (Backend Authority) */}
                {item.eligibilityResult && (
                  <div style={{ marginBottom: '1.5rem', fontSize: '0.813rem' }}>
                    <div style={{ fontWeight: 700, marginBottom: '0.5rem', color: 'var(--text-main)' }}>
                      Trazabilidad del Motor de Elegibilidad (Backend Authority):
                    </div>
                    <div style={{ display: 'grid', gap: '0.5rem' }}>
                      {item.eligibilityResult.ruleResults.map((r, i) => (
                        <div
                          key={i}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '0.5rem 0.75rem',
                            borderRadius: 'var(--radius-sm)',
                            backgroundColor: r.passed ? 'var(--success-light)' : 'var(--danger-light)',
                            color: r.passed ? 'var(--success)' : 'var(--danger)',
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            {r.passed ? <CheckCircle2 size={16} /> : <XCircle size={16} />}
                            <strong>{r.ruleName}:</strong> {r.reason}
                          </div>
                          <div style={{ fontFamily: 'monospace', fontWeight: 600 }}>
                            Obtenido: {r.obtainedValue} (Esperado: {r.expectedValue})
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Bottom Action Toolbar */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', flexWrap: 'wrap', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                  {!isPaymentDone && (
                    <button onClick={() => setPaymentEnrollment(enr)} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                      <CreditCard size={16} /> Simular Pago ($100 USD)
                    </button>
                  )}

                  <button
                    onClick={() => handleEvaluateEligibilityClick(enr.id)}
                    disabled={evaluatingId === enr.id}
                    className="btn btn-secondary"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
                  >
                    {evaluatingId === enr.id ? <Spinner size={16} /> : <RefreshCw size={16} />}
                    Evaluar Elegibilidad (Real API)
                  </button>

                  {isEmitted && (
                    <>
                      <button
                        onClick={() => setSelectedCertificateItem(item)}
                        className="btn btn-primary"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
                      >
                        <Award size={16} /> Ver Certificado
                      </button>
                      <button
                        onClick={() => alert(`Descargando certificado oficial PDF para ${enr.participantName}...`)}
                        className="btn btn-secondary"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
                      >
                        <Download size={16} /> Descargar PDF
                      </button>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal Simulación de Pago Real Backend */}
      {paymentEnrollment && (
        <PaymentSimulationModal
          isOpen={!!paymentEnrollment}
          onClose={() => setPaymentEnrollment(null)}
          enrollmentId={paymentEnrollment.id}
          tenantId={paymentEnrollment.tenantId || activeTenant?.tenantId || 'tenant-001-aaaa-bbbb-cccc-dddddddd'}
          amount={100.0}
          eventName={paymentEnrollment.eventName}
          onPaymentComplete={() => {
            fetchProcedureData();
          }}
        />
      )}

      {/* Modal Ver Certificado Oficial */}
      {selectedCertificateItem && (
        <CertificateViewerModal
          isOpen={!!selectedCertificateItem}
          onClose={() => setSelectedCertificateItem(null)}
          participantName={selectedCertificateItem.enrollment.participantName}
          eventName={selectedCertificateItem.enrollment.eventName}
          eventType={selectedCertificateItem.enrollment.eventType}
          issuedDate={new Date().toISOString()}
          certificateId={`CERT-${selectedCertificateItem.enrollment.id.substring(0, 12)}`}
        />
      )}
    </div>
  );
};
