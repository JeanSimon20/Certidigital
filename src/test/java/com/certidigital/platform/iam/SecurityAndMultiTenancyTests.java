package com.certidigital.platform.iam;

import com.certidigital.platform.shared.infrastructure.security.JwtProvider;
import com.certidigital.platform.shared.infrastructure.security.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndMultiTenancyTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("1. Registro correcto de usuario")
    void test1_registerSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "nuevo.usuario@unitech.edu.hn");
        request.put("fullName", "Nuevo Usuario");
        request.put("password", "SecurePassword123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("nuevo.usuario@unitech.edu.hn"))
            .andExpect(jsonPath("$.data.fullName").value("Nuevo Usuario"));
    }

    @Test
    @DisplayName("2. Registro con email duplicado")
    void test2_registerDuplicateEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "dup.user@unitech.edu.hn");
        request.put("fullName", "Usuario Duplicado");
        request.put("password", "Password123!");

        // Primer registro exitoso
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Intento duplicado
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("El email 'dup.user@unitech.edu.hn' ya se encuentra registrado en el sistema."));
    }

    @Test
    @DisplayName("3. Login correcto")
    void test3_loginSuccess() throws Exception {
        // Registrar usuario
        Map<String, String> reg = new HashMap<>();
        reg.put("email", "login.test@unitech.edu.hn");
        reg.put("fullName", "Login Test User");
        reg.put("password", "LoginPass123!");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reg))).andExpect(status().isCreated());

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", "login.test@unitech.edu.hn");
        login.put("password", "LoginPass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.email").value("login.test@unitech.edu.hn"));
    }

    @Test
    @DisplayName("3b. Login con usuario semilla superadmin")
    void testSeedUsersLogin() throws Exception {
        Map<String, String> login = new HashMap<>();
        login.put("email", "superadmin@certidigital.com");
        login.put("password", "SuperAdmin@2024!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("superadmin@certidigital.com"));
    }

    @Test
    @DisplayName("3c. Catálogo público de eventos responde 200 OK")
    void testPublicEventCatalog() throws Exception {
        mockMvc.perform(get("/api/events/public"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("4. Login con password incorrecta")
    void test4_loginBadPassword() throws Exception {
        Map<String, String> login = new HashMap<>();
        login.put("email", "login.test@unitech.edu.hn");
        login.put("password", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("5. JWT válido permite acceso a /api/users/me")
    void test5_validJwtAccess() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc", "admin@unitech.edu.hn", "Dr. Carlos Mendoza", "tenant-001-aaaa-bbbb-cccc-dddddddd", List.of("TENANT_ADMIN"), List.of("user:read")
        );

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("6. JWT expirado o inválido niega acceso")
    void test6_expiredJwtAccess() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidtokenbody.invalid";

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("7. Refresh Token válido renueva el Access Token")
    void test7_refreshTokenValid() throws Exception {
        // Register & Login
        Map<String, String> reg = new HashMap<>();
        reg.put("email", "refresh.test@unitech.edu.hn");
        reg.put("fullName", "Refresh Test");
        reg.put("password", "RefreshPass123!");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reg)));

        Map<String, String> login = new HashMap<>();
        login.put("email", "refresh.test@unitech.edu.hn");
        login.put("password", "RefreshPass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(json).get("data").get("refreshToken").asText();

        // Refresh
        Map<String, String> refreshReq = new HashMap<>();
        refreshReq.put("refreshToken", refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("8. Refresh Token inválido niega renovación")
    void test8_refreshTokenInvalid() throws Exception {
        Map<String, String> refreshReq = new HashMap<>();
        refreshReq.put("refreshToken", "invalid-refresh-token-uuid");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("9. Usuario obtiene sus Tenants disponibles")
    void test9_getUserTenants() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc", "admin@unitech.edu.hn", "Dr. Carlos Mendoza", null, List.of("TENANT_ADMIN"), Collections.emptyList()
        );

        mockMvc.perform(get("/api/users/me/tenants")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].tenantId").value("tenant-001-aaaa-bbbb-cccc-dddddddd"));
    }

    @Test
    @DisplayName("10. Usuario cambia correctamente de Tenant")
    void test10_switchTenantSuccess() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc", "admin@unitech.edu.hn", "Dr. Carlos Mendoza", null, List.of("TENANT_ADMIN"), Collections.emptyList()
        );

        Map<String, String> switchReq = new HashMap<>();
        switchReq.put("tenantId", "tenant-001-aaaa-bbbb-cccc-dddddddd");

        mockMvc.perform(post("/api/auth/switch-tenant")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(switchReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.activeTenantId").value("tenant-001-aaaa-bbbb-cccc-dddddddd"))
            .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("11. Usuario intenta cambiar a Tenant donde no pertenece (Acceso denegado)")
    void test11_switchTenantUnauthorized() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc", "admin@unitech.edu.hn", "Dr. Carlos Mendoza", null, List.of("TENANT_ADMIN"), Collections.emptyList()
        );

        Map<String, String> switchReq = new HashMap<>();
        switchReq.put("tenantId", "tenant-non-existent-9999");

        mockMvc.perform(post("/api/auth/switch-tenant")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(switchReq)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("12. Tenant Admin accede a su propio Tenant")
    void test12_tenantAdminAccessOwnTenant() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc",
            "admin@unitech.edu.hn",
            "Dr. Carlos Mendoza",
            "tenant-001-aaaa-bbbb-cccc-dddddddd",
            List.of("TENANT_ADMIN"),
            List.of("user:read", "event:create")
        );

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.activeTenantId").value("tenant-001-aaaa-bbbb-cccc-dddddddd"));
    }

    @Test
    @DisplayName("13. Tenant Admin intenta cambiar a otro Tenant sin membresía")
    void test13_tenantAdminCrossTenantDenied() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc",
            "admin@unitech.edu.hn",
            "Dr. Carlos Mendoza",
            "tenant-001-aaaa-bbbb-cccc-dddddddd",
            List.of("TENANT_ADMIN"),
            List.of("user:read")
        );

        Map<String, String> switchReq = new HashMap<>();
        switchReq.put("tenantId", "tenant-other-university-9999");

        mockMvc.perform(post("/api/auth/switch-tenant")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(switchReq)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("14. Solicitud no autenticada niega acceso")
    void test14_unauthenticatedRequestDenied() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("15. SUPER_ADMIN obtiene su perfil correctamente")
    void test15_superAdminGlobalAccess() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-superadmin-0000000000000000000",
            "superadmin@certidigital.com",
            "Super Administrator",
            null,
            List.of("SUPER_ADMIN"),
            List.of("tenant:create", "tenant:read", "tenant:suspend")
        );

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("superadmin@certidigital.com"));
    }

    @Test
    @DisplayName("16. Verificación de limpieza del TenantContext (ThreadLocal)")
    void test16_tenantContextCleanup() throws Exception {
        String token = jwtProvider.generateAccessToken(
            "user-admin-0001-aaaa-bbbb-cccccccc",
            "admin@unitech.edu.hn",
            "Dr. Carlos Mendoza",
            "tenant-001-aaaa-bbbb-cccc-dddddddd",
            List.of("TENANT_ADMIN"),
            List.of("user:read")
        );

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Después de la finalización del request HTTP, el ThreadLocal del hilo actual DEBE ser null
        assertNull(TenantContextHolder.getTenantId(), "El TenantContext must be cleared post HTTP request");
    }
}
