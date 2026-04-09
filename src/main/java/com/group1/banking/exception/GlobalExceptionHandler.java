package com.group1.banking.exception;

import com.group1.banking.dto.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().get(0).getField();
        logger.warn("Validation failed for field={}", field);

        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse("VALIDATION_FAILED", "Validation failed", field));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        logger.warn("Conflict occurred. code={}, message={}", ex.getCode(), ex.getMessage());

        return ResponseEntity.status(409)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        logger.warn("Resource not found. code={}, message={}", ex.getCode(), ex.getMessage());

        return ResponseEntity.status(404)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        return ResponseEntity.status(500)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "Something went wrong.", null));
    }
    
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(403)
                .body(new ErrorResponse("FORBIDDEN", "Access denied.", null));
    }
}