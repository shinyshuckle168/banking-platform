package com.group1.banking.dto.customer;

import com.group1.banking.enums.CustomerType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class CustomerResponse {
    private Long customerId;
    private String name;
    private String address;
    private CustomerType type;
    private List<AccountResponse> accounts;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}