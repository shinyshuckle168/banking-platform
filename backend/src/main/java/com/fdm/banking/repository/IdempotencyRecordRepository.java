package com.fdm.banking.repository;

import com.fdm.banking.entity.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
    Optional<IdempotencyRecordEntity> findByIdempotencyKey(String key);
}
