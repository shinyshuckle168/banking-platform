package com.group1.banking.service.impl;

import com.group1.banking.dto.customer.CreateCustomerRequest;
import com.group1.banking.dto.customer.CustomerResponse;
import com.group1.banking.dto.customer.PatchCustomerRequest;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.User;
import com.group1.banking.exception.BadRequestException;
import com.group1.banking.exception.NotFoundException;
import com.group1.banking.mapper.CustomerMapper;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.UserRepository;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerMapper customerMapper,
                               UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.userRepository = userRepository;
    }

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new BadRequestException("UNAUTHORISED", "Authenticated user not found.", null);
        }

        UUID userId = principal.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found."));

        if (user.getCustomerId() != null) {
            throw new BadRequestException("CUSTOMER_ALREADY_LINKED", "This user already has a customer profile.", null);
        }

        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setAddress(request.getAddress().trim());
        customer.setType(request.getType());

        Customer savedCustomer = customerRepository.save(customer);

        user.setCustomerId(savedCustomer.getCustomerId());
        userRepository.save(user);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, PatchCustomerRequest request) {
        if (request.getEmail() != null || request.getAccountNumber() != null) {
            throw new BadRequestException("FIELD_NOT_UPDATABLE", "email and accountNumber cannot be updated.", null);
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found."));

        if (request.getName() != null) {
            customer.setName(request.getName().trim());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress().trim());
        }
        if (request.getType() != null) {
            customer.setType(request.getType());
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found."));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customerList = customerRepository.findAll();
        return customerList.stream()
                .map(customerMapper::toResponse)
                .toList();
    }
}