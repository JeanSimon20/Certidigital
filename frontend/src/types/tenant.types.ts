export interface TenantSummary {
  tenantId: string;
  legalName: string;
  commercialName: string;
  membershipStatus: string;
  servicePlan: string;
}

export interface TenantContextState {
  activeTenantId: string | null;
  activeTenant: TenantSummary | null;
  availableTenants: TenantSummary[];
  activeRoles: string[];
  activePermissions: string[];
}
