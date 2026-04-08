package com.bankapp.security;

import com.bankapp.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final SecurityProperties securityProperties;
    private final SecretKey signingKey;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.signingKey = Keys.hmacShaKeyFor(resolveKeyBytes(securityProperties.secret()));
    }

    public String generateAccessToken(UUID userId, Collection<String> roles) {
        return buildToken(userId, roles, securityProperties.accessTokenExpiry());
    }

    public String generateRefreshToken(UUID userId, Collection<String> roles) {
        return buildToken(userId, roles, securityProperties.refreshTokenExpiry());
    }

    public long getAccessTokenExpirySeconds() {
        return securityProperties.accessTokenExpiry();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractSubject(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof Collection<?> values) {
            return values.stream().map(String::valueOf).toList();
        }

        return List.of();
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean isTokenValid(String token) {
        try {
            return extractExpiration(token).isAfter(Instant.now());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String buildToken(UUID userId, Collection<String> roles, long expirySeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirySeconds);

        return Jwts.builder()
                .subject(userId.toString())
                .claims(Map.of("roles", List.copyOf(roles)))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    private static byte[] resolveKeyBytes(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ignored) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
