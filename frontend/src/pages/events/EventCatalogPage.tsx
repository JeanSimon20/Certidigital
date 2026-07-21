import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Calendar, MapPin, Video, Users, ChevronRight, Filter } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { Event } from '../../types/event.types';
import { Spinner } from '../../components/feedback/Spinner';
import { EmptyState } from '../../components/ui/EmptyState';

export const EventCatalogPage: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedMode, setSelectedMode] = useState('ALL');

  const fetchEvents = async () => {
    setIsLoading(true);
    try {
      const data = await eventService.getPublicEvents({
        query: searchQuery,
        eventType: selectedType,
        mode: selectedMode,
      });
      setEvents(data);
    } catch (err) {
      console.error('Error al cargar catálogo de eventos:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, [selectedType, selectedMode]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchEvents();
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2.5rem', textAlign: 'center' }}>
        <span className="badge badge-primary" style={{ marginBottom: '0.75rem' }}>
          Catálogo Público Institucional
        </span>
        <h1 style={{ fontSize: '2.5rem', fontWeight: 800, marginBottom: '0.75rem' }}>
          Explora Eventos y Programas Académicos
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.125rem', maxWidth: '640px', margin: '0 auto' }}>
          Inscríbete en talleres, diplomados y conferencias organizados por universidades y entidades verificadas en CertiDigital.
        </p>
      </div>

      {/* Filter Bar */}
      <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '2.5rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', alignItems: 'end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Buscar Evento</label>
            <div style={{ position: 'relative' }}>
              <input
                type="text"
                placeholder="ej. Taller Python, IA..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '2.5rem' }}
              />
              <Search size={18} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Tipo de Evento</label>
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="form-select"
            >
              <option value="ALL">Todos los tipos</option>
              <option value="COURSE">Curso</option>
              <option value="WORKSHOP">Taller</option>
              <option value="SEMINAR">Seminario</option>
              <option value="CONFERENCE">Conferencia</option>
              <option value="DIPLOMA">Diplomado</option>
            </select>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Modalidad</label>
            <select
              value={selectedMode}
              onChange={(e) => setSelectedMode(e.target.value)}
              className="form-select"
            >
              <option value="ALL">Todas las modalidades</option>
              <option value="IN_PERSON">Presencial</option>
              <option value="VIRTUAL">Virtual</option>
              <option value="HYBRID">Híbrida</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary" style={{ padding: '0.625rem 1.25rem' }}>
            <Filter size={18} /> Filtrar
          </button>
        </form>
      </div>

      {/* Event Cards Grid */}
      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <Spinner size={36} />
        </div>
      ) : events.length === 0 ? (
        <EmptyState
          title="No se encontraron eventos"
          description="Actualmente no hay eventos disponibles con los filtros seleccionados."
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '2rem' }}>
          {events.map((event) => (
            <div key={event.id} className="glass-card" style={{ display: 'flex', flexDirection: 'column', padding: '1.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span className="badge badge-primary">{event.eventType}</span>
                <span className="badge badge-success">
                  {event.mode === 'VIRTUAL' ? 'Virtual' : event.mode === 'IN_PERSON' ? 'Presencial' : 'Híbrido'}
                </span>
              </div>

              <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem', lineHeight: 1.3 }}>{event.name}</h3>
              <span style={{ fontSize: '0.813rem', color: 'var(--primary)', fontWeight: 600, marginBottom: '1rem' }}>
                {event.tenantName}
              </span>

              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem', flex: 1, display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                {event.description}
              </p>

              <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.25rem', fontSize: '0.813rem', color: 'var(--text-muted)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Calendar size={16} />
                  <span>{new Date(event.startDate).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' })}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {event.mode === 'VIRTUAL' ? <Video size={16} /> : <MapPin size={16} />}
                  <span>{event.locationName || event.virtualUrl || 'Por confirmar'}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Users size={16} />
                  <span>Capacidad: {event.enrolledCount} / {event.maxCapacity} participantes</span>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div>
                  <span style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)' }}>
                    {event.price === 0 ? 'Gratuito' : `$${event.price}`}
                  </span>
                </div>
                <Link to={`/events/catalog/${event.id}`} className="btn btn-primary">
                  Ver Detalle <ChevronRight size={16} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
