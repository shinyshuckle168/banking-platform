package com.bankapp.customer.service;

import com.bankapp.auth.domain.User;
import com.bankapp.auth.repository.UserRepository;
import com.bankapp.common.api.ApiException;
import com.bankapp.customer.api.CreateCustomerRequest;
import com.bankapp.customer.api.CustomerResponse;
import com.bankapp.customer.api.UpdateCustomerRequest;
import com.bankapp.customer.domain.Customer;
import com.bankapp.customer.repository.CustomerRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(UUID userId, CreateCustomerRequest request) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORISED", "Access is not authorised for this request.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MISSING_REQUIRED_FIELD", "Name is required.", "name");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "MISSING_REQUIRED_FIELD", "Address is required.", "address");
        }
        if (request.type() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CUSTOMER_TYPE", "Customer type is invalid.", "type");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORISED", "Access is not authorised for this request."));

        Customer customer = new Customer(userId, request.name().trim(), request.address().trim(), request.type());
        Customer savedCustomer = customerRepository.save(customer);
        user.setCustomerId(savedCustomer.getCustomerId());
        userRepository.save(user);

        return CustomerResponse.from(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID userId, Long customerId, UpdateCustomerRequest request) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORISED", "Access is not authorised for this request.");
        }
        if (request.email() != null || request.accountNumber() != null) {
            String field = request.email() != null ? "email" : "accountNumber";
            throw new ApiException(HttpStatus.BAD_REQUEST, "FIELD_NOT_UPDATABLE", "Immutable customer fields cannot be updated.", field);
        }

        Customer customer = customerRepository.findByCustomerIdAndOwnerUserId(customerId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer was not found."));

        Instant requestTimestamp = request.updatedAt();
        if (requestTimestamp == null || !requestTimestamp.equals(customer.getUpdatedAt())) {
            throw new ApiException(HttpStatus.CONFLICT, "CUSTOMER_CONFLICT", "Customer state is stale. Refresh and try again.");
        }

        if (request.name() != null && !request.name().isBlank()) {
            customer.setName(request.name().trim());
        }
        if (request.address() != null && !request.address().isBlank()) {
            customer.setAddress(request.address().trim());
        }
        if (request.type() != null) {
            customer.setType(request.type());
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }
}

