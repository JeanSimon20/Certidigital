import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { ShieldCheck, LogIn, UserPlus } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';

export const PublicLayout: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  return (
    <div className="public-shell">
      <header className="public-navbar">
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: 'var(--radius-md)',
              background: 'var(--primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
            }}
          >
            <ShieldCheck size={24} />
          </div>
          <span style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)', fontFamily: 'var(--font-family-heading)' }}>
            Certi<span style={{ color: 'var(--primary)' }}>Digital</span>
          </span>
        </Link>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <Link to="/events/catalog" style={{ color: 'var(--text-main)', fontWeight: 600, fontSize: '0.938rem', textDecoration: 'none' }}>
            Catálogo de Eventos
          </Link>

          {isAuthenticated ? (
            <Link to="/dashboard" className="btn btn-primary">
              Ir al Dashboard
            </Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-secondary">
                <LogIn size={16} /> Iniciar Sesión
              </Link>
              <Link to="/register" className="btn btn-primary">
                <UserPlus size={16} /> Registrarse
              </Link>
            </>
          )}
        </div>
      </header>

      <main className="public-main">
        <Outlet />
      </main>

      <footer
        style={{
          borderTop: '1px solid var(--border-color)',
          padding: '2rem',
          textAlign: 'center',
          color: 'var(--text-muted)',
          fontSize: '0.875rem',
          backgroundColor: 'var(--bg-surface)',
        }}
      >
        <p>© 2026 CertiDigital Platform. Plataforma de Gestión de Credenciales Digitales. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
};
