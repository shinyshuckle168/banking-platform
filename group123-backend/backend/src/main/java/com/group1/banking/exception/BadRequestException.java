package com.group1.banking.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String code, String message, String field) {
        super(400, code, message, field);
    }
}
