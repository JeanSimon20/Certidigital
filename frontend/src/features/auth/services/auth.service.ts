import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';
import { AuthResponse, LoginRequest, RegisterRequest, SwitchTenantRequest, User } from '../../../types/auth.types';
import { TenantSummary } from '../../../types/tenant.types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/auth/login', credentials);
    return response.data.data;
  },

  register: async (payload: RegisterRequest): Promise<User> => {
    const response = await apiClient.post<ApiResponse<User>>('/api/auth/register', payload);
    return response.data.data;
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient.post('/api/auth/logout');
    } catch {
      // Ignorar errores en logout para permitir limpieza local
    }
  },

  switchTenant: async (tenantId: string): Promise<AuthResponse> => {
    const payload: SwitchTenantRequest = { tenantId };
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/auth/switch-tenant', payload);
    return response.data.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>('/api/users/me');
    return response.data.data;
  },

  getUserTenants: async (): Promise<TenantSummary[]> => {
    const response = await apiClient.get<ApiResponse<TenantSummary[]>>('/api/users/me/tenants');
    return response.data.data;
  },
};
