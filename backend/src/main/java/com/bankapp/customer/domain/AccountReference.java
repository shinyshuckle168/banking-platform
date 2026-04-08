package com.bankapp.customer.domain;

import java.util.UUID;

public record AccountReference(Long customerId, UUID userId) {
}
