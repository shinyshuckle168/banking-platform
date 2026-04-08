package com.bank.service;

import com.bank.exception.ConflictException;
import com.bank.exception.NotFoundException;
import com.bank.exception.UnauthorizedException;
import com.bank.model.AccountStatus;
import com.bank.model.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.security.AuthenticatedUser;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void deleteCustomer(Long customerId, AuthenticatedUser user) {
        if (user == null || !user.isAdmin()) {
            throw new UnauthorizedException("UNAUTHORIZED", "Unauthorized");
        }

        Customer customer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", null));

        if (accountRepository.existsByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)) {
            throw new ConflictException("CUSTOMER_HAS_ACTIVE_ACCOUNTS", "Customer has active accounts and cannot be deleted", null);
        }

        customer.setDeletedAt(Instant.now());
        customerRepository.save(customer);
    }
}
