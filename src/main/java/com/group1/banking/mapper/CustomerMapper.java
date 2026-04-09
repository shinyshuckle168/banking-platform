package com.group1.banking.mapper;

import com.group1.banking.dto.customer.CustomerResponse;
import com.group1.banking.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomerMapper {
    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .address(customer.getAddress())
                .type(customer.getType())
                .accounts(Collections.emptyList())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
