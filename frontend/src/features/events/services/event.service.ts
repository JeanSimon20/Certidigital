import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { Event, CreateEventPayload, EventFilter } from '../../../types/event.types';
import { Enrollment } from '../../../types/enrollment.types';

export const eventService = {
  // Catálogo Público
  getPublicEvents: async (filter?: EventFilter): Promise<Event[]> => {
    const params = new URLSearchParams();
    if (filter?.query) params.append('query', filter.query);
    if (filter?.eventType && filter.eventType !== 'ALL') params.append('eventType', filter.eventType);
    if (filter?.mode && filter.mode !== 'ALL') params.append('mode', filter.mode);

    const response = await apiClient.get<ApiResponse<Event[]>>(`/api/events/public?${params.toString()}`);
    return response.data.data;
  },

  getPublicEventDetail: async (id: string): Promise<Event> => {
    const response = await apiClient.get<ApiResponse<Event>>(`/api/events/public/${id}`);
    return response.data.data;
  },

  // Administración por Tenant
  getTenantEvents: async (): Promise<Event[]> => {
    const response = await apiClient.get<ApiResponse<Event[]>>('/api/events');
    return response.data.data;
  },

  createEvent: async (payload: CreateEventPayload): Promise<Event> => {
    const response = await apiClient.post<ApiResponse<Event>>('/api/events', payload);
    return response.data.data;
  },

  updateEvent: async (id: string, payload: CreateEventPayload): Promise<Event> => {
    const response = await apiClient.put<ApiResponse<Event>>(`/api/events/${id}`, payload);
    return response.data.data;
  },

  publishEvent: async (id: string): Promise<Event> => {
    const response = await apiClient.post<ApiResponse<Event>>(`/api/events/${id}/publish`);
    return response.data.data;
  },

  cancelEvent: async (id: string): Promise<Event> => {
    const response = await apiClient.post<ApiResponse<Event>>(`/api/events/${id}/cancel`);
    return response.data.data;
  },

  getEventEnrollments: async (id: string): Promise<Enrollment[]> => {
    const response = await apiClient.get<ApiResponse<Enrollment[]>>(`/api/events/${id}/enrollments`);
    return response.data.data;
  },
};
