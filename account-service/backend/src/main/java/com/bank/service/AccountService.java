package com.bank.service;

import com.bank.dto.AccountResponse;
import com.bank.dto.CreateAccountRequest;
import com.bank.dto.UpdateAccountRequest;
import com.bank.exception.ConflictException;
import com.bank.exception.NotFoundException;
import com.bank.exception.UnprocessableException;
import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import com.bank.model.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuthorizationService authorizationService;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            AuthorizationService authorizationService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public AccountResponse createAccount(Long customerId, CreateAccountRequest request, AuthenticatedUser user) {
        authorizationService.assertCanAccessCustomer(user, customerId);
        Customer customer = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", null));
        validateCreateRequest(request);

        Account account = new Account();
        account.setAccountId(nextAccountId());
        account.setCustomer(customer);
        account.setAccountType(request.accountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(scaleMoney(request.balance()));
        account.setInterestRate(request.interestRate());

        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId, AuthenticatedUser user) {
        Account account = accountRepository.findByAccountIdAndDeletedAtIsNull(accountId)
                .filter(existing -> existing.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "Account not found", null));
        authorizationService.assertCanAccessAccount(user, account);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listCustomerAccounts(Long customerId, AuthenticatedUser user) {
        authorizationService.assertCanAccessCustomer(user, customerId);
        if (!customerRepository.existsByCustomerIdAndDeletedAtIsNull(customerId)) {
            throw new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", null);
        }
        return accountRepository.findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request, AuthenticatedUser user) {
        Account account = loadActiveAccount(accountId);
        authorizationService.assertCanAccessAccount(user, account);
        validateUpdateRequest(account, request);

        if (request.interestRate() != null) {
            account.setInterestRate(scaleInterestRate(request.interestRate()));
        }
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long accountId, AuthenticatedUser user) {
        Account account = loadActiveAccount(accountId);
        if (!user.isAdmin()) {
            throw new com.bank.exception.UnauthorizedException("UNAUTHORIZED", "Unauthorized");
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
                .orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "Account not found", null));
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
}
