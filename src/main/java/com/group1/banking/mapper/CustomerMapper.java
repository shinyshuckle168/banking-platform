package com.group1.banking.mapper;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.group1.banking.dto.customer.CustomerResponse;
import com.group1.banking.entity.Customer;

import lombok.Builder;
import lombok.Data;

@Builder
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
