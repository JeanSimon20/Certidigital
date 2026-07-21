import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useTenantStore } from '../store/useTenantStore';
import { PermissionCode } from '../types/rbac.types';

interface PermissionGuardProps {
  requiredPermission: PermissionCode;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  requiredPermission,
  children,
  fallback,
}) => {
  const { activePermissions } = useTenantStore();
  const { user } = useAuthStore();

  const permissions = activePermissions.length > 0 ? activePermissions : user?.activePermissions || [];

  const hasPermission = permissions.includes(requiredPermission);

  if (!hasPermission) {
    if (fallback) return <>{fallback}</>;
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
};
