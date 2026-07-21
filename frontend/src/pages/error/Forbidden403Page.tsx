import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export const Forbidden403Page: React.FC = () => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '70vh',
        textAlign: 'center',
        padding: '2rem',
      }}
    >
      <ShieldAlert size={64} style={{ color: 'var(--danger)', marginBottom: '1.5rem', opacity: 0.8 }} />
      <h1 style={{ fontSize: '2.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>403 — Acceso Denegado</h1>
      <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '480px', marginBottom: '2rem' }}>
        No cuenta con los permisos necesarios o la membresía requerida para acceder a este recurso o módulo en la organización actual.
      </p>
      <Link to="/dashboard" className="btn btn-primary">
        <ArrowLeft size={18} /> Volver al Dashboard
      </Link>
    </div>
  );
};
