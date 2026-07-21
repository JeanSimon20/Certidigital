import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useTenantStore } from '../store/useTenantStore';
import { authService } from '../features/auth/services/auth.service';
import { LoadingScreen } from '../components/feedback/LoadingScreen';

interface AuthGuardProps {
  children: React.ReactNode;
}

export const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  const { isAuthenticated, user, setUser, logout } = useAuthStore();
  const { availableTenants, setAvailableTenants } = useTenantStore();
  const [isInitializing, setIsInitializing] = useState(!user && isAuthenticated);
  const location = useLocation();

  useEffect(() => {
    let isMounted = true;

    const rehydrateSession = async () => {
      if (isAuthenticated && !user) {
        try {
          const currentUser = await authService.getCurrentUser();
          const userTenants = await authService.getUserTenants();

          if (isMounted) {
            setUser(currentUser);
            setAvailableTenants(userTenants);
          }
        } catch (err) {
          console.error('Error al restaurar sesión:', err);
          if (isMounted) {
            logout();
          }
        } finally {
          if (isMounted) {
            setIsInitializing(false);
          }
        }
      } else {
        setIsInitializing(false);
      }
    };

    rehydrateSession();

    return () => {
      isMounted = false;
    };
  }, [isAuthenticated, user, setUser, setAvailableTenants, logout]);

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (isInitializing) {
    return <LoadingScreen message="Restaurando sesión y contexto de seguridad..." />;
  }

  return <>{children}</>;
};
