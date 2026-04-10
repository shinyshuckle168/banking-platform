package com.group1.banking.dto.customer;

import com.group1.banking.enums.CustomerType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchCustomerRequest {

    @Size(min = 2, message = "name must be at least 2 characters")
    private String name;

    private String address;

    private CustomerType type;

    // Explicitly blocked by the service layer per spec.
    private String email;
    private String accountNumber;
}
