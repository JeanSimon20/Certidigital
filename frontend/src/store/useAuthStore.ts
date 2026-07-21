import { create } from 'zustand';
import { User, AuthResponse } from '../types/auth.types';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // Actions
  setSession: (response: AuthResponse) => void;
  setAccessToken: (token: string) => void;
  setUser: (user: User) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
}

const TOKEN_KEY = 'certidigital_access_token';
const REFRESH_KEY = 'certidigital_refresh_token';

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: localStorage.getItem(TOKEN_KEY),
  refreshToken: localStorage.getItem(REFRESH_KEY),
  isAuthenticated: Boolean(localStorage.getItem(TOKEN_KEY)),
  isLoading: false,

  setSession: (response: AuthResponse) => {
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    if (response.refreshToken) {
      localStorage.setItem(REFRESH_KEY, response.refreshToken);
    }

    const user: User = {
      id: response.userId,
      email: response.email,
      fullName: response.fullName,
      status: 'ACTIVE',
      emailVerified: true,
      lastLoginAt: new Date().toISOString(),
      activeTenantId: response.activeTenantId,
      activeRoles: response.roles,
      activePermissions: response.permissions,
    };

    set({
      user,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken || localStorage.getItem(REFRESH_KEY),
      isAuthenticated: true,
      isLoading: false,
    });
  },

  setAccessToken: (token: string) => {
    localStorage.setItem(TOKEN_KEY, token);
    set({ accessToken: token, isAuthenticated: true });
  },

  setUser: (user: User) => set({ user }),

  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem('certidigital_active_tenant_id');
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
    });
  },

  setLoading: (isLoading: boolean) => set({ isLoading }),
}));
