package com.bankapp.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bankapp.auth.api.LoginRequest;
import com.bankapp.auth.api.SessionResponse;
import com.bankapp.auth.domain.User;
import com.bankapp.auth.domain.UserRole;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import com.bankapp.config.SecurityProperties;
import com.bankapp.security.JwtTokenService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        JwtTokenService jwtTokenService = new JwtTokenService(new SecurityProperties(
                "0123456789012345678901234567890123456789012345678901234567890123",
                3600,
                604800
        ));
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void rejectsBadCredentialsForUnknownUser() {
        when(userRepository.findByUsernameIgnoreCase("customer@example.com")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authenticationService.authenticate(new LoginRequest("customer@example.com", "SecurePass1!"))
        );

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
    }

    @Test
    void rejectsInactiveUser() {
        User user = new User("customer@example.com", "encoded", Set.of(UserRole.CUSTOMER), false);
        when(userRepository.findByUsernameIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authenticationService.authenticate(new LoginRequest("customer@example.com", "SecurePass1!"))
        );

        assertEquals("ACCOUNT_INACTIVE", exception.getCode());
    }

    @Test
    void rejectsWrongPasswordWithGenericFailure() {
        User user = new User("customer@example.com", "encoded", Set.of(UserRole.CUSTOMER), true);
        when(userRepository.findByUsernameIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1!", "encoded")).thenReturn(false);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authenticationService.authenticate(new LoginRequest("customer@example.com", "WrongPass1!"))
        );

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
    }

    @Test
    void generatesJwtClaimsForSubjectRolesAndExpiry() {
        User user = new User("customer@example.com", "encoded", Set.of(UserRole.CUSTOMER), true);
        when(userRepository.findByUsernameIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass1!", "encoded")).thenReturn(true);

        SessionResponse session = authenticationService.authenticate(new LoginRequest("customer@example.com", "SecurePass1!"));

        assertTrue(session.accessToken().length() > 20);
        assertEquals("Bearer", session.tokenType());
        assertEquals(3600, session.expiresIn());
    }
}
