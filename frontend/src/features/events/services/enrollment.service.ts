import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { Enrollment, CreateEnrollmentPayload } from '../../../types/enrollment.types';

export const enrollmentService = {
  enrollParticipant: async (payload: CreateEnrollmentPayload): Promise<Enrollment> => {
    const response = await apiClient.post<ApiResponse<Enrollment>>('/api/enrollments', payload);
    return response.data.data;
  },

  getMyEnrollments: async (): Promise<Enrollment[]> => {
    const response = await apiClient.get<ApiResponse<Enrollment[]>>('/api/enrollments/my');
    return response.data.data;
  },
};
