import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useTenantStore } from '../store/useTenantStore';
import { SystemRole } from '../types/rbac.types';

interface RoleGuardProps {
  allowedRoles: SystemRole[];
  children: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ allowedRoles, children }) => {
  const { activeRoles } = useTenantStore();
  const { user } = useAuthStore();

  const userRoles = activeRoles.length > 0 ? activeRoles : user?.activeRoles || [];

  const hasRole = allowedRoles.some((role) => userRoles.includes(role));

  if (!hasRole) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
};
