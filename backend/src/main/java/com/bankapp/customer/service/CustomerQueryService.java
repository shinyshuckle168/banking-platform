package com.bankapp.customer.service;

import com.bankapp.common.api.ApiException;
import com.bankapp.customer.api.CustomerResponse;
import com.bankapp.customer.repository.CustomerRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerQueryService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse getCustomer(UUID userId, Long customerId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORISED", "Access is not authorised for this request.");
        }

        return customerRepository.findByCustomerIdAndOwnerUserId(customerId, userId)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer was not found."));
    }
}
