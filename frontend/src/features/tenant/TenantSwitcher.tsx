import React, { useState } from 'react';
import { Building2, ChevronDown, Check } from 'lucide-react';
import { useTenantStore } from '../../store/useTenantStore';
import { authService } from '../auth/services/auth.service';
import { useAuthStore } from '../../store/useAuthStore';

export const TenantSwitcher: React.FC = () => {
  const { activeTenant, availableTenants, activeTenantId, setActiveContext } = useTenantStore();
  const { setSession } = useAuthStore();
  const [isOpen, setIsOpen] = useState(false);
  const [isSwitching, setIsSwitching] = useState(false);

  if (availableTenants.length === 0) return null;

  const handleSwitch = async (tenantId: string) => {
    if (tenantId === activeTenantId) {
      setIsOpen(false);
      return;
    }

    try {
      setIsSwitching(true);
      const authData = await authService.switchTenant(tenantId);
      setSession(authData);
      setActiveContext(authData.activeTenantId, authData.roles, authData.permissions);
      setIsOpen(false);
    } catch (err) {
      console.error('Error al cambiar de Tenant:', err);
    } finally {
      setIsSwitching(false);
    }
  };

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        disabled={isSwitching}
        className="btn btn-secondary"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          padding: '0.5rem 0.875rem',
          borderRadius: 'var(--radius-md)',
          fontSize: '0.875rem',
        }}
      >
        <Building2 size={18} className="text-primary" />
        <span style={{ fontWeight: 600, maxWidth: '160px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {activeTenant ? activeTenant.commercialName || activeTenant.legalName : 'Seleccionar Organización'}
        </span>
        <ChevronDown size={16} />
      </button>

      {isOpen && (
        <div
          className="glass-card"
          style={{
            position: 'absolute',
            top: 'calc(100% + 8px)',
            right: 0,
            width: '240px',
            padding: '0.5rem',
            zIndex: 100,
            backgroundColor: 'var(--bg-surface)',
          }}
        >
          <div style={{ padding: '0.5rem', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Organizaciones Disponibles
          </div>
          {availableTenants.map((t) => (
            <div
              key={t.tenantId}
              onClick={() => handleSwitch(t.tenantId)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.625rem 0.75rem',
                borderRadius: 'var(--radius-sm)',
                cursor: 'pointer',
                backgroundColor: t.tenantId === activeTenantId ? 'var(--primary-light)' : 'transparent',
                color: t.tenantId === activeTenantId ? 'var(--primary)' : 'var(--text-main)',
                fontWeight: t.tenantId === activeTenantId ? 600 : 400,
                fontSize: '0.875rem',
              }}
            >
              <span>{t.commercialName || t.legalName}</span>
              {t.tenantId === activeTenantId && <Check size={16} />}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
