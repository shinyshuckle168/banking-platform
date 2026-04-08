package com.bank.dto;

public record MonetaryOperationResponse(String message, AccountResponse account, TransactionResponse transaction) {}
