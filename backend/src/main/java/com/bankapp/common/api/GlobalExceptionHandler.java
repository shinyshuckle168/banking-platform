package com.bankapp.common.api;

import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity.status(Objects.requireNonNull(exception.getStatus(), "API exception status is required."))
                .body(ErrorResponse.of(exception.getCode(), exception.getMessage(), exception.getField()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : null;
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("MISSING_REQUIRED_FIELD", "Request validation failed.", field));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("INVALID_INPUT", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_IDENTIFIER", "Request parameter type is invalid.", exception.getName()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("INVALID_CREDENTIALS", "The supplied credentials are invalid."));
    }

    @ExceptionHandler(DisabledException.class)
    ResponseEntity<ErrorResponse> handleDisabled(DisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCOUNT_INACTIVE", "The account is inactive."));
    }

    @ExceptionHandler({AccessDeniedException.class, JwtException.class})
    ResponseEntity<ErrorResponse> handleUnauthorized(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("UNAUTHORISED", "Access is not authorised for this request."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        String code = Optional.ofNullable(exception.getReason()).orElse(exception.getStatusCode().toString());
        return ResponseEntity.status(exception.getStatusCode())
                .body(ErrorResponse.of(code, exception.getReason() == null ? "Request failed." : exception.getReason()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of("INVALID_INPUT", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred."));
    }
}
