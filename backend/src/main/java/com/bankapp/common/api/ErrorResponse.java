package com.bankapp.common.api;

public record ErrorResponse(String code, String message, String field) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, String field) {
        return new ErrorResponse(code, message, field);
    }
}
