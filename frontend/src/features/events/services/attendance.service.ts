import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { RecordAttendancePayload, AttendanceRecordResponse } from '../../../types/attendance.types';

export const attendanceService = {
  recordAttendance: async (payload: RecordAttendancePayload): Promise<AttendanceRecordResponse> => {
    const response = await apiClient.post<ApiResponse<AttendanceRecordResponse>>('/api/attendance', payload);
    return response.data.data;
  },

  getSessionAttendance: async (sessionId: string): Promise<AttendanceRecordResponse[]> => {
    const response = await apiClient.get<ApiResponse<AttendanceRecordResponse[]>>(`/api/attendance/session/${sessionId}`);
    return response.data.data;
  },
};
