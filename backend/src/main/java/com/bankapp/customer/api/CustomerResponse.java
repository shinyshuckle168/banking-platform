package com.bankapp.customer.api;

import com.bankapp.customer.domain.Customer;
import com.bankapp.customer.domain.CustomerType;
import java.time.Instant;

public record CustomerResponse(
        Long customerId,
        String name,
        String address,
        CustomerType type,
        Instant createdAt,
        Instant updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getAddress(),
                customer.getType(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
