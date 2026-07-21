import {
  LayoutDashboard,
  Calendar,
  UserCheck,
  Award,
  Users,
  ShieldCheck,
  FileText,
  Building2,
  Sliders,
  FileCheck2,
  BookOpen,
} from 'lucide-react';
import { PermissionCode } from '../types/rbac.types';

export interface NavItem {
  title: string;
  path: string;
  icon: React.ElementType;
  requiredPermission?: PermissionCode;
}

export const getNavigationItems = (roles: string[]): NavItem[] => {
  const isSuperAdmin = roles.includes('SUPER_ADMIN');
  const isTenantAdmin = roles.includes('TENANT_ADMIN');
  const isOrganizer = roles.includes('ORGANIZER');
  const isTeacher = roles.includes('TEACHER') || roles.includes('FACILITATOR');
  const isParticipant = roles.includes('PARTICIPANT') || roles.includes('VIEWER') || (!isSuperAdmin && !isTenantAdmin && !isOrganizer && !isTeacher);

  if (isSuperAdmin) {
    return [
      { title: 'Dashboard Global', path: '/dashboard', icon: LayoutDashboard },
      { title: 'Organizaciones (Tenants)', path: '/admin/tenants', icon: Building2 },
      { title: 'Planes de Servicio', path: '/admin/plans', icon: Sliders },
      { title: 'Auditoría Global', path: '/admin/audit', icon: FileText },
    ];
  }

  if (isTenantAdmin) {
    return [
      { title: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { title: 'Usuarios y Membresías', path: '/users', icon: Users },
      { title: 'Roles y Permisos', path: '/roles', icon: ShieldCheck },
      { title: 'Eventos', path: '/events', icon: Calendar },
      { title: 'Validación de Pagos', path: '/admin/payment-verifications', icon: FileCheck2 },
      { title: 'Participantes', path: '/participants', icon: UserCheck },
      { title: 'Credenciales', path: '/credentials', icon: Award },
      { title: 'Auditoría de Seguridad', path: '/audit', icon: FileText },
    ];
  }

  if (isTeacher || isOrganizer) {
    return [
      { title: 'Panel Docente', path: '/dashboard', icon: LayoutDashboard },
      { title: 'Mis Eventos Asignados', path: '/events', icon: Calendar },
      { title: 'Padrón de Alumnos', path: '/participants', icon: Users },
    ];
  }

  if (isParticipant) {
    return [
      { title: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
      { title: 'Explorar Catálogo', path: '/events/catalog', icon: BookOpen },
      { title: 'Mis Inscripciones', path: '/my-enrollments', icon: UserCheck },
      { title: 'Mis Certificados', path: '/my-credentials', icon: Award },
    ];
  }

  // Default fallback navigation
  return [
    { title: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  ];
};
