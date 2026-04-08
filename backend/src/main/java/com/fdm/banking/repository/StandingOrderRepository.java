package com.fdm.banking.repository;

import com.fdm.banking.entity.StandingOrderEntity;
import com.fdm.banking.entity.StandingOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import com.fdm.banking.entity.Frequency;
import java.util.Optional;

@Repository
public interface StandingOrderRepository extends JpaRepository<StandingOrderEntity, String> {

    List<StandingOrderEntity> findBySourceAccountId(Long sourceAccountId);

    List<StandingOrderEntity> findByStatusAndNextRunDateBefore(StandingOrderStatus status, LocalDateTime cutoff);

    Optional<StandingOrderEntity> findBySourceAccountIdAndPayeeAccountAndAmountAndFrequencyAndStatus(
            Long sourceAccountId, String payeeAccount, BigDecimal amount,
            Frequency frequency, StandingOrderStatus status);

    List<StandingOrderEntity> findByStatus(StandingOrderStatus status);
}
