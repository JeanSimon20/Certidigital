import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { PaymentRequestPayload, PaymentResultResponse } from '../../../types/payment.types';

export const paymentService = {
  processPayment: async (payload: PaymentRequestPayload): Promise<PaymentResultResponse> => {
    const response = await apiClient.post<ApiResponse<PaymentResultResponse>>('/api/payments/process', payload);
    return response.data.data;
  },
};
