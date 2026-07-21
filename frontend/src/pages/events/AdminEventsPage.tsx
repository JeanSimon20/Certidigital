import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Calendar, Edit, Globe, XCircle, Users } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { Event } from '../../types/event.types';
import { useTenantStore } from '../../store/useTenantStore';
import { useAuthStore } from '../../store/useAuthStore';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';

export const AdminEventsPage: React.FC = () => {
  const { activeTenant, activeRoles } = useTenantStore();
  const { user } = useAuthStore();
  const [events, setEvents] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const userRoles = activeRoles.length > 0 ? activeRoles : user?.activeRoles || [];
  const isTenantAdmin = userRoles.includes('TENANT_ADMIN') || userRoles.includes('SUPER_ADMIN');
  const isTeacher = userRoles.includes('TEACHER') || userRoles.includes('ORGANIZER');

  const fetchTenantEvents = async () => {
    setIsLoading(true);
    try {
      const data = await eventService.getTenantEvents();
      setEvents(data);
    } catch (err) {
      console.error('Error al cargar eventos del tenant:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTenantEvents();
  }, [activeTenant?.tenantId]);

  const handlePublish = async (eventId: string) => {
    setActionLoadingId(eventId);
    try {
      await eventService.publishEvent(eventId);
      await fetchTenantEvents();
    } catch (err) {
      console.error('Error al publicar evento:', err);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCancel = async (eventId: string) => {
    if (!window.confirm('¿Está seguro de cancelar este evento?')) return;
    setActionLoadingId(eventId);
    try {
      await eventService.cancelEvent(eventId);
      await fetchTenantEvents();
    } catch (err) {
      console.error('Error al cancelar evento:', err);
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Gestión de Eventos</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Organización: <strong>{activeTenant?.commercialName || activeTenant?.legalName || 'Tenant Activo'}</strong>
          </p>
        </div>

        {isTenantAdmin && (
          <Link to="/events/new" className="btn btn-primary">
            <Plus size={18} /> Crear Evento
          </Link>
        )}
      </div>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={36} />
        </div>
      ) : events.length === 0 ? (
        <EmptyState
          title="No hay eventos creados"
          description="Comienza creando un nuevo evento académico para tu organización."
          action={
            isTenantAdmin ? (
              <Link to="/events/new" className="btn btn-primary">
                <Plus size={18} /> Crear Evento
              </Link>
            ) : undefined
          }
        />
      ) : (
        <div className="glass-card" style={{ overflowX: 'auto', padding: 0 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-surface-secondary)' }}>
                <th style={{ padding: '1rem' }}>Evento</th>
                <th style={{ padding: '1rem' }}>Tipo</th>
                <th style={{ padding: '1rem' }}>Modalidad</th>
                <th style={{ padding: '1rem' }}>Fecha de Inicio</th>
                <th style={{ padding: '1rem' }}>Capacidad</th>
                <th style={{ padding: '1rem' }}>Estado</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: 600 }}>{event.name}</td>
                  <td style={{ padding: '1rem' }}><span className="badge badge-primary">{event.eventType}</span></td>
                  <td style={{ padding: '1rem' }}>{event.mode}</td>
                  <td style={{ padding: '1rem' }}>{new Date(event.startDate).toLocaleDateString('es-ES')}</td>
                  <td style={{ padding: '1rem' }}>{event.enrolledCount} / {event.maxCapacity}</td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${event.status === 'PUBLISHED' ? 'badge-success' : event.status === 'DRAFT' ? 'badge-warning' : 'badge-danger'}`}>
                      {event.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                      <Link to={`/events/${event.id}/enrollments`} className="btn btn-primary" title="Ver Inscritos, Asistencia y Notas" style={{ padding: '0.375rem 0.75rem', fontSize: '0.813rem' }}>
                        <Users size={16} /> Ver Alumnos / Asistencia / Notas
                      </Link>

                      {isTenantAdmin && (
                        <>
                          <Link to={`/events/${event.id}/edit`} className="btn btn-secondary" title="Editar" style={{ padding: '0.375rem 0.625rem' }}>
                            <Edit size={16} />
                          </Link>

                          {event.status === 'DRAFT' && (
                            <button
                              onClick={() => handlePublish(event.id)}
                              disabled={actionLoadingId === event.id}
                              className="btn btn-primary"
                              title="Publicar en Catálogo"
                              style={{ padding: '0.375rem 0.625rem' }}
                            >
                              <Globe size={16} />
                            </button>
                          )}

                          {event.status !== 'CANCELLED' && (
                            <button
                              onClick={() => handleCancel(event.id)}
                              disabled={actionLoadingId === event.id}
                              className="btn btn-secondary"
                              title="Cancelar Evento"
                              style={{ padding: '0.375rem 0.625rem' }}
                            >
                              <XCircle size={16} className="text-danger" />
                            </button>
                          )}
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
