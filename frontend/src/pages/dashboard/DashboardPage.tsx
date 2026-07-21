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
  GraduationCap,
  Sparkles,
  BookOpen,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuthStore();
  const { activeTenant, activeRoles } = useTenantStore();

  const userRoles = activeRoles.length > 0 ? activeRoles : user?.activeRoles || [];

  const isSuperAdmin = userRoles.includes('SUPER_ADMIN');
  const isTenantAdmin = userRoles.includes('TENANT_ADMIN');
  const isOrganizer = userRoles.includes('ORGANIZER') || userRoles.includes('TEACHER');
  const isParticipant = !isSuperAdmin && !isTenantAdmin && !isOrganizer;

  // Helper para nombre amigable de rol
  const getRoleDisplayName = () => {
    if (isSuperAdmin) return 'Administrador Global de Plataforma';
    if (isTenantAdmin) return 'Administrador de Institución';
    if (isOrganizer) return 'Docente / Instructor Académico';
    return 'Estudiante / Participante';
  };

  return (
    <div style={{ maxWidth: '1080px', margin: '0 auto' }}>
      {/* Header Widget Personalizado por Rol */}
      <div
        className="glass-card"
        style={{
          padding: '2rem',
          marginBottom: '2rem',
          background: isParticipant
            ? 'linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(16, 185, 129, 0.05))'
            : isTenantAdmin
            ? 'linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(245, 158, 11, 0.05))'
            : 'linear-gradient(135deg, var(--primary-light), var(--bg-surface))',
          borderRadius: 'var(--radius-lg)',
          borderLeft: '4px solid var(--primary)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <span className="badge badge-primary" style={{ fontWeight: 700 }}>
                {getRoleDisplayName()}
              </span>
              {activeTenant && (
                <span className="badge badge-success">
                  {activeTenant.commercialName || activeTenant.legalName}
                </span>
              )}
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Bienvenido, {user?.fullName} 👋</h1>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.25rem', fontSize: '0.938rem' }}>
              {isSuperAdmin
                ? 'Supervisión global de organizaciones, planes de servicio e infraestructura multi-tenant.'
                : isTenantAdmin
                ? `Gestión institucional de ${activeTenant?.legalName || 'la organización'}.`
                : isOrganizer
                ? 'Control académico de cursos asignados, registro de asistencia y notas de estudiantes.'
                : 'Portal centralizado de seguimiento de trámites académicos y credenciales digitales.'}
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ padding: '0.75rem 1.25rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--radius-md)', textAlign: 'right', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>Estado de Cuenta</div>
              <div style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--success)', marginTop: '0.125rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <CheckCircle2 size={14} /> Activa y Verificada
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ============================================================ */}
      {/* 1. VISTA PARTICIPANTE / ESTUDIANTE                            */}
      {/* ============================================================ */}
      {isParticipant && (
        <div>
          {/* Tarjetas Resumen del Participante */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
            <div className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 600 }}>Mis Inscripciones</span>
                <UserCheck size={24} style={{ color: 'var(--primary)' }} />
              </div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>Curso Kubernetes</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--success)', fontWeight: 600 }}> Inscripto Oficialmente</span>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 600 }}>Estado de Trámite</span>
                <Sparkles size={24} style={{ color: 'var(--warning)' }} />
              </div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--success)' }}>Elegible</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Requisitos 100% Cumplidos</span>
            </div>

            <div className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 600 }}>Credencial Digital</span>
                <Award size={24} style={{ color: 'var(--success)' }} />
              </div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>1 Certificado</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Verificable en Blockchain</span>
            </div>
          </div>

          {/* Accesos Directos del Participante */}
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem' }}>Módulos del Participante</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
            <Link to="/my-credentials" className="glass-card" style={{ padding: '1.75rem', textDecoration: 'none', color: 'inherit', transition: 'all 0.2s ease' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', backgroundColor: 'var(--success-light)', color: 'var(--success)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
                <Award size={28} />
              </div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>Mis Certificados</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1.25rem', lineHeight: '1.5' }}>
                Accede a tu billetera de credenciales digitales, simula el pago de tu trámite, evalúa tu elegibilidad y descarga tu certificado en PDF.
              </p>
              <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.375rem', color: 'var(--primary)', fontWeight: 700, fontSize: '0.875rem' }}>
                Ver Mis Certificados <ChevronRight size={16} />
              </div>
            </Link>

            <Link to="/my-enrollments" className="glass-card" style={{ padding: '1.75rem', textDecoration: 'none', color: 'inherit', transition: 'all 0.2s ease' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
                <UserCheck size={28} />
              </div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>Mis Inscripciones</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1.25rem', lineHeight: '1.5' }}>
                Revisa el detalle de tus eventos registrados, el estado del pago simulado y tu porcentaje de asistencia acumulada.
              </p>
              <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.375rem', color: 'var(--primary)', fontWeight: 700, fontSize: '0.875rem' }}>
                Ver Inscripciones <ChevronRight size={16} />
              </div>
            </Link>

            <Link to="/events/catalog" className="glass-card" style={{ padding: '1.75rem', textDecoration: 'none', color: 'inherit', transition: 'all 0.2s ease' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', backgroundColor: 'var(--warning-light)', color: 'var(--warning)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
                <BookOpen size={28} />
              </div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>Catálogo de Cursos</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1.25rem', lineHeight: '1.5' }}>
                Explora la oferta académica disponible en tu institución e inscríbete en nuevos diplomados y programas.
              </p>
              <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.375rem', color: 'var(--primary)', fontWeight: 700, fontSize: '0.875rem' }}>
                Explorar Catálogo <ChevronRight size={16} />
              </div>
            </Link>
          </div>
        </div>
      )}

      {/* ============================================================ */}
      {/* 2. VISTA DOCENTE / ORGANIZADOR                                */}
      {/* ============================================================ */}
      {isOrganizer && !isTenantAdmin && !isSuperAdmin && (
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem' }}>Panel del Instructor Académico</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/events" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Calendar size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Eventos Asignados</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Supervisar el estado de publicación y fechas de los cursos a tu cargo.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Gestionar Cursos <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/participants" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Users size={32} style={{ color: 'var(--success)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Listado de Estudiantes</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Consultar el registro de alumnos inscritos en cada grupo académico.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Alumnos <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/attendance" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <FileCheck2 size={32} style={{ color: 'var(--warning)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Asistencia y Notas</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Registrar la asistencia diaria y las calificaciones finales de los participantes.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Controlar Asistencia <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* ============================================================ */}
      {/* 3. VISTA ADMINISTRADOR DE INSTITUCIÓN                         */}
      {/* ============================================================ */}
      {isTenantAdmin && (
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem' }}>Gestión Institucional (Tenant Admin)</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/events" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Calendar size={32} style={{ color: 'var(--success)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Catálogo e Inscripciones</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Crear nuevos eventos académicos, publicar cursos y revisar inscripciones.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Administrar Eventos <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/users" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Users size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Usuarios y Roles</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Gestionar docentes, administradores y permisos atómicos dentro de la institución.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Administrar Usuarios <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/credentials" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Award size={32} style={{ color: 'var(--warning)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Emisión de Credenciales</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Supervisar solicitudes de emisión, firma criptográfica SHA-256 y revocaciones.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Gestionar Emisiones <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/audit" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <FileText size={32} style={{ color: 'var(--danger)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Auditoría Institucional</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Logs inmutables de trazabilidad, cambios de estado y registros de seguridad.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Ver Audit Logs <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}

      {/* ============================================================ */}
      {/* 4. VISTA SUPER ADMINISTRADOR (PLATAFORMA GLOBAL)             */}
      {/* ============================================================ */}
      {isSuperAdmin && (
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem' }}>Gestión Global de Plataforma (Super Admin)</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <Link to="/admin/tenants" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <Building2 size={32} style={{ color: 'var(--primary)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Organizaciones (Tenants)</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Crear instituciones educativas, suspender organizaciones y asignar planes.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Gestionar Tenants <ChevronRight size={14} />
              </span>
            </Link>

            <Link to="/admin/audit" className="glass-card" style={{ padding: '1.5rem', textDecoration: 'none', color: 'inherit' }}>
              <ShieldAlert size={32} style={{ color: 'var(--danger)', marginBottom: '0.75rem' }} />
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.25rem' }}>Auditoría Global Multi-Tenant</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Supervisión centralizada de eventos de seguridad de todas las instituciones.
              </p>
              <span className="btn btn-outline" style={{ padding: '0.375rem 0.75rem', fontSize: '0.75rem' }}>
                Auditoría Plataforma <ChevronRight size={14} />
              </span>
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};
