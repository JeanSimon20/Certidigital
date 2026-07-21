import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, UserCheck, Calendar, Award, FileCheck2 } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { attendanceService } from '../../features/events/services/attendance.service';
import { evaluationService } from '../../features/events/services/evaluation.service';
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
  const [attendanceNotes, setAttendanceNotes] = useState('Asistencia registrada manualmente por el docente');
  const [isRecordingAttendance, setIsRecordingAttendance] = useState(false);
  const [attendanceMessage, setAttendanceMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // State for Grade Registration Modal
  const [selectedEnrollmentForGrade, setSelectedEnrollmentForGrade] = useState<Enrollment | null>(null);
  const [evaluationName, setEvaluationName] = useState('Examen Final / Evaluación General');
  const [score, setScore] = useState('17.0');
  const [isRecordingGrade, setIsRecordingGrade] = useState(false);
  const [gradeMessage, setGradeMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

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

  const handleRecordGradeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEnrollmentForGrade) return;

    setIsRecordingGrade(true);
    setGradeMessage(null);

    try {
      await evaluationService.recordEvaluation({
        enrollmentId: selectedEnrollmentForGrade.id,
        evaluationName,
        evaluationType: 'EXAM',
        score: parseFloat(score),
        maxScore: 20.0,
        passingScore: 14.0,
      });

      setGradeMessage({
        type: 'success',
        text: `Nota (${score}/20) registrada correctamente para ${selectedEnrollmentForGrade.participantName}.`,
      });

      await fetchEnrollmentData();

      setTimeout(() => {
        setSelectedEnrollmentForGrade(null);
        setGradeMessage(null);
      }, 1500);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al registrar la calificación.';
      setGradeMessage({ type: 'error', text: msg });
    } finally {
      setIsRecordingGrade(false);
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
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Padrón de Inscritos, Asistencia y Calificaciones</h1>
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
                <th style={{ padding: '1rem' }}>Inscripción</th>
                <th style={{ padding: '1rem' }}>Pago</th>
                <th style={{ padding: '1rem' }}>Asistencia (%)</th>
                <th style={{ padding: '1rem' }}>Calificación</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Acciones Docentes</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.map((enr) => {
                const hasAttendance = enr.attendancePercentage !== undefined && enr.attendancePercentage > 0;
                const hasScore = enr.finalScore !== undefined && enr.finalScore > 0;
                return (
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
                    <td style={{ padding: '1rem' }}>
                      <span className={`badge ${hasAttendance ? 'badge-success' : 'badge-warning'}`}>
                        {hasAttendance ? `✓ ${enr.attendancePercentage}%` : `⏳ 0% Registrada`}
                      </span>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span className={`badge ${hasScore ? 'badge-success' : 'badge-warning'}`}>
                        {hasScore ? `✓ Nota: ${enr.finalScore}/20` : `⏳ Sin Calificación`}
                      </span>
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem', alignItems: 'center' }}>
                        {hasAttendance ? (
                          <span className="badge badge-success" style={{ fontSize: '0.75rem', padding: '0.375rem 0.625rem' }}>
                            <CheckCircle2 size={12} /> Asistencia Registrada
                          </span>
                        ) : (
                          <button
                            onClick={() => {
                              setSelectedEnrollmentForAttendance(enr);
                              setAttendanceSessionId(`session-${event?.id?.substring(0, 8)}-01`);
                            }}
                            className="btn btn-primary"
                            style={{ padding: '0.375rem 0.625rem', fontSize: '0.75rem' }}
                          >
                            <UserCheck size={14} /> Marcar Asistencia
                          </button>
                        )}

                        <button
                          onClick={() => {
                            setSelectedEnrollmentForGrade(enr);
                            setScore(enr.finalScore && enr.finalScore > 0 ? String(enr.finalScore) : '17.0');
                          }}
                          className={hasScore ? 'btn btn-secondary' : 'btn btn-primary'}
                          style={{ padding: '0.375rem 0.625rem', fontSize: '0.75rem' }}
                        >
                          <FileCheck2 size={14} /> {hasScore ? 'Editar Nota' : 'Registrar Nota'}
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
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
              <div className="form-group" style={{ marginBottom: '1.25rem' }}>
                <label className="form-label">Sesión de Clase</label>
                <div
                  style={{
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--bg-surface-secondary)',
                    fontSize: '0.875rem',
                    fontWeight: 600,
                    color: 'var(--primary)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                  }}
                >
                  <Calendar size={16} /> Sesión Principal — Asistencia Presencial / Virtual
                </div>
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

      {/* Modal de Registro de Calificaciones / Notas */}
      {selectedEnrollmentForGrade && (
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
              Registrar Calificación Académica
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
              Participante: <strong>{selectedEnrollmentForGrade.participantName}</strong>
            </p>

            {gradeMessage && (
              <div
                style={{
                  padding: '0.875rem',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '1rem',
                  fontSize: '0.875rem',
                  backgroundColor: gradeMessage.type === 'success' ? 'var(--success-light)' : 'var(--danger-light)',
                  color: gradeMessage.type === 'success' ? 'var(--success)' : 'var(--danger)',
                }}
              >
                {gradeMessage.text}
              </div>
            )}

            <form onSubmit={handleRecordGradeSubmit}>
              <div className="form-group">
                <label className="form-label">Nombre de Evaluación</label>
                <input
                  type="text"
                  value={evaluationName}
                  onChange={(e) => setEvaluationName(e.target.value)}
                  className="form-input"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Nota Final (Escala 0 - 20, Aprobatorio &gt;= 14.0)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="20"
                  value={score}
                  onChange={(e) => setScore(e.target.value)}
                  className="form-input"
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  onClick={() => setSelectedEnrollmentForGrade(null)}
                  className="btn btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" disabled={isRecordingGrade} className="btn btn-primary">
                  {isRecordingGrade ? <Spinner size={18} /> : 'Guardar Calificación'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
