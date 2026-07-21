import { TenantSummary } from './tenant.types';

export interface User {
  id: string;
  email: string;
  fullName: string;
  status: string;
  emailVerified: boolean;
  lastLoginAt: string | null;
  activeTenantId?: string | null;
  activeRoles?: string[];
  activePermissions?: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  userId: string;
  email: string;
  fullName: string;
  activeTenantId: string | null;
  roles: string[];
  permissions: string[];
  availableTenants: TenantSummary[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  fullName: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface SwitchTenantRequest {
  tenantId: string;
}
