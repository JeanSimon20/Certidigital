import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus, AlertCircle } from 'lucide-react';
import { authService } from '../../features/auth/services/auth.service';
import { Spinner } from '../../components/feedback/Spinner';

const registerSchema = z.object({
  fullName: z.string().min(2, 'El nombre completo debe tener al menos 2 caracteres'),
  email: z.string().min(1, 'El email es obligatorio').email('Formato de email inválido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
  confirmPassword: z.string().min(8, 'Confirme su contraseña'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmPassword'],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormValues) => {
    setErrorMessage(null);
    setIsLoading(true);

    try {
      await authService.register({
        email: data.email,
        fullName: data.fullName,
        password: data.password,
      });
      navigate('/login?registered=true');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al registrar la cuenta.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.25rem' }}>Crear Cuenta Global</h3>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
        Registro de identidad única en CertiDigital
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
          <label className="form-label">Nombre Completo</label>
          <input
            {...register('fullName')}
            type="text"
            placeholder="ej. Carlos Mendoza"
            className="form-input"
          />
          {errors.fullName && <span className="form-error">{errors.fullName.message}</span>}
        </div>

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
            placeholder="Mínimo 8 caracteres"
            className="form-input"
          />
          {errors.password && <span className="form-error">{errors.password.message}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Confirmar Contraseña</label>
          <input
            {...register('confirmPassword')}
            type="password"
            placeholder="Repita su contraseña"
            className="form-input"
          />
          {errors.confirmPassword && <span className="form-error">{errors.confirmPassword.message}</span>}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="btn btn-primary"
          style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem' }}
        >
          {isLoading ? <Spinner size={20} /> : <><UserPlus size={18} /> Crear Cuenta</>}
        </button>
      </form>

      <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
        ¿Ya posee una cuenta?{' '}
        <Link to="/login" style={{ fontWeight: 600 }}>
          Iniciar sesión
        </Link>
      </div>
    </div>
  );
};
