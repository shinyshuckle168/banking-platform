package com.bankapp.customer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import com.bankapp.customer.api.CreateCustomerRequest;
import com.bankapp.customer.domain.CustomerType;
import com.bankapp.customer.repository.CustomerRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, userRepository);
    }

    @Test
    void rejectsUnauthorizedRequest() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> customerService.createCustomer(null, new CreateCustomerRequest("Jane", "10 Main Street", CustomerType.PERSON))
        );

        assertEquals("UNAUTHORISED", exception.getCode());
    }

    @Test
    void rejectsMissingFields() {
        UUID userId = UUID.randomUUID();

        ApiException exception = assertThrows(
                ApiException.class,
                () -> customerService.createCustomer(userId, new CreateCustomerRequest("", "10 Main Street", CustomerType.PERSON))
        );

        assertEquals("MISSING_REQUIRED_FIELD", exception.getCode());
    }

    @Test
    void rejectsInvalidType() {
        UUID userId = UUID.randomUUID();

        ApiException exception = assertThrows(
                ApiException.class,
                () -> customerService.createCustomer(userId, new CreateCustomerRequest("Jane", "10 Main Street", null))
        );

        assertEquals("INVALID_CUSTOMER_TYPE", exception.getCode());
    }
}
