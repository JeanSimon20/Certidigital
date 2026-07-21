import React from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import { useTenantStore } from '../../store/useTenantStore';
import {
  Building2,
  ShieldCheck,
  Users,
  Calendar,
  Award,
  CheckCircle2,
  FileCheck2,
  FileText,
  UserCheck,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuthStore();
  const { activeTenant, activeRoles, activePermissions } = useTenantStore();

  const userRoles = activeRoles.length > 0 ? activeRoles : user?.activeRoles || [];
  const primaryRole = userRoles[0] || 'VIEWER';

  const isSuperAdmin = userRoles.includes('SUPER_ADMIN');
  const isTenantAdmin = userRoles.includes('TENANT_ADMIN');
  const isOrganizer = userRoles.includes('ORGANIZER') || userRoles.includes('TEACHER');
  const isParticipant = userRoles.includes('PARTICIPANT');

  return (
    <div>
      {/* Header Widget */}
      <div
        className="glass-card"
        style={{
          padding: '2rem',
          marginBottom: '2rem',
          background: 'linear-gradient(135deg, var(--primary-light), var(--bg-surface))',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Bienvenido, {user?.fullName} 👋</h1>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              {isSuperAdmin
                ? 'Panel de Control Global de CertiDigital (Nivel Plataforma)'
                : activeTenant
                ? `Organización Activa: ${activeTenant.commercialName || activeTenant.legalName}`
                : 'Seleccione un Tenant activo para gestionar eventos y credenciales.'}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {userRoles.map((role) => (
              <span key={role} className="badge badge-primary">
                {role}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Metrics Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>Organización</span>
            <Building2 size={24} style={{ color: 'var(--primary)' }} />
          </div>
          <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>
            {activeTenant ? activeTenant.commercialName || activeTenant.legalName : 'Global Admin'}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Plan: {activeTenant?.servicePlan || 'SUPER_ADMIN'}
          </span>
        </div>

        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>Rol Activo</span>
            <ShieldCheck size={24} style={{ color: 'var(--success)' }} />
          </div>
          <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>
            {primaryRole}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Contexto Multi-Tenant
          </span>
        </div>

        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>Permisos Asignados</span>
            <Award size={24} style={{ color: 'var(--warning)' }} />
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>
            {activePermissions.length}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Permisos atómicos activos
          </span>
        </div>
      </div>

      {/* Role-Specific Dashboard Sections */}

      {/* PARTICIPANT Dashboard */}
      {isParticipant && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>Panel de Participante</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/my-enrollments" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <UserCheck size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Mis Inscripciones</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Consulta tus solicitudes e inscripciones en talleres y programas académicos.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Inscripciones <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/my-credentials" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Award size={32} style={{ color: 'var(--success)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Mis Certificados</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Billetera digital de certificados y credenciales académicas verificables.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Billetera <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/my-events" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Calendar size={32} style={{ color: 'var(--warning)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Trámites y Eventos</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Eventos académicos activos y estado de trámites de titulación.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Eventos <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* ORGANIZER / TEACHER Dashboard */}
      {isOrganizer && !isTenantAdmin && !isSuperAdmin && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>Panel de Organizador / Instructor</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/events" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Calendar size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Eventos Académicos</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Gestionar cursos, seminarios, fechas y políticas de aprobación.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Gestionar Eventos <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/participants" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Users size={32} style={{ color: 'var(--success)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Participantes</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Revisar participantes inscritos, asistencia y notas registradas.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Participantes <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/attendance" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <FileCheck2 size={32} style={{ color: 'var(--warning)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Asistencia e Inscripciones</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Control de asistencia diaria y confirmación de inscripciones.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Controlar Asistencia <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* TENANT_ADMIN Dashboard */}
      {isTenantAdmin && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>Administración de Organización (Tenant Admin)</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/users" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Users size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Gestión de Usuarios</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Invitar usuarios, asignar roles y controlar membresías del Tenant.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Administrar Usuarios <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/events" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Calendar size={32} style={{ color: 'var(--success)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Gestión de Eventos</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Supervisar catálogo de eventos y aprobaciones institucionales.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Administrar Eventos <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/credentials" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Award size={32} style={{ color: 'var(--warning)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Emisión de Credenciales</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Firma criptográfica, plantillas de certificados y revocación.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Emite y Revisa <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/audit" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <FileText size={32} style={{ color: 'var(--danger)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Auditoría de Seguridad</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Logs inmutables de accesos, revocaciones y cambios de rol.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Auditoría <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* SUPER_ADMIN Dashboard */}
      {isSuperAdmin && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>Administración de Plataforma Global (Super Admin)</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/admin/tenants" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Building2 size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Organizaciones (Tenants)</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Crear instituciones, suspender tenants y asignar planes de servicio.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Gestionar Tenants <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/admin/audit" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <ShieldAlert size={32} style={{ color: 'var(--danger)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>Auditoría Global</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Registros de auditoría de todas las organizaciones en la plataforma.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Auditoría Global <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* Permissions Audit Panel */}
      <div className="glass-card" style={{ padding: '1.5rem' }}>
        <h3 style={{ fontSize: '1.125rem', marginBottom: '1rem' }}>Permisos Verificados por Backend (`{primaryRole}`)</h3>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {activePermissions.length > 0 ? (
            activePermissions.map((perm) => (
              <div
                key={perm}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.375rem',
                  padding: '0.375rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--bg-surface-secondary)',
                  fontSize: '0.813rem',
                  fontFamily: 'monospace',
                }}
              >
                <CheckCircle2 size={14} style={{ color: 'var(--success)' }} />
                <span>{perm}</span>
              </div>
            ))
          ) : (
            <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              No hay permisos asignados explícitamente en este contexto.
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
