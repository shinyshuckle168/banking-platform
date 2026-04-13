package com.fdm.banking.entity;

/**
 * Group 2 enum — GAP-1 resolution: PENDING status added by Group 3 coordination.
 * Three values: PENDING (initiated, not settled), SUCCESS (completed), FAILED (failed).
 * Flyway/Liquibase migration required if column uses database-level ENUM type.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
