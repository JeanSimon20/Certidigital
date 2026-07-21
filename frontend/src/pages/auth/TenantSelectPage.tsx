import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, ChevronRight } from 'lucide-react';
import { useTenantStore } from '../../store/useTenantStore';
import { useAuthStore } from '../../store/useAuthStore';
import { authService } from '../../features/auth/services/auth.service';
import { Spinner } from '../../components/feedback/Spinner';

export const TenantSelectPage: React.FC = () => {
  const navigate = useNavigate();
  const { availableTenants, setActiveContext } = useTenantStore();
  const { setSession } = useAuthStore();
  const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSelectTenant = async (tenantId: string) => {
    setSelectedTenantId(tenantId);
    setIsLoading(true);

    try {
      const response = await authService.switchTenant(tenantId);
      setSession(response);
      setActiveContext(response.activeTenantId, response.roles, response.permissions);
      navigate('/dashboard');
    } catch (err) {
      console.error('Error seleccionando Tenant:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.25rem' }}>Seleccione una Organización</h3>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
        Su cuenta posee membresías activas en múltiples organizaciones. Seleccione con cuál desea operar.
      </p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
        {availableTenants.map((t) => (
          <div
            key={t.tenantId}
            onClick={() => !isLoading && handleSelectTenant(t.tenantId)}
            className="glass-card"
            style={{
              padding: '1rem 1.25rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              border: selectedTenantId === t.tenantId ? '2px solid var(--primary)' : '1px solid var(--border-color)',
              transition: 'all var(--transition-fast)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.875rem' }}>
              <div
                style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--primary-light)',
                  color: 'var(--primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Building2 size={20} />
              </div>
              <div>
                <h4 style={{ fontSize: '0.938rem', fontWeight: 600 }}>{t.commercialName || t.legalName}</h4>
                <span className="badge badge-primary" style={{ marginTop: '0.25rem' }}>
                  {t.servicePlan}
                </span>
              </div>
            </div>

            {isLoading && selectedTenantId === t.tenantId ? (
              <Spinner size={18} />
            ) : (
              <ChevronRight size={18} style={{ color: 'var(--text-muted)' }} />
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
