package com.group1.banking.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.banking.dto.customer.AccountResponse;
import com.group1.banking.dto.customer.CreateCustomerRequest;
import com.group1.banking.dto.customer.CustomerResponse;
import com.group1.banking.dto.customer.PatchCustomerRequest;
import com.group1.banking.entity.AccountStatus;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.User;
import com.group1.banking.exception.BadRequestException;
import com.group1.banking.exception.ConflictException;
import com.group1.banking.exception.NotFoundException;
import com.group1.banking.exception.UnauthorisedException;
import com.group1.banking.mapper.CustomerMapper;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.UserRepository;
import com.group1.banking.security.AuthenticatedUser;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.CustomerService;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerMapper customerMapper,
                               UserRepository userRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new BadRequestException("UNAUTHORISED", "Authenticated user not found.", null);
        }

        UUID userId = principal.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found.", Map.of("uderId", userId)));

        if (user.getCustomerId() != null) {
            throw new BadRequestException("CUSTOMER_ALREADY_LINKED", "This user already has a customer profile.", Map.of(
                    "userId", user.getUserId(),
                    "customerId", user.getCustomerId()
                ));
        }

        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setAddress(request.getAddress().trim());
        customer.setType(request.getType());
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
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
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found.", Map.of("customerId", customerId)));

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
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found.", Map.of("customerId", customerId)));

        List<AccountResponse> accounts = accountRepository
                .findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)
                .stream()
                .map(AccountResponse::from)
                .toList();

        return CustomerResponse.builder()
            .customerId(customer.getCustomerId())
            .name(customer.getName())
            .address(customer.getAddress())
            .type(customer.getType())
            .accounts(accounts)
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .deletedAt(customer.getDeletedAt())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customerList = customerRepository.findAll();

        return customerList.stream()
                .map(customer -> {
                    List<AccountResponse> accounts = accountRepository
                            .findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(
                                    customer.getCustomerId(), AccountStatus.ACTIVE)
                            .stream()
                            .map(AccountResponse::from)
                            .toList();

                        return CustomerResponse.builder()
                            .customerId(customer.getCustomerId())
                            .name(customer.getName())
                            .address(customer.getAddress())
                            .type(customer.getType())
                            .accounts(accounts)
                            .createdAt(customer.getCreatedAt())
                            .updatedAt(customer.getUpdatedAt())
                            .deletedAt(customer.getDeletedAt())
                            .build();
                })
                .toList();
    }
    
    @Transactional
    public void deleteCustomer(Long customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new UnauthorisedException("UNAUTHORIZED", "Unauthorized");
        }

        UUID userId = principal.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorisedException("UNAUTHORIZED", "Unauthorized"));

        System.out.println("User Roles"+ user.getRoles());
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.name().equalsIgnoreCase("ADMIN") || role.name().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new UnauthorisedException("UNAUTHORIZED", "User is not an admin.");
        }

        // 3️⃣ Load customer and check active accounts
        Customer customer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", Map.of("customerId", customerId)));

        if (accountRepository.existsByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)) {
            throw new ConflictException("CUSTOMER_HAS_ACTIVE_ACCOUNTS",
                    "Customer has active accounts and cannot be deleted", null);
        }

        // 4️⃣ Soft delete
        customer.setDeletedAt(Instant.now());
        customerRepository.save(customer);
    }
}
