package com.bankapp.auth.service;

import com.bankapp.auth.api.LoginRequest;
import com.bankapp.auth.api.SessionResponse;
import com.bankapp.auth.domain.User;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import com.bankapp.security.JwtTokenService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public SessionResponse authenticate(LoginRequest request) {
        String email = Optional.ofNullable(request.email()).orElse("").trim().toLowerCase();
        String password = Optional.ofNullable(request.password()).orElse("");

        if (email.isBlank() || password.isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MISSING_REQUIRED_FIELD", "Email and password are required.");
        }

        User user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "The supplied credentials are invalid."));

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "The account is inactive.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "The supplied credentials are invalid.");
        }

        return new SessionResponse(
                jwtTokenService.generateAccessToken(user.getUserId(), user.getRoles().stream().map(Enum::name).toList()),
                jwtTokenService.generateRefreshToken(user.getUserId(), user.getRoles().stream().map(Enum::name).toList()),
                "Bearer",
                jwtTokenService.getAccessTokenExpirySeconds()
        );
    }
}
