import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { LogIn, AlertCircle } from 'lucide-react';
import { authService } from '../../features/auth/services/auth.service';
import { useAuthStore } from '../../store/useAuthStore';
import { useTenantStore } from '../../store/useTenantStore';
import { Spinner } from '../../components/feedback/Spinner';

const loginSchema = z.object({
  email: z.string().min(1, 'El email es obligatorio').email('Formato de email inválido'),
  password: z.string().min(1, 'La contraseña es obligatoria'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { setSession } = useAuthStore();
  const { setAvailableTenants, setActiveContext } = useTenantStore();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormValues) => {
    setErrorMessage(null);
    setIsLoading(true);

    try {
      const response = await authService.login(data);
      setSession(response);
      setAvailableTenants(response.availableTenants);

      const tenants = response.availableTenants || [];
      const isSuperAdmin = response.roles.includes('SUPER_ADMIN');

      if (tenants.length === 0 && !isSuperAdmin) {
        setErrorMessage('Acceso restringido: El usuario no posee organizaciones activas asignadas.');
        return;
      }

      if (tenants.length === 1) {
        // Seleccionar automáticamente el único Tenant del usuario
        const singleTenant = tenants[0];
        const switchResponse = await authService.switchTenant(singleTenant.tenantId);
        setSession(switchResponse);
        setActiveContext(switchResponse.activeTenantId, switchResponse.roles, switchResponse.permissions);
        navigate('/dashboard');
      } else if (tenants.length > 1) {
        // Múltiples organizaciones -> Dirigir al Tenant Selector
        navigate('/select-tenant');
      } else {
        // SUPER_ADMIN global sin Tenant específico
        navigate('/dashboard');
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error de autenticación. Verifique sus credenciales.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.25rem' }}>Iniciar Sesión</h3>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
        Ingrese sus credenciales de acceso a CertiDigital
      </p>

      {errorMessage && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.75rem 1rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'var(--danger-light)',
            color: 'var(--danger)',
            fontSize: '0.875rem',
            marginBottom: '1.25rem',
          }}
        >
          <AlertCircle size={18} />
          <span>{errorMessage}</span>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-group">
          <label className="form-label">Correo Electrónico</label>
          <input
            {...register('email')}
            type="email"
            placeholder="ej. usuario@organizacion.edu.hn"
            className="form-input"
          />
          {errors.email && <span className="form-error">{errors.email.message}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Contraseña</label>
          <input
            {...register('password')}
            type="password"
            placeholder="••••••••"
            className="form-input"
          />
          {errors.password && <span className="form-error">{errors.password.message}</span>}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="btn btn-primary"
          style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem' }}
        >
          {isLoading ? <Spinner size={20} /> : <><LogIn size={18} /> Iniciar Sesión</>}
        </button>
      </form>

      <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
        ¿No tiene una cuenta registrada?{' '}
        <Link to="/register" style={{ fontWeight: 600 }}>
          Crear cuenta
        </Link>
      </div>
    </div>
  );
};
