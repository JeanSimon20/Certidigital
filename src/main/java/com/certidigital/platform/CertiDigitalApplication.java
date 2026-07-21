package com.certidigital.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CertiDigital Platform — Punto de entrada de la aplicación.
 *
 * Plataforma de Gestión de Credenciales Digitales Multi-Tenant.
 * Arquitectura: Monolito Modular con Spring Boot 3.3 / Java 21.
 *
 * Fases de implementación:
 *   FASE 0: Fundaciones (activa)
 *   FASE 1: Auth + Tenants
 *   FASE 2: IAM + Organización
 *   ...
 */
@SpringBootApplication
public class CertiDigitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertiDigitalApplication.class, args);
    }

}
