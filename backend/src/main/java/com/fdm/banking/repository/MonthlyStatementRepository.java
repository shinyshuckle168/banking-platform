package com.fdm.banking.repository;

import com.fdm.banking.entity.MonthlyStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatementEntity, Long> {
    Optional<MonthlyStatementEntity> findTopByAccountIdAndPeriodOrderByVersionNumberDesc(Long accountId, String period);
    Optional<MonthlyStatementEntity> findByAccountIdAndPeriodAndVersionNumber(Long accountId, String period, Integer versionNumber);
}
