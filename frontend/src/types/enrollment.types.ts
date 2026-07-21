export type EnrollmentStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';
export type PaymentStatus = 'NOT_REQUIRED' | 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface Enrollment {
  id: string;
  eventId: string;
  eventName: string;
  eventType: string;
  eventMode: string;
  tenantId: string;
  participantId: string;
  participantName: string;
  participantEmail: string;
  status: EnrollmentStatus;
  paymentStatus: PaymentStatus;
  enrolledAt: string;
}

export interface CreateEnrollmentPayload {
  eventId: string;
}
