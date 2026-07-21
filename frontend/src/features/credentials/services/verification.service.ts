import axios from 'axios';
import { ApiResponse } from '../../../types/api.types';

export interface CredentialVerificationResultDTO {
  status: 'VALID' | 'REVOKED' | 'EXPIRED' | 'NOT_FOUND';
  isValid: boolean;
  publicCode?: string;
  participantName?: string;
  participantDocMasked?: string;
  issuerName?: string;
  eventName?: string;
  eventType?: string;
  issuedAt?: string;
  revokedAt?: string;
  revocationReason?: string;
  contentHash?: string;
  blockchainTxId?: string;
  blockchainNetwork?: string;
  verificationUrl?: string;
  qrCodeUrl?: string;
  message?: string;
}

const API_BASE_URL = 'http://localhost:8080';

export const verificationService = {
  verifyCredential: async (codeOrHash: string): Promise<CredentialVerificationResultDTO> => {
    // EndPoint Público (sin token)
    const response = await axios.get<ApiResponse<CredentialVerificationResultDTO>>(
      `${API_BASE_URL}/api/verification/${encodeURIComponent(codeOrHash)}`
    );
    return response.data.data;
  },
};
