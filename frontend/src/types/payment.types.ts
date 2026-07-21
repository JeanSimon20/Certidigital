export interface PaymentRequestPayload {
  tenantId: string;
  enrollmentId: string;
  amount: number;
  currency?: string;
  paymentMethod?: string;
  simulateFailure?: boolean;
  failureReason?: string;
}

export interface PaymentResultResponse {
  success: boolean;
  status: 'CONFIRMED' | 'REJECTED' | 'REFUNDED' | 'PENDING';
  externalReference?: string;
  receiptUrl?: string;
  errorMessage?: string;
  timestamp: string;
}
