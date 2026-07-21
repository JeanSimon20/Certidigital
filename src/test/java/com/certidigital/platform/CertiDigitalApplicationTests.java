package com.certidigital.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de integración que verifica la inicialización limpia de Spring Context,
 * la ejecución de migraciones Flyway y la validación de entidades JPA contra la BD H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class CertiDigitalApplicationTests {

    @Test
    void contextLoads() {
        // Si este test pasa, significa que:
        // 1. Spring Context levanta sin errores
        // 2. Flyway ejecuta todas las migraciones V1..V9 correctamente
        // 3. Hibernate valida todas las entidades JPA contra la estructura generada por Flyway
        // 4. No hay inconsistencias de tipos, nombres de columnas, ni FKs
    }

}
