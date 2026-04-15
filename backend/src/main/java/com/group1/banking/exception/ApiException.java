package com.group1.banking.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int status;
    private final String code;
    private final String field;

    public ApiException(int status, String code, String message, String field) {
        super(message);
        this.status = status;
        this.code = code;
        this.field = field;
    }
}
