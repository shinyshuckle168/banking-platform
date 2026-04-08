package com.bank.dto;

public record ErrorResponse(String code, String message, String field) {
}
