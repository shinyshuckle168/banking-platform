package com.bank.dto;

import com.bank.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull AccountType accountType,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 17, fraction = 2) BigDecimal balance,
        @Digits(integer = 8, fraction = 4) BigDecimal interestRate) {
}
