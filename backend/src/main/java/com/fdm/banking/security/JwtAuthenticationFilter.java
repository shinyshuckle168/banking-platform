package com.fdm.banking.security;

import com.fdm.banking.entity.UserEntity;
import com.fdm.banking.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter — reused from Group 2.
 * Validates Bearer JWT and populates SecurityContext with UserPrincipal.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(String jwtSecret, UserRepository userRepository) {
        this.jwtSecret = jwtSecret;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String permissionsStr = claims.get("permissions", String.class);
            Long customerId = claims.get("customerId", Long.class);

            Collection<String> permissions = (permissionsStr != null && !permissionsStr.isBlank())
                    ? Arrays.asList(permissionsStr.split(","))
                    : Collections.emptyList();

            UserPrincipal principal = new UserPrincipal(userId, username, role, permissions, customerId);
            JwtAuthenticationToken auth = new JwtAuthenticationToken(principal);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            // Invalid token — proceed without authentication
             System.out.println("JWT ERROR: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
