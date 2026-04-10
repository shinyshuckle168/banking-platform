package com.group1.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {}
