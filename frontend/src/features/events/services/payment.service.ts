import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { PaymentRequestPayload, PaymentResultResponse } from '../../../types/payment.types';

export interface SubmitVoucherPayload {
  enrollmentId: string;
  paymentMethod: 'YAPE' | 'PLIN' | 'BANK_TRANSFER' | 'CARD';
  operationNumber: string;
  voucherUrl: string;
  amount?: number;
}

export interface PaymentVerificationItem {
  paymentId: string;
  enrollmentId: string;
  participantName: string;
  participantEmail: string;
  eventName: string;
  amount: number;
  currency: string;
  paymentMethod: string;
  operationNumber: string;
  voucherUrl: string;
  paymentStatus: string;
  paymentDate: string;
  notes?: string;
}

export interface VerifyPaymentPayload {
  action: 'APPROVE' | 'REJECT';
  notes?: string;
}

export const paymentService = {
  processPayment: async (payload: PaymentRequestPayload): Promise<PaymentResultResponse> => {
    const response = await apiClient.post<ApiResponse<PaymentResultResponse>>('/api/payments/process', payload);
    return response.data.data;
  },

  submitVoucher: async (payload: SubmitVoucherPayload): Promise<PaymentResultResponse> => {
    const response = await apiClient.post<ApiResponse<PaymentResultResponse>>('/api/payments/submit-voucher', payload);
    return response.data.data;
  },

  getPendingVerifications: async (): Promise<PaymentVerificationItem[]> => {
    const response = await apiClient.get<ApiResponse<PaymentVerificationItem[]>>('/api/payments/pending-verifications');
    return response.data.data;
  },

  verifyPayment: async (paymentId: string, payload: VerifyPaymentPayload): Promise<PaymentVerificationItem> => {
    const response = await apiClient.post<ApiResponse<PaymentVerificationItem>>(`/api/payments/${paymentId}/verify`, payload);
    return response.data.data;
  },
};
