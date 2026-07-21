export interface RecordAttendancePayload {
  enrollmentId: string;
  sessionId: string;
  attended: boolean;
  method?: 'MANUAL' | 'QR_CODE' | 'IMPORT';
  notes?: string;
}

export interface AttendanceRecordResponse {
  id: string;
  tenantId: string;
  enrollmentId: string;
  participantId: string;
  participantName: string;
  sessionId: string;
  attended: boolean;
  method: string;
  recordedBy: string;
  recordedAt: string;
  notes?: string;
}
