package com.bankapp.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankapp.auth.api.RegisterRequest;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(userRepository, passwordEncoder);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByUsernameIgnoreCase("customer@example.com")).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> registrationService.register(new RegisterRequest("customer@example.com", "SecurePass1!"))
        );

        assertEquals("USER_ALREADY_EXISTS", exception.getCode());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsInvalidEmail() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> registrationService.register(new RegisterRequest("invalid-email", "SecurePass1!"))
        );

        assertEquals("INVALID_EMAIL_FORMAT", exception.getCode());
    }

    @Test
    void rejectsWeakPassword() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> registrationService.register(new RegisterRequest("customer@example.com", "weakpass"))
        );

        assertEquals("INVALID_PASSWORD_FORMAT", exception.getCode());
    }

    @Test
    void rejectsMissingFields() {
        ApiException emailException = assertThrows(
                ApiException.class,
                () -> registrationService.register(new RegisterRequest("", "SecurePass1!"))
        );
        ApiException passwordException = assertThrows(
                ApiException.class,
                () -> registrationService.register(new RegisterRequest("customer@example.com", ""))
        );

        assertEquals("MISSING_REQUIRED_FIELD", emailException.getCode());
        assertEquals("MISSING_REQUIRED_FIELD", passwordException.getCode());
    }
}
