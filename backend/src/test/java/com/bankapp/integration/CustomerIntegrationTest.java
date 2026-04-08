package com.bankapp.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bankapp.auth.repository.UserRepository;
import com.bankapp.customer.repository.CustomerRepository;
import com.bankapp.support.ApiIntegrationTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

class CustomerIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void customerEndpointsSupportCreateGetAndPatchFlow() throws Exception {
        registerUser("flow@example.com", "SecurePass1!");
        String token = loginUser("flow@example.com", "SecurePass1!").get("accessToken").asText();

        MvcResult createResponse = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(toJson(Map.of("name", "Jamie Customer", "address", "10 Main Street", "type", "PERSON"))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        Long customerId = created.get("customerId").asLong();
        String updatedAt = created.get("updatedAt").asText();

        MvcResult getResponse = mockMvc.perform(get("/api/customers/{customerId}", customerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode fetched = objectMapper.readTree(getResponse.getResponse().getContentAsString());
        MvcResult patchResponse = mockMvc.perform(patch("/api/customers/{customerId}", customerId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "name", "Jamie Customer Updated",
                                "address", "20 Main Street",
                                "type", "COMPANY",
                                "updatedAt", updatedAt
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode patched = objectMapper.readTree(patchResponse.getResponse().getContentAsString());

        assertEquals(HttpStatus.CREATED.value(), createResponse.getResponse().getStatus());
        assertEquals(HttpStatus.OK.value(), getResponse.getResponse().getStatus());
        assertEquals(HttpStatus.OK.value(), patchResponse.getResponse().getStatus());
        assertEquals("Jamie Customer", fetched.get("name").asText());
        assertEquals("Jamie Customer Updated", patched.get("name").asText());
                assertEquals("20 Main Street", patched.get("address").asText());
                assertEquals("COMPANY", patched.get("type").asText());
    }

    @Test
    void createCustomerWithoutTokenReturnsJsonUnauthorized() throws Exception {
        MvcResult response = mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(toJson(Map.of("name", "Jamie Customer", "address", "10 Main Street", "type", "PERSON"))))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getResponse().getStatus());
        assertEquals("UNAUTHORISED", body.get("code").asText());
    }

    @Test
    void stalePatchReturnsConflict() throws Exception {
        registerUser("stale@example.com", "SecurePass1!");
        String token = loginUser("stale@example.com", "SecurePass1!").get("accessToken").asText();

        JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(toJson(Map.of("name", "Jamie Customer", "address", "10 Main Street", "type", "PERSON"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        MvcResult response = mockMvc.perform(patch("/api/customers/{customerId}", created.get("customerId").asLong())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "name", "Stale Update",
                                "updatedAt", "2000-01-01T00:00:00.000Z"
                        ))))
                .andExpect(status().isConflict())
                .andReturn();

        JsonNode body = objectMapper.readTree(response.getResponse().getContentAsString());
        assertEquals(HttpStatus.CONFLICT.value(), response.getResponse().getStatus());
        assertEquals("CUSTOMER_CONFLICT", body.get("code").asText());
    }
}
