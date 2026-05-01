package com.group1.banking.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
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
import com.group1.banking.entity.GicStatus;
import com.group1.banking.exception.BadRequestException;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.GicRepository;
import com.group1.banking.repository.UserRepository;
import com.group1.banking.security.AuthenticatedUser;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.AuthService;

@Service
public class AccountService {

    // region Dependencies

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuthService authorizationService;
    private final UserRepository userRepository;
    private final GicRepository gicRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            AuthService authorizationService,
            UserRepository userRepository,
            GicRepository gicRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.gicRepository = gicRepository;
    }

    @Transactional
    public AccountResponse createAccount(Long customerId, CreateAccountRequest request) {
        User user = getAuthenticatedUser();
        Customer customer = loadCustomer(customerId);
        checkAuthorization(user, customerId);
        validateCreateRequest(request, customer);

        Account account = buildAccount(request, customer);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);
        checkAuthorization(user, account.getCustomer().getCustomerId());
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listCustomerAccounts(Long customerId) {
        User user = getAuthenticatedUser();
        if (!customerRepository.existsByCustomerIdAndDeletedAtIsNull(customerId)) {
            throw new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", Map.of("customerId", customerId));
        }
        checkAuthorization(user, customerId);
        return accountRepository.findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(customerId, AccountStatus.ACTIVE)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }


    @Transactional
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request) {
        User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);
        checkAuthorization(user, account.getCustomer().getCustomerId());
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
        checkAuthorization(user, account.getCustomer().getCustomerId());
        if (account.getBalance().compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY)) != 0) {
            throw new ConflictException("ACCOUNT_HAS_NON_ZERO_BALANCE", "Account has a non-zero balance", null);
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
    }

    private void validateCreateRequest(CreateAccountRequest request, Customer customer) {
        AccountType type = request.accountType();
        BigDecimal interestRate = request.interestRate();
        if (type == AccountType.SAVINGS) {
            if (interestRate == null) {
                throw new UnprocessableException("MISSING_INTEREST_RATE", "interestRate is required for SAVINGS accounts", "interestRate");
            }
            if (interestRate.scale() > 4 || interestRate.compareTo(BigDecimal.ZERO) < 0) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate must be non-negative with at most 4 decimal places", "interestRate");
            }
        } else if (type == AccountType.CHECKING) {
            if (interestRate != null) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate is not allowed for CHECKING accounts", "interestRate");
            }
        } else if (type == AccountType.TFSA) {
            validateTfsaEligibility(customer, interestRate);
        } else if (type == AccountType.RRSP) {
            validateRrspEligibility(customer, interestRate);
        }
    }

    private void validateTfsaEligibility(Customer customer, BigDecimal interestRate) {
        if (interestRate == null) {
            throw new UnprocessableException("MISSING_INTEREST_RATE", "interestRate is required for TFSA accounts", "interestRate");
        }
        if (interestRate.scale() > 4 || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate must be non-negative with at most 4 decimal places", "interestRate");
        }
        // Age check (must be 18+)
        LocalDate dob = getCustomerDob(customer);
        if (dob == null || Period.between(dob, LocalDate.now()).getYears() < 18) {
            throw new UnprocessableException("AGE_REQUIREMENT", "Customer must be at least 18 years old for TFSA", "dateOfBirth");
        }
        // KYC check
        if (!isKycVerified(customer)) {
            throw new UnprocessableException("KYC_REQUIRED", "Customer must be KYC verified for TFSA", "kyc");
        }
        // Only one TFSA per customer
        boolean hasTfsa = customer.getAccounts().stream()
                .anyMatch(a -> a.getAccountType() == AccountType.TFSA && a.getDeletedAt() == null);
        if (hasTfsa) {
            throw new ConflictException("TFSA_EXISTS", "Customer already has an active TFSA account", null);
        }
        // Contribution room check (placeholder)
        // throw new UnprocessableException("CONTRIBUTION_ROOM", "Contribution room exceeded", "contributionRoom");
    }

    private void validateRrspEligibility(Customer customer, BigDecimal interestRate) {
        if (interestRate != null) {
            throw new UnprocessableException("INVALID_INTEREST_RATE",
                    "interestRate is not applicable for RRSP accounts — it is derived from the GIC term",
                    "interestRate");
        }
        if (!customer.isKycVerified()) {
            throw new UnprocessableException("KYC_REQUIRED",
                    "Customer must be KYC verified to open an RRSP account", "kyc");
        }
        boolean hasRrsp = accountRepository
                .existsByCustomerCustomerIdAndAccountTypeAndDeletedAtIsNull(
                        customer.getCustomerId(), AccountType.RRSP);
        if (hasRrsp) {
            throw new ConflictException("RRSP_ALREADY_EXISTS",
                    "Customer already has an active RRSP account", null);
        }
    }

    @Transactional
    public Map<String, Object> closeRrspAccount(Long accountId) {
        User user = getAuthenticatedUser();
        Account account = loadActiveAccount(accountId);
        checkAuthorization(user, account.getCustomer().getCustomerId());

        if (account.getAccountType() != AccountType.RRSP) {
            throw new BadRequestException("INVALID_ACCOUNT_TYPE",
                    "Only RRSP accounts can be closed via this endpoint",
                    Map.of("accountType", account.getAccountType()));
        }
        if (gicRepository.existsByAccount_AccountIdAndDeletedAtIsNullAndStatus(accountId, GicStatus.ACTIVE)) {
            throw new BadRequestException("ACTIVE_GIC_EXISTS",
                    "Cannot close RRSP account while an active GIC exists", null);
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY)) != 0) {
            throw new BadRequestException("NON_ZERO_BALANCE",
                    "Cannot close RRSP account with a non-zero balance", null);
        }

        Instant now = Instant.now();
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(now);
        account.setDeletedAt(now);
        accountRepository.save(account);

        return Map.of(
                "message", "RRSP account closed successfully",
                "accountId", accountId,
                "closedAt", now.toString());
    }

    private void validateUpdateRequest(Account account, UpdateAccountRequest request) {
        if (request.isEmpty()) {
            throw new UnprocessableException("EMPTY_UPDATE_REQUEST", "At least one updatable field is required", null);
        }
        AccountType type = account.getAccountType();
        BigDecimal interestRate = request.interestRate();
        if (type == AccountType.SAVINGS) {
            if (interestRate == null) {
                throw new UnprocessableException("MISSING_INTEREST_RATE", "interestRate is required for SAVINGS updates", "interestRate");
            }
            if (interestRate.compareTo(BigDecimal.ZERO) < 0 || interestRate.scale() > 4) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate must be non-negative with at most 4 decimal places", "interestRate");
            }
        } else if (type == AccountType.CHECKING) {
            if (interestRate != null) {
                throw new UnprocessableException("INVALID_INTEREST_RATE", "interestRate is not allowed for CHECKING accounts", "interestRate");
            }
        }
    }

    private Account loadActiveAccount(Long accountId) {
        return accountRepository.findByAccountIdAndDeletedAtIsNull(accountId)
                .filter(existing -> existing.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "Account not found", Map.of("accountId", accountId)));
    }

    private Customer loadCustomer(Long customerId) {
        return customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found", Map.of("customerId", customerId)));
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

    private void checkAuthorization(User user, Long customerId) {
        if (!isAdmin(user) && !user.getCustomerId().equals(customerId)) {
            throw new UnauthorisedException("UNAUTHORIZED", "You can only manage your own accounts.");
        }
    }

    private Account buildAccount(CreateAccountRequest request, Customer customer) {
        Account account = new Account();
        long accountId = nextAccountId();
        account.setAccountId(accountId);
        account.setAccountNumber(generateAccountNumber(accountId));
        account.setCustomer(customer);
        account.setAccountType(request.accountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(scaleMoney(request.balance()));
        account.setInterestRate(request.interestRate());
        return account;
    }

    private LocalDate getCustomerDob(Customer customer) {
        try {
            java.lang.reflect.Method m = customer.getClass().getMethod("getDateOfBirth");
            return (LocalDate) m.invoke(customer);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isKycVerified(Customer customer) {
        try {
            java.lang.reflect.Method m = customer.getClass().getMethod("isKycVerified");
            return (Boolean) m.invoke(customer);
        } catch (Exception e) {
            return false;
        }
    }

}
