package com.fdm.banking.repository;

import com.fdm.banking.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
    void deleteByCreatedAtBefore(Instant cutoff);
    Optional<IdempotencyRecord> findByStorageKey(String storageKey);
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
