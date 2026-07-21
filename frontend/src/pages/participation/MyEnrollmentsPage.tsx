import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, CheckCircle2, ChevronRight, CreditCard } from 'lucide-react';
import { enrollmentService } from '../../features/events/services/enrollment.service';
import { Enrollment } from '../../types/enrollment.types';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';
import { PaymentSimulationModal } from '../../components/payment/PaymentSimulationModal';
import { useTenantStore } from '../../store/useTenantStore';

export const MyEnrollmentsPage: React.FC = () => {
  const { activeTenant } = useTenantStore();
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal State for Payment Simulation
  const [selectedEnrollmentForPayment, setSelectedEnrollmentForPayment] = useState<Enrollment | null>(null);

  const fetchEnrollments = async () => {
    setIsLoading(true);
    try {
      const data = await enrollmentService.getMyEnrollments();
      setEnrollments(data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollments();
  }, []);

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Mis Inscripciones y Estado de Pago</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          Consulta el estado de tus inscripciones, completa el pago simulado y revisa tu asistencia.
        </p>
      </div>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={36} />
        </div>
      ) : enrollments.length === 0 ? (
        <EmptyState
          title="Sin inscripciones registradas"
          description="Aún no te has inscrito en ningún evento académico."
          action={
            <Link to="/events/catalog" className="btn btn-primary">
              Explorar Catálogo de Eventos
            </Link>
          }
        />
      ) : (
        <div style={{ display: 'grid', gap: '1.25rem' }}>
          {enrollments.map((enr) => (
            <div
              key={enr.id}
              className="glass-card"
              style={{
                padding: '1.5rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
                <div
                  style={{
                    width: '48px',
                    height: '48px',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--primary-light)',
                    color: 'var(--primary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Calendar size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>{enr.eventName}</h3>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    <span className="badge badge-primary">{enr.eventType}</span>

                    {/* Badge Estado Inscripción */}
                    <span className={`badge ${enr.status === 'CONFIRMED' ? 'badge-success' : 'badge-warning'}`}>
                      <CheckCircle2 size={12} /> {enr.status === 'CONFIRMED' ? 'Inscripción Confirmada' : enr.status}
                    </span>

                    {/* Badge Estado Pago */}
                    <span
                      className={`badge ${
                        enr.paymentStatus === 'COMPLETED'
                          ? 'badge-success'
                          : enr.paymentStatus === 'PENDING' || enr.paymentStatus === 'PENDING_PAYMENT'
                          ? 'badge-warning'
                          : 'badge-primary'
                      }`}
                    >
                      {enr.paymentStatus === 'COMPLETED' ? (
                        'Pago Confirmado'
                      ) : enr.paymentStatus === 'PENDING' || enr.paymentStatus === 'PENDING_PAYMENT' ? (
                        'Pendiente de Pago'
                      ) : enr.paymentStatus === 'FAILED' ? (
                        'Pago Rechazado'
                      ) : (
                        'Sin Costo'
                      )}
                    </span>
                  </div>

                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                    Inscrito el {new Date(enr.enrolledAt).toLocaleDateString('es-ES')}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {(enr.paymentStatus === 'PENDING' || enr.paymentStatus === 'PENDING_PAYMENT' || enr.paymentStatus === 'FAILED') && (
                  <button
                    onClick={() => setSelectedEnrollmentForPayment(enr)}
                    className="btn btn-primary"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
                  >
                    <CreditCard size={16} /> Simular Pago ($100 USD)
                  </button>
                )}

                <Link to={`/events/catalog/${enr.eventId}`} className="btn btn-secondary">
                  Ver Detalle <ChevronRight size={16} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal de Simulación de Pago Real Backend */}
      {selectedEnrollmentForPayment && (
        <PaymentSimulationModal
          isOpen={!!selectedEnrollmentForPayment}
          onClose={() => setSelectedEnrollmentForPayment(null)}
          enrollmentId={selectedEnrollmentForPayment.id}
          tenantId={selectedEnrollmentForPayment.tenantId || activeTenant?.tenantId || 'tenant-001-aaaa-bbbb-cccc-dddddddd'}
          amount={100.0}
          eventName={selectedEnrollmentForPayment.eventName}
          onPaymentComplete={() => {
            fetchEnrollments();
          }}
        />
      )}
    </div>
  );
};
