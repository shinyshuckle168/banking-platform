package com.group1.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.banking.dto.customer.CreateCustomerRequest;
import com.group1.banking.dto.customer.CustomerResponse;
import com.group1.banking.dto.customer.PatchCustomerRequest;
import com.group1.banking.enums.CustomerType;
import com.group1.banking.exception.GlobalExceptionHandler;
import com.group1.banking.security.CustomAccessDeniedHandler;
import com.group1.banking.security.CustomAuthenticationEntryPoint;
import com.group1.banking.security.JwtAuthenticationFilter;
import com.group1.banking.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    void createCustomerReturnsCreated() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .customerId(1L)
                .name("Jane Doe")
                .address("123 Main St, Toronto, ON")
                .type(CustomerType.PERSON)
                .accounts(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(response);

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Jane Doe");
        requestBody.put("address", "123 Main St, Toronto, ON");
        requestBody.put("type", "PERSON");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.address").value("123 Main St, Toronto, ON"))
                .andExpect(jsonPath("$.type").value("PERSON"));
    }

    @Test
    void updateCustomerReturnsOk() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .customerId(1L)
                .name("Jane Smith")
                .address("456 New St, Toronto, ON")
                .type(CustomerType.PERSON)
                .accounts(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerService.updateCustomer(eq(1L), any(PatchCustomerRequest.class))).thenReturn(response);

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Jane Smith");
        requestBody.put("address", "456 New St, Toronto, ON");

        mockMvc.perform(patch("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.address").value("456 New St, Toronto, ON"));
    }

    @Test
    void getCustomerReturnsOk() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .customerId(1L)
                .name("Jane Doe")
                .address("123 Main St, Toronto, ON")
                .type(CustomerType.PERSON)
                .accounts(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerService.getCustomer(1L)).thenReturn(response);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.address").value("123 Main St, Toronto, ON"))
                .andExpect(jsonPath("$.type").value("PERSON"));
    }

    @Test
    void getAllCustomersReturnsOk() throws Exception {
        CustomerResponse customer1 = CustomerResponse.builder()
                .customerId(1L)
                .name("Jane Doe")
                .address("123 Main St, Toronto, ON")
                .type(CustomerType.PERSON)
                .accounts(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CustomerResponse customer2 = CustomerResponse.builder()
                .customerId(2L)
                .name("ABC Corp")
                .address("456 King St, Toronto, ON")
                .type(CustomerType.COMPANY)
                .accounts(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerService.getAllCustomers()).thenReturn(List.of(customer1, customer2));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].type").value("PERSON"))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].name").value("ABC Corp"))
                .andExpect(jsonPath("$[1].type").value("COMPANY"));
    }
}