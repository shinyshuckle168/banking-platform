package com.fdm.banking.repository;

import com.fdm.banking.entity.TransactionEntity;
import com.fdm.banking.entity.TransactionStatus;
import com.fdm.banking.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Group 3-owned query repository for transactions. (T025)
 * Does not redefine the TransactionEntity — extends Group 2 repository patterns.
 */
@Repository
public interface TransactionQueryRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
            Long accountId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM TransactionEntity t WHERE t.account.accountId = :accountId " +
           "AND t.type IN :types AND t.status = :status " +
           "AND t.timestamp >= :start AND t.timestamp < :end " +
           "ORDER BY t.timestamp ASC")
    List<TransactionEntity> findEligibleForInsights(
            @Param("accountId") Long accountId,
            @Param("types") List<TransactionType> types,
            @Param("status") TransactionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
