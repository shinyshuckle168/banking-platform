package com.bankapp.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bankapp.auth.repository.UserRepository;
import com.bankapp.support.ApiIntegrationTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

class AuthIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerReturnsCreatedWithoutPasswordHash() throws Exception {
        MvcResult response = mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(toJson(java.util.Map.of("email", "customer@example.com", "password", "SecurePass1!"))))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertEquals(HttpStatus.CREATED.value(), response.getResponse().getStatus());
        assertEquals("customer@example.com", body.get("email").asText());
        assertFalse(body.has("passwordHash"));
        assertNotNull(body.get("userId"));
    }

    @Test
    void loginReturnsJwtTokensForRegisteredUser() throws Exception {
        registerUser("login@example.com", "SecurePass1!");

        MvcResult response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(toJson(java.util.Map.of("email", "login@example.com", "password", "SecurePass1!"))))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertEquals(HttpStatus.OK.value(), response.getResponse().getStatus());
        assertEquals("Bearer", body.get("tokenType").asText());
        assertFalse(body.get("accessToken").asText().isBlank());
        assertFalse(body.get("refreshToken").asText().isBlank());
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        registerUser("invalid@example.com", "SecurePass1!");

        MvcResult response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(toJson(java.util.Map.of("email", "invalid@example.com", "password", "WrongPass1!"))))
            .andExpect(status().isUnauthorized())
            .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getResponse().getStatus());
        assertEquals("INVALID_CREDENTIALS", body.get("code").asText());
    }
}
