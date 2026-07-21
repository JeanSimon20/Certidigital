import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useTenantStore } from '../store/useTenantStore';

interface TenantGuardProps {
  children: React.ReactNode;
}

export const TenantGuard: React.FC<TenantGuardProps> = ({ children }) => {
  const { activeTenantId, availableTenants, activeRoles } = useTenantStore();
  const { user } = useAuthStore();

  // SUPER_ADMIN global no necesita Tenant activo si está navegando la administración de plataforma
  const isSuperAdmin = activeRoles.includes('SUPER_ADMIN') || user?.activeRoles?.includes('SUPER_ADMIN');

  if (!isSuperAdmin && availableTenants.length > 0 && !activeTenantId) {
    return <Navigate to="/select-tenant" replace />;
  }

  return <>{children}</>;
};
