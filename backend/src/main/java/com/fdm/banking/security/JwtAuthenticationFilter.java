package com.fdm.banking.security;

import com.fdm.banking.entity.User;
import com.fdm.banking.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JWT authentication filter — compatible with Group 1's token format.
 *
 * Group 1 tokens carry:
 *   sub   — UUID string (userId)
 *   roles — List<String>  e.g. ["CUSTOMER"]
 *
 * The filter resolves the user from our local database using the sub value,
 * derives permissions from the stored RoleEntity, and builds a UserPrincipal
 * that all five Group 3 services consume unchanged.
 *
 * Backward-compatibility: if sub is not found by externalSubjectId (e.g.
 * "testuser" from a jwt.io test token), we fall back to findByUsername(sub)
 * so local development tokens still work.
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
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid or expired token — proceed unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // Step 1 — sub is Group 1's UUID (or local dev username)
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2 — Look up user by externalSubjectId first, then username fallback
        Optional<User> userOpt = userRepository.findByExternalSubjectId(sub);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(sub);
        }

        if (userOpt.isEmpty()) {
            // User not in our database — proceed unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        User user = userOpt.get();

        // Step 3 — Reject inactive users
        if (!user.isEnabled()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Step 4 — Derive customerId from DB (Long)
        Long customerId = user.getCustomerId();

        // Step 5 — Derive role name and permissions from the stored RoleEntity
        String roleName = (user.getRole() != null) ? user.getRole().getName() : null;
        List<String> roles = (roleName != null) ? List.of(roleName) : Collections.emptyList();

        String permissionsStr = (user.getRole() != null) ? user.getRole().getPermissions() : null;
        Collection<String> permissions = (permissionsStr != null && !permissionsStr.isBlank())
                ? Arrays.asList(permissionsStr.split(","))
                : Collections.emptyList();

        // Step 6 — Build principal and set authentication
        UserPrincipal principal = new UserPrincipal(sub, user.getUsername(), roles, permissions, customerId);
        JwtAuthenticationToken auth = new JwtAuthenticationToken(principal);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
