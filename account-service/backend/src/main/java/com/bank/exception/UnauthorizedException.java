package com.bank.exception;

public class UnauthorizedException extends BankingException {

    public UnauthorizedException(String code, String message) {
        super(code, message, null);
    }
}
