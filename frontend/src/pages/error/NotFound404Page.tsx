import React from 'react';
import { Link } from 'react-router-dom';
import { FileQuestion, ArrowLeft } from 'lucide-react';

export const NotFound404Page: React.FC = () => {
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
      <FileQuestion size={64} style={{ color: 'var(--primary)', marginBottom: '1.5rem', opacity: 0.8 }} />
      <h1 style={{ fontSize: '2.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>404 — Página No Encontrada</h1>
      <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '480px', marginBottom: '2rem' }}>
        La ruta solicitada no existe o ha sido movida dentro de la plataforma CertiDigital.
      </p>
      <Link to="/" className="btn btn-primary">
        <ArrowLeft size={18} /> Ir al Inicio
      </Link>
    </div>
  );
};
