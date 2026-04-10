package com.group1.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.IdempotencyRecord;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {}
