package com.bankapp.customer.api;

import com.bankapp.customer.service.CustomerService;
import com.bankapp.customer.service.CustomerQueryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerQueryService customerQueryService;

    public CustomerController(CustomerService customerService, CustomerQueryService customerQueryService) {
        this.customerService = customerService;
        this.customerQueryService = customerQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request, Authentication authentication) {
        UUID userId = authentication != null && authentication.getPrincipal() instanceof UUID principal
                ? principal
                : null;
        return customerService.createCustomer(userId, request);
    }

    @PatchMapping("/{customerId}")
    public CustomerResponse updateCustomer(
            @PathVariable Long customerId,
            @RequestBody UpdateCustomerRequest request,
            Authentication authentication
    ) {
        UUID userId = authentication != null && authentication.getPrincipal() instanceof UUID principal
                ? principal
                : null;
        return customerService.updateCustomer(userId, customerId, request);
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable Long customerId, Authentication authentication) {
        UUID userId = authentication != null && authentication.getPrincipal() instanceof UUID principal
                ? principal
                : null;
        return customerQueryService.getCustomer(userId, customerId);
    }
}

