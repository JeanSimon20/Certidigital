package com.certidigital.platform.shared.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TenantContextHolder — Gestiona el contexto del Tenant activo por hilo HTTP (ThreadLocal).
 *
 * Muestra el tenant_id activo de la solicitud actual.
 * Importante: Debe limpiarse en el bloque finally de cada solicitud HTTP.
 */
public final class TenantContextHolder {

    private static final Logger log = LoggerFactory.getLogger(TenantContextHolder.class);

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {}

    /**
     * Establece el Tenant ID activo para el hilo actual.
     */
    public static void setTenantId(String tenantId) {
        log.trace("Estableciendo TenantContext: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Obtiene el Tenant ID activo para el hilo actual.
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Exige que exista un Tenant ID en el hilo actual.
     * Lanzará una excepción si no hay Tenant seleccionado.
     */
    public static String requireTenantId() {
        String tenantId = getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new TenantAccessDeniedException("No se ha seleccionado ningún Tenant activo para realizar esta operación.");
        }
        return tenantId;
    }

    /**
     * Limpia el ThreadLocal para prevenir fugas de contexto entre solicitudes.
     */
    public static void clear() {
        log.trace("Limpiando TenantContext para hilo: {}", Thread.currentThread().getName());
        CURRENT_TENANT.remove();
    }
}
