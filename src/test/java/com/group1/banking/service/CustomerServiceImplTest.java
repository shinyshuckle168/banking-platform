package com.group1.banking.service;

import com.group1.banking.dto.customer.PatchCustomerRequest;
import com.group1.banking.exception.BadRequestException;
import com.group1.banking.mapper.CustomerMapper;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void updateShouldRejectImmutableFieldsBeforeDbCall() {
        PatchCustomerRequest request = new PatchCustomerRequest();
        request.setEmail("x@example.com");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> customerService.updateCustomer(1L, request));
        assertEquals("FIELD_NOT_UPDATABLE", ex.getCode());
    }
}
