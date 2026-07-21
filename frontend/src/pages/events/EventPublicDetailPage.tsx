import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Calendar, MapPin, Video, Users, Building2, CheckCircle2, ArrowLeft, AlertCircle } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { enrollmentService } from '../../features/events/services/enrollment.service';
import { Event } from '../../types/event.types';
import { useAuthStore } from '../../store/useAuthStore';
import { Spinner } from '../../components/feedback/Spinner';

export const EventPublicDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();

  const [event, setEvent] = useState<Event | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEnrolling, setIsEnrolling] = useState(false);
  const [enrollSuccess, setEnrollSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      eventService
        .getPublicEventDetail(id)
        .then(setEvent)
        .catch((err) => {
          console.error(err);
          setErrorMessage('No se pudo obtener la información del evento.');
        })
        .finally(() => setIsLoading(false));
    }
  }, [id]);

  const handleEnroll = async () => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=/events/catalog/${id}`);
      return;
    }

    if (!id) return;

    setIsEnrolling(true);
    setErrorMessage(null);

    try {
      await enrollmentService.enrollParticipant({ eventId: id });
      setEnrollSuccess(true);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Ocurrió un error al procesar la inscripción.';
      setErrorMessage(msg);
    } finally {
      setIsEnrolling(false);
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '5rem' }}>
        <Spinner size={36} />
      </div>
    );
  }

  if (!event) {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center' }}>
        <h2>Evento no encontrado</h2>
        <Link to="/events/catalog" className="btn btn-primary" style={{ marginTop: '1rem' }}>
          Volver al Catálogo
        </Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <Link to="/events/catalog" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
        <ArrowLeft size={16} /> Volver al Catálogo
      </Link>

      {errorMessage && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '1rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--danger-light)',
            color: 'var(--danger)',
            marginBottom: '1.5rem',
          }}
        >
          <AlertCircle size={20} />
          <span>{errorMessage}</span>
        </div>
      )}

      {enrollSuccess ? (
        <div
          className="glass-card"
          style={{
            padding: '3rem 2rem',
            textAlign: 'center',
            background: 'linear-gradient(135deg, var(--success-light), var(--bg-surface))',
          }}
        >
          <CheckCircle2 size={56} style={{ color: 'var(--success)', marginBottom: '1rem', margin: '0 auto' }} />
          <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem' }}>¡Inscripción Exitosa!</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '500px', margin: '0 auto 2rem' }}>
            Tu inscripción al evento <strong>{event.name}</strong> ha sido confirmada en CertiDigital.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
            <Link to="/my-enrollments" className="btn btn-primary">
              Ir a Mis Inscripciones
            </Link>
            <Link to="/events/catalog" className="btn btn-secondary">
              Explorar Más Eventos
            </Link>
          </div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
          {/* Main Detail Info */}
          <div>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <span className="badge badge-primary">{event.eventType}</span>
              <span className="badge badge-success">{event.mode}</span>
            </div>

            <h1 style={{ fontSize: '2.25rem', fontWeight: 800, marginBottom: '0.75rem', lineHeight: 1.25 }}>
              {event.name}
            </h1>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary)', fontWeight: 600, marginBottom: '2rem' }}>
              <Building2 size={18} />
              <span>{event.tenantName}</span>
            </div>

            <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '2rem' }}>
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.75rem' }}>Descripción del Evento</h3>
              <p style={{ color: 'var(--text-main)', lineHeight: 1.7, whiteSpace: 'pre-line' }}>
                {event.description}
              </p>
            </div>
          </div>

          {/* Sidebar Action Card */}
          <div>
            <div className="glass-card" style={{ padding: '1.75rem', position: 'sticky', top: '90px' }}>
              <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem', marginBottom: '1.25rem' }}>
                <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Costo de Inscripción</span>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--primary)' }}>
                  {event.price === 0 ? 'Gratuito' : `$${event.price}`}
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem', marginBottom: '1.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <Calendar size={18} className="text-primary" />
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>Fecha de Inicio</div>
                    <div>{new Date(event.startDate).toLocaleString('es-ES')}</div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  {event.mode === 'VIRTUAL' ? <Video size={18} /> : <MapPin size={18} />}
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>Ubicación / Modalidad</div>
                    <div>{event.locationName || event.virtualUrl || 'Modalidad ' + event.mode}</div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <Users size={18} />
                  <div>
                    <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>Cupos Disponibles</div>
                    <div>{event.maxCapacity - event.enrolledCount} de {event.maxCapacity} vacantes</div>
                  </div>
                </div>
              </div>

              <button
                onClick={handleEnroll}
                disabled={isEnrolling || event.enrolledCount >= event.maxCapacity}
                className="btn btn-primary"
                style={{ width: '100%', padding: '0.875rem', fontSize: '1rem' }}
              >
                {isEnrolling ? <Spinner size={20} /> : 'Inscribirme Ahora'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
