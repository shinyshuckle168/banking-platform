package com.bank.dto;

import org.springframework.http.HttpStatus;

public record OperationResult(HttpStatus status, Object body) {}
