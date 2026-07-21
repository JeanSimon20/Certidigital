package com.certidigital.platform.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Componente para la generación, parsing y validación de tokens JWT.
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;

    public JwtProvider(
        @Value("${jwt.secret:CertiDigitalSuperSecretKeyForJWTAuth2024Minimum256BitsLongSecretKey!}") String secret,
        @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs // Default: 15 minutos
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Genera un Access Token JWT con la información del usuario y contexto de Tenant activo.
     */
    public String generateAccessToken(
        String userId,
        String email,
        String fullName,
        String tenantId,
        List<String> roles,
        List<String> permissions
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", fullName);
        claims.put("tid", tenantId); // tenant_id activo (puede ser null si no hay tenant seleccionado)
        claims.put("roles", roles != null ? roles : Collections.emptyList());
        claims.put("permissions", permissions != null ? permissions : Collections.emptyList());

        return Jwts.builder()
            .subject(userId)
            .id(jti)
            .claims(claims)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    /**
     * Valida la firma y vigencia del JWT.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae las claims del token.
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getTenantIdFromToken(String token) {
        return getClaimsFromToken(token).get("tid", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        List<?> roles = getClaimsFromToken(token).get("roles", List.class);
        if (roles == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Object r : roles) {
            result.add(r.toString());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        List<?> permissions = getClaimsFromToken(token).get("permissions", List.class);
        if (permissions == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Object p : permissions) {
            result.add(p.toString());
        }
        return result;
    }
}
