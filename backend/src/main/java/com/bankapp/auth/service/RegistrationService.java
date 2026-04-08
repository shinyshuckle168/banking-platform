package com.bankapp.auth.service;

import com.bankapp.auth.api.RegisterRequest;
import com.bankapp.auth.api.UserResponse;
import com.bankapp.auth.domain.User;
import com.bankapp.auth.domain.UserRole;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        validatePassword(request.password());

        if (userRepository.existsByUsernameIgnoreCase(normalizedEmail)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "USER_ALREADY_EXISTS",
                    "A user with this email already exists.",
                    "email"
            );
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                Set.of(UserRole.CUSTOMER),
                true
        );

        return UserResponse.from(userRepository.save(user));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MISSING_REQUIRED_FIELD", "Email is required.", "email");
        }

        String normalized = email.trim().toLowerCase();
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_EMAIL_FORMAT", "Email format is invalid.", "email");
        }

        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MISSING_REQUIRED_FIELD", "Password is required.", "password");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "INVALID_PASSWORD_FORMAT",
                    "Password must be at least 8 characters and include an uppercase letter, digit, and special character.",
                    "password"
            );
        }
    }
}
