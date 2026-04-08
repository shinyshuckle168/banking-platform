package com.bankapp.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record SecurityProperties(
        @NotBlank String secret,
        @Min(1) long accessTokenExpiry,
        @Min(1) long refreshTokenExpiry
) {
}
