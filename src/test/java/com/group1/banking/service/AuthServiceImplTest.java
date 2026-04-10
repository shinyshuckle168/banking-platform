package com.group1.banking.service;

import com.group1.banking.dto.auth.LoginRequest;
import com.group1.banking.dto.auth.RegisterRequest;
import com.group1.banking.entity.User;
import com.group1.banking.enums.RoleName;
import com.group1.banking.exception.ConflictException;
import com.group1.banking.exception.UnauthorisedException;
import com.group1.banking.mapper.UserMapper;
import com.group1.banking.repository.UserRepository;
import com.group1.banking.security.JwtProperties;
import com.group1.banking.security.JwtService;
import com.group1.banking.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("Tarun@example.com");
        registerRequest.setPassword("Secure@123");
    }

    @Test
    void registerShouldThrowWhenUserExists() {
        when(userRepository.existsByUsernameIgnoreCase("tarun@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.register(registerRequest));
    }

    @Test
    void loginShouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("tarun@example.com");
        request.setPassword("Wrong@123");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("tarun@example.com");
        user.setPasswordHash("encoded");
        user.setRoles(List.of(RoleName.CUSTOMER));
        user.setActive(true);

        when(userRepository.findByUsernameIgnoreCase("tarun@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong@123", "encoded")).thenReturn(false);

        UnauthorisedException ex = assertThrows(UnauthorisedException.class, () -> authService.login(request));
        assertEquals("INVALID_CREDENTIALS", ex.getCode());
    }
}
