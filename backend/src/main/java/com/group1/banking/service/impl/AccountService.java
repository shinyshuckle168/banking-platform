package com.group1.banking.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.banking.dto.customer.AccountResponse;
import com.group1.banking.dto.customer.CreateAccountRequest;
import com.group1.banking.dto.customer.UpdateAccountRequest;
import com.group1.banking.entity.Account;
import com.group1.banking.entity.AccountStatus;
import com.group1.banking.entity.AccountType;
import com.group1.banking.entity.Customer;
import com.group1.banking.entity.User;
import com.group1.banking.exception.ConflictException;
import com.group1.banking.exception.NotFoundException;
import com.group1.banking.exception.UnauthorisedException;
import com.group1.banking.exception.UnprocessableException;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.UserRepository;
import com.group1.banking.security.AuthenticatedUser;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.AuthService;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuthService authorizationService;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            AuthService authorizationService, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountResponse createAccount(Long customerId, CreateAccountRequest request) {
        // 1️⃣ Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new UnauthorisedException("UNAUTHORIZED", "Authenticated user not found.");
        }
        UUID userId = principal.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorisedException("UNAUTHORIZED", "Authenticated user not found."));

        // 2️⃣ Load customer
        Customer customer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found"));

        // 3️⃣ Authorization check
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.name().equalsIgnoreCase("ADMIN") 
                               || role.name().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !user.getCustomerId().equals(customerId)) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only manage your own accounts.");
        }

        // 4️⃣ Validate request
        validateCreateRequest(request);

        // 5️⃣ Create account
        Account account = new Account();
        long accountId = nextAccountId();
        account.setAccountId(accountId);
        account.setAccountNumber(generateAccountNumber(accountId));
        account.setCustomer(customer);
        account.setAccountType(request.accountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(scaleMoney(request.balance()));
        account.setInterestRate(request.interestRate());

        // 6️⃣ Save and return response
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
    	User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);

        if (!isAdmin(user) && !user.getCustomerId().equals(account.getCustomer().getCustomerId())) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only access your own accounts.");
        }

        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listCustomerAccounts(Long customerId) {
    	User user = getAuthenticatedUser();

        if (!customerRepository.existsByCustomerIdAndDeletedAtIsNull(customerId)) {
            throw new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found");
        }

        if (!isAdmin(user) && !user.getCustomerId().equals(customerId)) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only access your own accounts.");
        }

        return accountRepository.findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }


    @Transactional
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request) {
    	User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);

        if (!isAdmin(user) && !user.getCustomerId().equals(account.getCustomer().getCustomerId())) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only update your own accounts.");
        }

        validateUpdateRequest(account, request);

        if (request.interestRate() != null) {
            account.setInterestRate(scaleInterestRate(request.interestRate()));
        }

        return AccountResponse.from(accountRepository.save(account));
    }


    @Transactional
    public void deleteAccount(Long accountId) {
    	User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);

        if (!isAdmin(user) && !user.getCustomerId().equals(account.getCustomer().getCustomerId())) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only delete your own accounts.");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY)) != 0) {
            throw new ConflictException("ACCOUNT_HAS_NON_ZERO_BALANCE", "Account has a non-zero balance", null);
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
    }

    private void validateCreateRequest(CreateAccountRequest request) {
        if (request.accountType() == AccountType.SAVINGS) {
            if (request.interestRate() == null) {
                throw new UnprocessableException("MISSING_INTEREST_RATE", "interestRate is required for SAVINGS accounts", "interestRate");
            }
            if (request.interestRate().scale() > 4 || request.interestRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate must be non-negative with at most 4 decimal places", "interestRate");
            }
        }

        if (request.accountType() == AccountType.CHECKING) {
            if (request.interestRate() != null) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate is not allowed for CHECKING accounts", "interestRate");
            }
        }
    }

    private void validateUpdateRequest(Account account, UpdateAccountRequest request) {
        if (request.isEmpty()) {
            throw new UnprocessableException("EMPTY_UPDATE_REQUEST", "At least one updatable field is required", null);
        }

        if (account.getAccountType() == AccountType.SAVINGS) {
            if (request.interestRate() == null) {
                throw new UnprocessableException("MISSING_INTEREST_RATE", "interestRate is required for SAVINGS updates", "interestRate");
            }
            if (request.interestRate().compareTo(BigDecimal.ZERO) < 0 || request.interestRate().scale() > 4) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate must be non-negative with at most 4 decimal places", "interestRate");
            }
        }

        if (account.getAccountType() == AccountType.CHECKING) {
            if (request.interestRate() != null) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate is not allowed for CHECKING accounts", "interestRate");
            }
        }
    }

    private Account loadActiveAccount(Long accountId) {
        return accountRepository.findByAccountIdAndDeletedAtIsNull(accountId)
                .filter(existing -> existing.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "Account not found"));
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value.scale() > 2) {
            throw new UnprocessableException("INVALID_BALANCE", "balance must have at most two decimal places", "balance");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal scaleInterestRate(BigDecimal value) {
        return value.setScale(Math.min(value.scale(), 4), RoundingMode.UNNECESSARY);
    }

    private long nextAccountId() {
        return accountRepository.count() + 1000;
    }

    private String generateAccountNumber(long accountId) {
        return String.format("ACC%010d", accountId);
    }
    
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new UnauthorisedException("UNAUTHORIZED", "Authenticated user not found.");
        }
        UUID userId = principal.getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorisedException("UNAUTHORIZED", "Authenticated user not found."));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.name().equalsIgnoreCase("ADMIN") || r.name().equalsIgnoreCase("ROLE_ADMIN"));
    }

}
