import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';

export interface RuleEvaluationDetailDTO {
  ruleType: string;
  ruleName: string;
  passed: boolean;
  expectedValue: string;
  obtainedValue: string;
  reason: string;
  evaluatedAt: string;
}

export interface EligibilityResultDTO {
  enrollmentId: string;
  policyId: string;
  status: 'ELIGIBLE' | 'NOT_ELIGIBLE';
  summaryReason: string;
  ruleResults: RuleEvaluationDetailDTO[];
  evaluatedAt: string;
}

export const eligibilityService = {
  evaluateEligibility: async (enrollmentId: string, evaluationScore?: number): Promise<EligibilityResultDTO> => {
    const response = await apiClient.post<ApiResponse<EligibilityResultDTO>>('/api/eligibility/evaluate', {
      enrollmentId,
      evaluationScore: evaluationScore ?? 16.0,
    });
    return response.data.data;
  },

  getEnrollmentEvaluations: async (enrollmentId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(`/api/eligibility/enrollment/${enrollmentId}`);
    return response.data.data;
  },
};
