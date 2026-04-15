package com.group1.banking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.Account;
import com.group1.banking.entity.AccountStatus;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountIdAndDeletedAtIsNull(Long accountId);

    List<Account> findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(Long customerId, AccountStatus status);

    boolean existsByCustomerCustomerIdAndDeletedAtIsNullAndStatus(Long customerId, AccountStatus status);
    Optional<Account> findByAccountNumber(String accountNumber);
}
