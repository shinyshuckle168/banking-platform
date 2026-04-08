package com.fdm.banking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Validates X-Api-Key header for service-to-service calls to /notifications/**. (T015)
 * SHA-256 hashes the key and validates against configured allowed-service-ids map.
 */
public class ServiceApiKeyFilter extends OncePerRequestFilter {

    private final Map<String, String> allowedServiceIds;

    public ServiceApiKeyFilter(Map<String, String> allowedServiceIds) {
        this.allowedServiceIds = allowedServiceIds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey == null || apiKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-Api-Key header");
            return;
        }

        String hashedKey = sha256(apiKey);
        String serviceId = allowedServiceIds.entrySet().stream()
                .filter(e -> e.getValue().equals(hashedKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (serviceId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
            return;
        }

        ServiceAuthentication auth = new ServiceAuthentication(serviceId);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
