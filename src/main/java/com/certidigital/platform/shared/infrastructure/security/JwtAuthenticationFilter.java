package com.certidigital.platform.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JwtAuthenticationFilter — Filtro de autenticación HTTP.
 *
 * Extrae el JWT del Header `Authorization: Bearer <token>`,
 * construye las autoridades (roles y permisos), configura el SecurityContext
 * y establece el TenantContextHolder con el `tid` del token.
 *
 * CRÍTICO: Limpia siempre el TenantContextHolder en el bloque finally.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtProvider.validateToken(jwt)) {
                Claims claims = jwtProvider.getClaimsFromToken(jwt);
                String userId = claims.getSubject();
                String tenantId = claims.get("tid", String.class);

                List<String> roles = jwtProvider.getRolesFromToken(jwt);
                List<String> permissions = jwtProvider.getPermissionsFromToken(jwt);

                List<GrantedAuthority> authorities = new ArrayList<>();

                // Roles como ROLE_<nombre>
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }

                // Permisos como <resource>:<action> (ej: event:read)
                for (String perm : permissions) {
                    authorities.add(new SimpleGrantedAuthority(perm));
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Poblar TenantContext
                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContextHolder.setTenantId(tenantId);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Garantizar limpieza de ThreadLocal al terminar el request HTTP
            TenantContextHolder.clear();
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
