import React from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { ShieldCheck, LogOut, Menu, User as UserIcon, Moon, Sun } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import { useTenantStore } from '../store/useTenantStore';
import { useUiStore } from '../store/useUiStore';
import { TenantSwitcher } from '../features/tenant/TenantSwitcher';
import { NotificationDropdown } from '../components/notification/NotificationDropdown';
import { getNavigationItems } from '../routes/navigation.config';
import { authService } from '../features/auth/services/auth.service';

export const AppLayout: React.FC = () => {
  const { user, logout } = useAuthStore();
  const { activeRoles } = useTenantStore();
  const { sidebarOpen, toggleSidebar, theme, toggleTheme } = useUiStore();
  const location = useLocation();
  const navigate = useNavigate();

  const userRoles = activeRoles.length > 0 ? activeRoles : user?.activeRoles || [];
  const navItems = getNavigationItems(userRoles);

  const handleLogout = async () => {
    await authService.logout();
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      {/* Sidebar Lateral */}
      <aside className={`app-sidebar ${!sidebarOpen ? 'collapsed' : ''}`}>
        <div style={{ height: '64px', padding: '0 1.25rem', display: 'flex', alignItems: 'center', gap: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          <ShieldCheck size={28} className="text-primary" style={{ color: 'var(--primary)', flexShrink: 0 }} />
          {sidebarOpen && (
            <span style={{ fontWeight: 800, fontSize: '1.125rem', fontFamily: 'var(--font-family-heading)' }}>
              Certi<span style={{ color: 'var(--primary)' }}>Digital</span>
            </span>
          )}
        </div>

        <nav style={{ flex: 1, padding: '1rem 0.75rem', display: 'flex', flexDirection: 'column', gap: '0.25rem', overflowY: 'auto' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  padding: '0.75rem 1rem',
                  borderRadius: 'var(--radius-md)',
                  textDecoration: 'none',
                  color: isActive ? 'var(--primary)' : 'var(--text-main)',
                  backgroundColor: isActive ? 'var(--primary-light)' : 'transparent',
                  fontWeight: isActive ? 600 : 500,
                  fontSize: '0.875rem',
                  transition: 'all var(--transition-fast)',
                }}
              >
                <Icon size={20} />
                {sidebarOpen && <span>{item.title}</span>}
              </Link>
            );
          })}
        </nav>

        {sidebarOpen && (
          <div style={{ padding: '1rem', borderTop: '1px solid var(--border-color)', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            CertiDigital v0.1.0 (SaaS)
          </div>
        )}
      </aside>

      {/* Main Content Area */}
      <div className="app-main">
        {/* Header Superior */}
        <header className="app-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <button onClick={toggleSidebar} className="btn btn-secondary" style={{ padding: '0.5rem' }}>
              <Menu size={20} />
            </button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <NotificationDropdown />

            <button onClick={toggleTheme} className="btn btn-secondary" style={{ padding: '0.5rem' }}>
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            <TenantSwitcher />

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', paddingLeft: '0.5rem', borderLeft: '1px solid var(--border-color)' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: 'var(--radius-full)',
                  backgroundColor: 'var(--primary-light)',
                  color: 'var(--primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 700,
                }}
              >
                <UserIcon size={18} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>{user?.fullName || 'Usuario'}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {userRoles[0] || 'Miembro'}
                </span>
              </div>
            </div>

            <button onClick={handleLogout} className="btn btn-secondary" title="Cerrar Sesión" style={{ padding: '0.5rem' }}>
              <LogOut size={18} className="text-danger" />
            </button>
          </div>
        </header>

        {/* Content Area */}
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
