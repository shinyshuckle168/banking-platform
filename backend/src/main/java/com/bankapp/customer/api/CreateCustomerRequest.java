package com.bankapp.customer.api;

import com.bankapp.customer.domain.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = "Name is required.")
        @Size(min = 2, message = "Name must be at least 2 characters.")
        String name,
        @NotBlank(message = "Address is required.")
        String address,
        @NotNull(message = "Customer type is required.")
        CustomerType type
) {
}
