import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, UserCheck } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { attendanceService } from '../../features/events/services/attendance.service';
import { Enrollment } from '../../types/enrollment.types';
import { Event } from '../../types/event.types';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';

export const EventEnrollmentsAdminPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // State for Manual Attendance Modal
  const [selectedEnrollmentForAttendance, setSelectedEnrollmentForAttendance] = useState<Enrollment | null>(null);
  const [attendanceSessionId, setAttendanceSessionId] = useState('session-default-01');
  const [attendanceNotes, setAttendanceNotes] = useState('Asistencia registrada manualmente por el organizador');
  const [isRecordingAttendance, setIsRecordingAttendance] = useState(false);
  const [attendanceMessage, setAttendanceMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchEnrollmentData = async () => {
    if (!id) return;
    setIsLoading(true);
    try {
      const [eventData, enrollmentsData] = await Promise.all([
        eventService.getPublicEventDetail(id),
        eventService.getEventEnrollments(id),
      ]);
      setEvent(eventData);
      setEnrollments(enrollmentsData);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollmentData();
  }, [id]);

  const handleRecordAttendanceSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEnrollmentForAttendance) return;

    setIsRecordingAttendance(true);
    setAttendanceMessage(null);

    try {
      await attendanceService.recordAttendance({
        enrollmentId: selectedEnrollmentForAttendance.id,
        sessionId: attendanceSessionId,
        attended: true,
        method: 'MANUAL',
        notes: attendanceNotes,
      });

      setAttendanceMessage({
        type: 'success',
        text: `Asistencia marcada correctamente para ${selectedEnrollmentForAttendance.participantName}.`,
      });

      await fetchEnrollmentData();

      setTimeout(() => {
        setSelectedEnrollmentForAttendance(null);
        setAttendanceMessage(null);
      }, 1500);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al registrar la asistencia.';
      setAttendanceMessage({ type: 'error', text: msg });
    } finally {
      setIsRecordingAttendance(false);
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
        <Spinner size={36} />
      </div>
    );
  }

  return (
    <div>
      <Link to="/events" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
        <ArrowLeft size={16} /> Volver a Gestión de Eventos
      </Link>

      <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '2rem', background: 'linear-gradient(135deg, var(--primary-light), var(--bg-surface))' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Padrón de Inscritos y Registro de Asistencia</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
          Evento: <strong>{event?.name}</strong> | Capacidad: {enrollments.length} / {event?.maxCapacity} participantes
        </p>
      </div>

      {enrollments.length === 0 ? (
        <EmptyState
          title="No hay participantes inscritos"
          description="Actualmente no se han registrado inscripciones en este evento."
        />
      ) : (
        <div className="glass-card" style={{ overflowX: 'auto', padding: 0 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-surface-secondary)' }}>
                <th style={{ padding: '1rem' }}>Participante</th>
                <th style={{ padding: '1rem' }}>Correo Electrónico</th>
                <th style={{ padding: '1rem' }}>Estado Inscripción</th>
                <th style={{ padding: '1rem' }}>Estado Pago</th>
                <th style={{ padding: '1rem' }}>Fecha de Registro</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.map((enr) => (
                <tr key={enr.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: 600 }}>{enr.participantName}</td>
                  <td style={{ padding: '1rem' }}>{enr.participantEmail}</td>
                  <td style={{ padding: '1rem' }}>
                    <span className="badge badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                      <CheckCircle2 size={12} /> {enr.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${enr.paymentStatus === 'COMPLETED' ? 'badge-success' : 'badge-primary'}`}>
                      {enr.paymentStatus}
                    </span>
                  </td>
                  <td style={{ padding: '1rem' }}>{new Date(enr.enrolledAt).toLocaleString('es-ES')}</td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <button
                      onClick={() => {
                        setSelectedEnrollmentForAttendance(enr);
                        setAttendanceSessionId(`session-${event?.id?.substring(0, 8)}-01`);
                      }}
                      className="btn btn-primary"
                      style={{ padding: '0.375rem 0.75rem', fontSize: '0.813rem' }}
                    >
                      <UserCheck size={14} /> Marcar Asistencia
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal de Registro Manual de Asistencia */}
      {selectedEnrollmentForAttendance && (
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
              maxWidth: '480px',
              width: '100%',
              padding: '2rem',
              backgroundColor: 'var(--bg-surface)',
            }}
          >
            <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '0.5rem' }}>
              Registrar Asistencia (Método MANUAL)
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
              Participante: <strong>{selectedEnrollmentForAttendance.participantName}</strong>
            </p>

            {attendanceMessage && (
              <div
                style={{
                  padding: '0.875rem',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '1rem',
                  fontSize: '0.875rem',
                  backgroundColor: attendanceMessage.type === 'success' ? 'var(--success-light)' : 'var(--danger-light)',
                  color: attendanceMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
                }}
              >
                {attendanceMessage.text}
              </div>
            )}

            <form onSubmit={handleRecordAttendanceSubmit}>
              <div className="form-group">
                <label className="form-label">Identificador de Sesión</label>
                <input
                  type="text"
                  value={attendanceSessionId}
                  onChange={(e) => setAttendanceSessionId(e.target.value)}
                  className="form-input"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Notas / Observaciones</label>
                <input
                  type="text"
                  value={attendanceNotes}
                  onChange={(e) => setAttendanceNotes(e.target.value)}
                  className="form-input"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  onClick={() => setSelectedEnrollmentForAttendance(null)}
                  className="btn btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" disabled={isRecordingAttendance} className="btn btn-primary">
                  {isRecordingAttendance ? <Spinner size={18} /> : 'Guardar Asistencia'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
