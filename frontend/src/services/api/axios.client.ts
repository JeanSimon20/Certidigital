import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { env } from '../../app/config/env.config';
import { useAuthStore } from '../../store/useAuthStore';
import { useTenantStore } from '../../store/useTenantStore';
import { ApiResponse, ApiErrorResponse } from '../../types/api.types';
import { AuthResponse } from '../../types/auth.types';

export const apiClient = axios.create({
  baseURL: env.apiUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else if (token) {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

// ==========================================
// Request Interceptor: Inject Token & Tenant
// ==========================================
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    const activeTenantId = useTenantStore.getState().activeTenantId;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (activeTenantId) {
      config.headers['X-Tenant-Id'] = activeTenantId;
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ====================================================
// Response Interceptor: 401 & Automatic Refresh Queue
// ====================================================
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Si la respuesta es 401 y no es una ruta de auth publica o de refresco
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      if (originalRequest.url?.includes('/api/auth/refresh') || originalRequest.url?.includes('/api/auth/login')) {
        useAuthStore.getState().logout();
        useTenantStore.getState().clearTenant();
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      isRefreshing = true;
      const refreshToken = useAuthStore.getState().refreshToken;

      if (!refreshToken) {
        useAuthStore.getState().logout();
        useTenantStore.getState().clearTenant();
        return Promise.reject(error);
      }

      try {
        const refreshResponse = await axios.post<ApiResponse<AuthResponse>>(
          `${env.apiUrl}/api/auth/refresh`,
          { refreshToken }
        );

        const newAuthData = refreshResponse.data.data;
        useAuthStore.getState().setSession(newAuthData);
        useTenantStore.getState().setAvailableTenants(newAuthData.availableTenants);

        if (newAuthData.activeTenantId) {
          useTenantStore.getState().setActiveContext(
            newAuthData.activeTenantId,
            newAuthData.roles,
            newAuthData.permissions
          );
        }

        processQueue(null, newAuthData.accessToken);

        originalRequest.headers.Authorization = `Bearer ${newAuthData.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        useAuthStore.getState().logout();
        useTenantStore.getState().clearTenant();
        window.location.href = '/login?expired=true';
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
