package com.bankapp.customer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bankapp.common.api.ApiException;
import com.bankapp.customer.api.UpdateCustomerRequest;
import com.bankapp.customer.domain.Customer;
import com.bankapp.customer.domain.CustomerType;
import com.bankapp.customer.repository.CustomerRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private com.bankapp.auth.repository.UserRepository userRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, userRepository);
    }

    @Test
    void rejectsImmutableFieldChanges() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> customerService.updateCustomer(
                        UUID.randomUUID(),
                        1L,
                        new UpdateCustomerRequest("Name", null, null, "customer@example.com", null, Instant.now())
                )
        );

        assertEquals("FIELD_NOT_UPDATABLE", exception.getCode());
    }

    @Test
    void rejectsStaleUpdateTimestamp() {
        UUID userId = UUID.randomUUID();
        Customer customer = new Customer(userId, "Jamie", "10 Main Street", CustomerType.PERSON);
        when(customerRepository.findByCustomerIdAndOwnerUserId(1L, userId)).thenReturn(Optional.of(customer));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> customerService.updateCustomer(
                        userId,
                        1L,
                        new UpdateCustomerRequest("Updated", null, null, null, null, Instant.now().minusSeconds(60))
                )
        );

        assertEquals("CUSTOMER_CONFLICT", exception.getCode());
    }
}
