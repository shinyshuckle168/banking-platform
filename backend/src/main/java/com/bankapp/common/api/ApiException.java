package com.bankapp.common.api;

import org.springframework.http.HttpStatusCode;

public class ApiException extends RuntimeException {

    private final HttpStatusCode status;
    private final String code;
    private final String field;

    public ApiException(HttpStatusCode status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatusCode status, String code, String message, String field) {
        super(message);
        this.status = status;
        this.code = code;
        this.field = field;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
