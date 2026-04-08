package com.bankapp.customer.api;

import com.bankapp.customer.domain.CustomerType;
import java.time.Instant;

public record UpdateCustomerRequest(
        String name,
        String address,
        CustomerType type,
        String email,
        String accountNumber,
        Instant updatedAt
) {
}
