import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';

export interface RecordEvaluationPayload {
  enrollmentId: string;
  evaluationName: string;
  evaluationType?: string;
  score: number;
  maxScore?: number;
  passingScore?: number;
}

export interface EvaluationResultResponse {
  id: string;
  tenantId: string;
  enrollmentId: string;
  evaluationName: string;
  evaluationType: string;
  score: number;
  maxScore: number;
  passingScore: number;
  passed: boolean;
  recordedAt: string;
}

export const evaluationService = {
  recordEvaluation: async (payload: RecordEvaluationPayload): Promise<EvaluationResultResponse> => {
    const response = await apiClient.post<ApiResponse<EvaluationResultResponse>>('/api/evaluations', payload);
    return response.data.data;
  },

  getEnrollmentEvaluations: async (enrollmentId: string): Promise<EvaluationResultResponse[]> => {
    const response = await apiClient.get<ApiResponse<EvaluationResultResponse[]>>(`/api/evaluations/enrollment/${enrollmentId}`);
    return response.data.data;
  },
};
