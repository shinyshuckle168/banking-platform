package com.bankapp.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email format is invalid.")
        String email,
        @NotBlank(message = "Password is required.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must include an uppercase letter, a digit, and a special character."
        )
        String password
) {
}
