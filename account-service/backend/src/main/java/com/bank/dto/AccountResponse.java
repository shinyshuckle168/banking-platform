package com.bank.dto;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long accountId,
        Long customerId,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        BigDecimal interestRate,
        Instant createdAt,
        Instant updatedAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getCustomer().getCustomerId(),
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                account.getInterestRate(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}
