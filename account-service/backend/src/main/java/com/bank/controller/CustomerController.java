package com.bank.controller;

import com.bank.security.AuthenticatedUser;
import com.bank.service.CustomerService;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @DeleteMapping("/{customerId}")
    public Map<String, String> deleteCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        customerService.deleteCustomer(customerId, user);
        return Map.of("message", "Customer deleted successfully");
    }
}
