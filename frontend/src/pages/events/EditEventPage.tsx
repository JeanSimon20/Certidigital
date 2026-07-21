import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
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

export const EditEventPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<EventFormValues>({
    resolver: zodResolver(eventSchema),
  });

  const selectedMode = watch('mode');

  useEffect(() => {
    if (id) {
      eventService
        .getPublicEventDetail(id)
        .then((data) => {
          setValue('name', data.name);
          setValue('description', data.description);
          setValue('eventType', data.eventType);
          setValue('mode', data.mode);
          setValue('startDate', data.startDate ? data.startDate.substring(0, 16) : '');
          setValue('endDate', data.endDate ? data.endDate.substring(0, 16) : '');
          setValue('locationName', data.locationName || '');
          setValue('virtualUrl', data.virtualUrl || '');
          setValue('maxCapacity', data.maxCapacity);
          setValue('price', data.price);
        })
        .catch((err) => {
          console.error(err);
          setErrorMessage('No se pudo cargar el evento.');
        })
        .finally(() => setIsLoading(false));
    }
  }, [id, setValue]);

  const onSubmit = async (data: EventFormValues) => {
    if (!id) return;
    setIsSaving(true);
    setErrorMessage(null);

    try {
      await eventService.updateEvent(id, {
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
      const msg = err.response?.data?.message || 'Error al actualizar el evento.';
      setErrorMessage(msg);
    } finally {
      setIsSaving(false);
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
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <Link to="/events" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
        <ArrowLeft size={16} /> Volver a Gestión de Eventos
      </Link>

      <div className="glass-card" style={{ padding: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>Editar Evento Académico</h1>

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
            <input {...register('name')} type="text" className="form-input" />
            {errors.name && <span className="form-error">{errors.name.message}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Descripción Detallada</label>
            <textarea {...register('description')} rows={4} className="form-input" />
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
              <input {...register('locationName')} type="text" className="form-input" />
            </div>
          )}

          {selectedMode !== 'IN_PERSON' && (
            <div className="form-group">
              <label className="form-label">Enlace de Aula Virtual</label>
              <input {...register('virtualUrl')} type="text" className="form-input" />
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Capacidad Máxima</label>
              <input {...register('maxCapacity')} type="number" className="form-input" />
            </div>

            <div className="form-group">
              <label className="form-label">Precio ($)</label>
              <input {...register('price')} type="number" step="0.01" className="form-input" />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
            <Link to="/events" className="btn btn-secondary">
              Cancelar
            </Link>
            <button type="submit" disabled={isSaving} className="btn btn-primary">
              {isSaving ? <Spinner size={20} /> : <><Save size={18} /> Actualizar Evento</>}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
