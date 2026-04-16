package com.group1.banking.exception;

public class ConflictException extends ApiException {
    private static final long serialVersionUID = 1L;

	public ConflictException(String code, String message, String field) {
        super(409, code, message, field);
    }
}
