package com.bank.dto;

import java.math.BigDecimal;

public record MonetaryRequest(BigDecimal amount, String description) {}
