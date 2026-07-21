import { create } from 'zustand';
import { TenantSummary } from '../types/tenant.types';

interface TenantState {
  activeTenantId: string | null;
  activeTenant: TenantSummary | null;
  availableTenants: TenantSummary[];
  activeRoles: string[];
  activePermissions: string[];

  // Actions
  setActiveTenantId: (tenantId: string | null) => void;
  setAvailableTenants: (tenants: TenantSummary[]) => void;
  setActiveContext: (tenantId: string | null, roles: string[], permissions: string[]) => void;
  clearTenant: () => void;
}

const ACTIVE_TENANT_KEY = 'certidigital_active_tenant_id';

export const useTenantStore = create<TenantState>((set, get) => ({
  activeTenantId: localStorage.getItem(ACTIVE_TENANT_KEY),
  activeTenant: null,
  availableTenants: [],
  activeRoles: [],
  activePermissions: [],

  setActiveTenantId: (tenantId: string | null) => {
    if (tenantId) {
      localStorage.setItem(ACTIVE_TENANT_KEY, tenantId);
    } else {
      localStorage.removeItem(ACTIVE_TENANT_KEY);
    }

    const tenant = get().availableTenants.find((t) => t.tenantId === tenantId) || null;
    set({ activeTenantId: tenantId, activeTenant: tenant });
  },

  setAvailableTenants: (tenants: TenantSummary[]) => {
    const activeId = get().activeTenantId;
    const activeTenant = tenants.find((t) => t.tenantId === activeId) || null;
    set({ availableTenants: tenants, activeTenant });
  },

  setActiveContext: (tenantId: string | null, roles: string[], permissions: string[]) => {
    if (tenantId) {
      localStorage.setItem(ACTIVE_TENANT_KEY, tenantId);
    } else {
      localStorage.removeItem(ACTIVE_TENANT_KEY);
    }
    const tenant = get().availableTenants.find((t) => t.tenantId === tenantId) || null;
    set({
      activeTenantId: tenantId,
      activeTenant: tenant,
      activeRoles: roles,
      activePermissions: permissions,
    });
  },

  clearTenant: () => {
    localStorage.removeItem(ACTIVE_TENANT_KEY);
    set({
      activeTenantId: null,
      activeTenant: null,
      availableTenants: [],
      activeRoles: [],
      activePermissions: [],
    });
  },
}));
