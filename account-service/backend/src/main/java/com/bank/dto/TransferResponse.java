package com.bank.dto;

public record TransferResponse(
        String message,
        AccountResponse fromAccount,
        AccountResponse toAccount,
        TransactionResponse debitTransaction,
        TransactionResponse creditTransaction) {}
