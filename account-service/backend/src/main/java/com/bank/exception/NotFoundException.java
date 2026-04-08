package com.bank.exception;

public class NotFoundException extends BankingException {

    public NotFoundException(String code, String message, String field) {
        super(code, message, field);
    }
}
