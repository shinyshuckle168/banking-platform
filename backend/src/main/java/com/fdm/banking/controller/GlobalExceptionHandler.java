package com.fdm.banking.controller;

import com.fdm.banking.dto.response.ErrorResponse;
import com.fdm.banking.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler mapping custom exceptions to HTTP status codes. (T008)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorResponse> handleOwnership(OwnershipException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("ERR_OWNERSHIP", ex.getMessage()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDenied(PermissionDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("ERR_UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(LockException.class)
    public ResponseEntity<ErrorResponse> handleLock(LockException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessStateException.class)
    public ResponseEntity<ErrorResponse> handleBusinessState(BusinessStateException ex) {
        ErrorResponse body = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getField());
        // ERR_ACC_003 is a conflict (409), date range exceeded is a bad request (400)
        if ("ERR_ACC_003".equals(ex.getCode()) || ex.getCode().startsWith("ERR_SO_DUPLICATE")
                || ex.getCode().startsWith("ERR_DUPLICATE") || ex.getCode().startsWith("ERR_PERIOD_NOT_CLOSED")
                || ex.getCode().startsWith("ERR_FUTURE_MONTH")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RetentionWindowException.class)
    public ResponseEntity<ErrorResponse> handleRetention(RetentionWindowException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(SemanticValidationException.class)
    public ResponseEntity<ErrorResponse> handleSemanticValidation(SemanticValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getField()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        if (fieldError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("ERR_VALIDATION", fieldError.getDefaultMessage(), fieldError.getField()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("ERR_VALIDATION", "Validation failed"));
    }
}
