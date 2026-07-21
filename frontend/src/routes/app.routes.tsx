import { createBrowserRouter, Navigate } from 'react-router-dom';
import { PublicLayout } from '../layouts/PublicLayout';
import { AuthLayout } from '../layouts/AuthLayout';
import { AppLayout } from '../layouts/AppLayout';

import { LandingPage } from '../pages/public/LandingPage';
import { VerificationPage } from '../pages/public/VerificationPage';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';
import { TenantSelectPage } from '../pages/auth/TenantSelectPage';
import { DashboardPage } from '../pages/dashboard/DashboardPage';
import { Forbidden403Page } from '../pages/error/Forbidden403Page';
import { NotFound404Page } from '../pages/error/NotFound404Page';
import { ErrorPage } from '../pages/error/ErrorPage';

import { EventCatalogPage } from '../pages/events/EventCatalogPage';
import { EventPublicDetailPage } from '../pages/events/EventPublicDetailPage';
import { AdminEventsPage } from '../pages/events/AdminEventsPage';
import { CreateEventPage } from '../pages/events/CreateEventPage';
import { EditEventPage } from '../pages/events/EditEventPage';
import { EventEnrollmentsAdminPage } from '../pages/events/EventEnrollmentsAdminPage';
import { AdminPaymentVerificationsPage } from '../pages/events/AdminPaymentVerificationsPage';
import { MyEnrollmentsPage } from '../pages/participation/MyEnrollmentsPage';
import { MyCredentialsPage } from '../pages/credentials/MyCredentialsPage';

import { AuthGuard } from '../guards/AuthGuard';
import { GuestGuard } from '../guards/GuestGuard';
import { TenantGuard } from '../guards/TenantGuard';
import { RoleGuard } from '../guards/RoleGuard';
import { EmptyState } from '../components/ui/EmptyState';

export const router = createBrowserRouter([
  // Rutas Públicas (Landing, Catálogo y Verificación)
  {
    path: '/',
    element: <PublicLayout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'events/catalog', element: <EventCatalogPage /> },
      { path: 'events/catalog/:id', element: <EventPublicDetailPage /> },
      { path: 'verify', element: <VerificationPage /> },
      { path: 'verify/:codeOrHash', element: <VerificationPage /> },
    ],
  },

  // Rutas de Invitados (Solo no autenticados)
  {
    element: (
      <GuestGuard>
        <AuthLayout />
      </GuestGuard>
    ),
    errorElement: <ErrorPage />,
    children: [
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
    ],
  },

  // Selección de Tenant (Autenticado sin requerir Tenant Guard)
  {
    element: (
      <AuthGuard>
        <AuthLayout />
      </AuthGuard>
    ),
    children: [
      { path: 'select-tenant', element: <TenantSelectPage /> },
    ],
  },

  // Rutas de Aplicación Autenticada y Protegida por Tenant
  {
    element: (
      <AuthGuard>
        <TenantGuard>
          <AppLayout />
        </TenantGuard>
      </AuthGuard>
    ),
    errorElement: <ErrorPage />,
    children: [
      { path: 'dashboard', element: <DashboardPage /> },

      // Rutas de Administración de Eventos (TENANT_ADMIN, ORGANIZER, TEACHER, SUPER_ADMIN)
      {
        path: 'events',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <AdminEventsPage />
          </RoleGuard>
        ),
      },
      {
        path: 'events/new',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <CreateEventPage />
          </RoleGuard>
        ),
      },
      {
        path: 'events/:id/edit',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <EditEventPage />
          </RoleGuard>
        ),
      },
      {
        path: 'events/:id/enrollments',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <EventEnrollmentsAdminPage />
          </RoleGuard>
        ),
      },

      // Otras Vistas Administrativas (Placeholder para siguientes fases)
      {
        path: 'users',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'SUPER_ADMIN']}>
            <EmptyState title="Módulo de Usuarios y Membresías" description="Próximamente: Administración de usuarios y asignación de roles." />
          </RoleGuard>
        ),
      },
      {
        path: 'roles',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'SUPER_ADMIN']}>
            <EmptyState title="Módulo de Roles y Permisos" description="Próximamente: Configuración de roles y permisos atómicos." />
          </RoleGuard>
        ),
      },
      {
        path: 'participants',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <EmptyState title="Módulo de Participantes" description="Próximamente: Padrón global de estudiantes." />
          </RoleGuard>
        ),
      },
      {
        path: 'credentials',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'CREDENTIAL_APPROVER', 'SUPER_ADMIN']}>
            <EmptyState title="Módulo de Credenciales" description="Próximamente: Emisión criptográfica y plantillas de certificados." />
          </RoleGuard>
        ),
      },
      {
        path: 'admin/payment-verifications',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'ORGANIZER', 'TEACHER', 'SUPER_ADMIN']}>
            <AdminPaymentVerificationsPage />
          </RoleGuard>
        ),
      },
      {
        path: 'audit',
        element: (
          <RoleGuard allowedRoles={['TENANT_ADMIN', 'AUDIT_REVIEWER', 'SUPER_ADMIN']}>
            <EmptyState title="Auditoría de Seguridad" description="Próximamente: Historial inmutable de eventos de auditoría." />
          </RoleGuard>
        ),
      },

      // Rutas PARTICIPANT
      {
        path: 'events/catalog',
        element: <EventCatalogPage />,
      },
      {
        path: 'events/catalog/:id',
        element: <EventPublicDetailPage />,
      },
      {
        path: 'my-events',
        element: (
          <RoleGuard allowedRoles={['PARTICIPANT', 'VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN']}>
            <MyEnrollmentsPage />
          </RoleGuard>
        ),
      },
      {
        path: 'my-enrollments',
        element: (
          <RoleGuard allowedRoles={['PARTICIPANT', 'VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN']}>
            <MyEnrollmentsPage />
          </RoleGuard>
        ),
      },
      {
        path: 'my-credentials',
        element: (
          <RoleGuard allowedRoles={['PARTICIPANT', 'VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN']}>
            <MyCredentialsPage />
          </RoleGuard>
        ),
      },

      // Rutas SUPER_ADMIN Global
      {
        path: 'admin/tenants',
        element: (
          <RoleGuard allowedRoles={['SUPER_ADMIN']}>
            <EmptyState title="Gestión de Organizaciones (Tenants)" description="Próximamente: Alta, suspensión y planes de organizaciones." />
          </RoleGuard>
        ),
      },
      {
        path: 'admin/plans',
        element: (
          <RoleGuard allowedRoles={['SUPER_ADMIN']}>
            <EmptyState title="Planes de Servicio" description="Próximamente: Definición de límites y características por plan." />
          </RoleGuard>
        ),
      },
      {
        path: 'admin/audit',
        element: (
          <RoleGuard allowedRoles={['SUPER_ADMIN']}>
            <EmptyState title="Auditoría Global de Plataforma" description="Próximamente: Logs globales de seguridad del sistema." />
          </RoleGuard>
        ),
      },

      { path: '403', element: <Forbidden403Page /> },
    ],
  },

  // 404 Not Found
  { path: '404', element: <NotFound404Page /> },
  { path: '*', element: <Navigate to="/404" replace /> },
]);
