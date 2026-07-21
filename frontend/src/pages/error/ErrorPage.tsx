import React from 'react';
import { useRouteError, isRouteErrorResponse, Link } from 'react-router-dom';
import { AlertOctagon, RefreshCw } from 'lucide-react';

export const ErrorPage: React.FC = () => {
  const error = useRouteError();
  let errorMessage = 'Ocurrió un error inesperado en la aplicación.';

  if (isRouteErrorResponse(error)) {
    errorMessage = `${error.status} ${error.statusText}`;
  } else if (error instanceof Error) {
    errorMessage = error.message;
  }

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        textAlign: 'center',
        padding: '2rem',
        backgroundColor: 'var(--bg-page)',
      }}
    >
      <AlertOctagon size={64} style={{ color: 'var(--danger)', marginBottom: '1.5rem' }} />
      <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.5rem' }}>Excepción de Aplicación</h1>
      <p style={{ color: 'var(--danger)', fontSize: '0.938rem', fontFamily: 'monospace', maxWidth: '540px', marginBottom: '2rem', background: 'var(--danger-light)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
        {errorMessage}
      </p>
      <div style={{ display: 'flex', gap: '1rem' }}>
        <button onClick={() => window.location.reload()} className="btn btn-primary">
          <RefreshCw size={18} /> Recargar Aplicación
        </button>
        <Link to="/" className="btn btn-secondary">
          Ir al Inicio
        </Link>
      </div>
    </div>
  );
};
