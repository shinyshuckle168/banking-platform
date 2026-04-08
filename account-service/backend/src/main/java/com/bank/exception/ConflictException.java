package com.bank.exception;

public class ConflictException extends BankingException {

    public ConflictException(String code, String message, String field) {
        super(code, message, field);
    }
}
