import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save, AlertCircle } from 'lucide-react';
import { eventService } from '../../features/events/services/event.service';
import { EventType, EventMode } from '../../types/event.types';
import { Spinner } from '../../components/feedback/Spinner';

const eventSchema = z.object({
  name: z.string().min(3, 'El nombre debe tener al menos 3 caracteres'),
  description: z.string().min(10, 'La descripción debe tener al menos 10 caracteres'),
  eventType: z.enum(['COURSE', 'WORKSHOP', 'SEMINAR', 'CONFERENCE', 'DIPLOMA'] as const),
  mode: z.enum(['IN_PERSON', 'VIRTUAL', 'HYBRID'] as const),
  startDate: z.string().min(1, 'La fecha de inicio es requerida'),
  endDate: z.string().min(1, 'La fecha de fin es requerida'),
  locationName: z.string().optional(),
  virtualUrl: z.string().optional(),
  maxCapacity: z.coerce.number().min(1, 'La capacidad máxima debe ser al menos 1'),
  price: z.coerce.number().min(0, 'El precio no puede ser negativo'),
});

type EventFormValues = z.infer<typeof eventSchema>;

export const CreateEventPage: React.FC = () => {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<EventFormValues>({
    resolver: zodResolver(eventSchema),
    defaultValues: {
      eventType: 'COURSE',
      mode: 'IN_PERSON',
      maxCapacity: 50,
      price: 0,
    },
  });

  const selectedMode = watch('mode');

  const onSubmit = async (data: EventFormValues) => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      await eventService.createEvent({
        name: data.name,
        description: data.description,
        eventType: data.eventType as EventType,
        mode: data.mode as EventMode,
        startDate: new Date(data.startDate).toISOString(),
        endDate: new Date(data.endDate).toISOString(),
        locationName: data.locationName,
        virtualUrl: data.virtualUrl,
        maxCapacity: data.maxCapacity,
        price: data.price,
      });
      navigate('/events');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al crear el evento.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <Link to="/events" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
        <ArrowLeft size={16} /> Volver a Gestión de Eventos
      </Link>

      <div className="glass-card" style={{ padding: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>Crear Nuevo Evento Académico</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '2rem' }}>
          El evento se creará inicialmente en estado Borrador (DRAFT) y podrá ser publicado posteriormente.
        </p>

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

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="form-group">
            <label className="form-label">Nombre del Evento</label>
            <input {...register('name')} type="text" placeholder="ej. Taller Avanzado de Desarrollo Web" className="form-input" />
            {errors.name && <span className="form-error">{errors.name.message}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Descripción Detallada</label>
            <textarea {...register('description')} rows={4} placeholder="Describa el contenido del curso o taller..." className="form-input" />
            {errors.description && <span className="form-error">{errors.description.message}</span>}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Tipo de Evento</label>
              <select {...register('eventType')} className="form-select">
                <option value="COURSE">Curso</option>
                <option value="WORKSHOP">Taller</option>
                <option value="SEMINAR">Seminario</option>
                <option value="CONFERENCE">Conferencia</option>
                <option value="DIPLOMA">Diplomado</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Modalidad</label>
              <select {...register('mode')} className="form-select">
                <option value="IN_PERSON">Presencial</option>
                <option value="VIRTUAL">Virtual</option>
                <option value="HYBRID">Híbrida</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Fecha y Hora de Inicio</label>
              <input {...register('startDate')} type="datetime-local" className="form-input" />
              {errors.startDate && <span className="form-error">{errors.startDate.message}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Fecha y Hora de Fin</label>
              <input {...register('endDate')} type="datetime-local" className="form-input" />
              {errors.endDate && <span className="form-error">{errors.endDate.message}</span>}
            </div>
          </div>

          {selectedMode !== 'VIRTUAL' && (
            <div className="form-group">
              <label className="form-label">Ubicación / Campus</label>
              <input {...register('locationName')} type="text" placeholder="ej. Auditorio Principal, Edificio B" className="form-input" />
            </div>
          )}

          {selectedMode !== 'IN_PERSON' && (
            <div className="form-group">
              <label className="form-label">Enlace de Aula Virtual / Teams / Zoom</label>
              <input {...register('virtualUrl')} type="text" placeholder="https://teams.microsoft.com/..." className="form-input" />
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Capacidad Máxima</label>
              <input {...register('maxCapacity')} type="number" className="form-input" />
              {errors.maxCapacity && <span className="form-error">{errors.maxCapacity.message}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Precio ($)</label>
              <input {...register('price')} type="number" step="0.01" className="form-input" />
              {errors.price && <span className="form-error">{errors.price.message}</span>}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
            <Link to="/events" className="btn btn-secondary">
              Cancelar
            </Link>
            <button type="submit" disabled={isLoading} className="btn btn-primary">
              {isLoading ? <Spinner size={20} /> : <><Save size={18} /> Guardar Borrador</>}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
