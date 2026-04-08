package com.bankapp.auth.api;

public record SessionResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
