package com.group1.banking.dto.customer;

import com.group1.banking.enums.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerRequest {

    @NotBlank(message = "name is required")
    @Size(min = 2, message = "name must be at least 2 characters")
    private String name;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "type is required")
    private CustomerType type;
}
