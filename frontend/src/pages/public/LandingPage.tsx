import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, Award, Lock, Building2, ChevronRight, CheckCircle2 } from 'lucide-react';

export const LandingPage: React.FC = () => {
  return (
    <div>
      {/* Hero Section */}
      <section
        style={{
          padding: '5rem 2rem',
          textAlign: 'center',
          background: 'radial-gradient(circle at 50% 20%, var(--primary-light), transparent 70%)',
        }}
      >
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          <span className="badge badge-primary" style={{ marginBottom: '1rem' }}>
            Plataforma Enterprise SaaS Multi-Tenant
          </span>
          <h1 style={{ fontSize: '3rem', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '1.5rem' }}>
            Gestión Inteligente y Verificación de Credenciales Digitales
          </h1>
          <p style={{ fontSize: '1.125rem', color: 'var(--text-muted)', marginBottom: '2.5rem', lineHeight: 1.6 }}>
            CertiDigital permite a universidades, empresas y entidades gubernamentales emitir, auditar y respaldar certificados y títulos técnicos con tecnología blockchain e integridad criptográfica.
          </p>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem' }}>
            <Link to="/register" className="btn btn-primary" style={{ padding: '0.875rem 1.75rem', fontSize: '1rem' }}>
              Comenzar Ahora <ChevronRight size={18} />
            </Link>
            <Link to="/login" className="btn btn-secondary" style={{ padding: '0.875rem 1.75rem', fontSize: '1rem' }}>
              Acceso a Instituciones
            </Link>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section style={{ padding: '4rem 2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <h2 style={{ textAlign: 'center', fontSize: '2rem', marginBottom: '3rem' }}>
          Arquitectura Empresarial de Alto Rendimiento
        </h2>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
          <div className="glass-card" style={{ padding: '2rem' }}>
            <Building2 size={36} className="text-primary" style={{ color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Aislamiento Multi-Tenant</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              Cada organización cuenta con aislamiento lógico de datos, políticas de emisión propias y personalización de marca.
            </p>
          </div>

          <div className="glass-card" style={{ padding: '2rem' }}>
            <Lock size={36} style={{ color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Seguridad RBAC & Auditoría</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              Control de acceso basado en roles con permisos granulares a nivel de recurso y registro inmutable en audit_entries.
            </p>
          </div>

          <div className="glass-card" style={{ padding: '2rem' }}>
            <Award size={36} style={{ color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Credenciales Criptográficas</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              Firma digital avanzada RSA-SHA256 con verificación pública instantánea mediante QR.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};
