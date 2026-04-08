package com.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = headerOrDefault(request, "X-User-Id", "demo-user");
        Long customerId = parseLong(request.getHeader("X-Customer-Id"));
        List<String> roles = splitHeader(request.getHeader("X-Roles"));
        if (roles.isEmpty()) {
            roles = List.of("CUSTOMER");
        }
        List<String> permissions = derivePermissions(roles, splitHeader(request.getHeader("X-Permissions")));
        AuthenticatedUser principal = new AuthenticatedUser(userId, customerId, roles, permissions);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, authorization.substring(7), principal.authorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String headerOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    private List<String> splitHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<String> derivePermissions(List<String> roles, List<String> explicitPermissions) {
        Set<String> permissions = new LinkedHashSet<>(explicitPermissions);
        for (String role : roles) {
            if ("ADMIN".equals(role)) {
                permissions.addAll(List.of(
                        "CUSTOMER:DELETE",
                        "CUSTOMER:READ",
                        "ACCOUNT:CREATE",
                        "ACCOUNT:READ",
                        "ACCOUNT:UPDATE",
                        "ACCOUNT:DELETE",
                        "TRANSACTION:DEPOSIT",
                        "TRANSACTION:WITHDRAW",
                        "TRANSACTION:TRANSFER"));
            }
            if ("CUSTOMER".equals(role)) {
                permissions.addAll(List.of(
                        "CUSTOMER:READ",
                        "ACCOUNT:CREATE",
                        "ACCOUNT:READ",
                        "ACCOUNT:UPDATE",
                        "TRANSACTION:DEPOSIT",
                        "TRANSACTION:WITHDRAW",
                        "TRANSACTION:TRANSFER"));
            }
        }
        return new ArrayList<>(permissions);
    }
}
