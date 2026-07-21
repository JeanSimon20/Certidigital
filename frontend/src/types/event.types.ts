export type EventType = 'COURSE' | 'WORKSHOP' | 'SEMINAR' | 'CONFERENCE' | 'DIPLOMA';
export type EventMode = 'IN_PERSON' | 'VIRTUAL' | 'HYBRID';
export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Event {
  id: string;
  tenantId: string;
  tenantName: string;
  name: string;
  description: string;
  eventType: EventType;
  mode: EventMode;
  startDate: string;
  endDate: string;
  timezone: string;
  locationName: string | null;
  locationAddress: string | null;
  virtualUrl: string | null;
  maxCapacity: number;
  enrolledCount: number;
  price: number;
  isFree: boolean;
  status: EventStatus;
  createdBy: string;
  createdAt: string;
}

export interface CreateEventPayload {
  name: string;
  description: string;
  eventType: EventType;
  mode: EventMode;
  startDate: string;
  endDate: string;
  locationName?: string;
  locationAddress?: string;
  virtualUrl?: string;
  maxCapacity: number;
  price?: number;
}

export interface EventFilter {
  query?: string;
  eventType?: string;
  mode?: string;
}
