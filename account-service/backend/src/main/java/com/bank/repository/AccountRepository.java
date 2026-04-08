package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountIdAndDeletedAtIsNull(Long accountId);

    List<Account> findAllByCustomerCustomerIdAndDeletedAtIsNullAndStatus(Long customerId, AccountStatus status);

    boolean existsByCustomerCustomerIdAndDeletedAtIsNullAndStatus(Long customerId, AccountStatus status);
}
