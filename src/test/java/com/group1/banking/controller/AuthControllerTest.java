package com.group1.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.banking.dto.auth.AuthResponse;
import com.group1.banking.dto.auth.UserResponse;
import com.group1.banking.enums.RoleName;
import com.group1.banking.exception.GlobalExceptionHandler;
import com.group1.banking.security.CustomAccessDeniedHandler;
import com.group1.banking.security.CustomAuthenticationEntryPoint;
import com.group1.banking.security.JwtAuthenticationFilter;
import com.group1.banking.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @WithAnonymousUser
    void registerReturnsCreated() throws Exception {
        UserResponse response = new UserResponse();
        response.setUserId(UUID.randomUUID());
        response.setUsername("tarun@example.com");
        response.setRoles(List.of(RoleName.CUSTOMER));
        response.setExternalSubjectId(null);
        response.setCustomerId(null);
        response.setActive(true);
        response.setCreatedAt(Instant.now());

        when(authService.register(any())).thenReturn(response);

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "tarun@example.com");
        requestBody.put("password", "Secure@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("tarun@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithAnonymousUser
    void loginReturnsOk() throws Exception {
        AuthResponse response = new AuthResponse(
                "mock-access-token",
                "mock-refresh-token",
                "Bearer",
                3600
        );

        when(authService.login(any())).thenReturn(response);

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "tarun@example.com");
        requestBody.put("password", "Secure@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }
}