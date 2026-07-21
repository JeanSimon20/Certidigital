import React from 'react';
import { Spinner } from './Spinner';

interface LoadingScreenProps {
  message?: string;
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({
  message = 'Cargando plataforma CertiDigital...',
}) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        gap: '1rem',
        backgroundColor: 'var(--bg-page)',
      }}
    >
      <Spinner size={40} />
      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', fontWeight: 500 }}>
        {message}
      </p>
    </div>
  );
};
