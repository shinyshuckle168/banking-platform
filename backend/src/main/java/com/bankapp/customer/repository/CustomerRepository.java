package com.bankapp.customer.repository;

import com.bankapp.customer.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerIdAndOwnerUserId(Long customerId, UUID ownerUserId);
}
