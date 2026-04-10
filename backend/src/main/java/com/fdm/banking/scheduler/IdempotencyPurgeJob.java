package com.fdm.banking.scheduler;

import com.fdm.banking.repository.ExportCacheRepository;
import com.fdm.banking.repository.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job to purge stale idempotency records and export cache entries. (T113)
 */
@Component
public class IdempotencyPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyPurgeJob.class);

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ExportCacheRepository exportCacheRepository;

    @Value("${banking.idempotency.purge-after-hours:72}")
    private int purgeAfterHours;

    public IdempotencyPurgeJob(IdempotencyRecordRepository idempotencyRecordRepository,
                                ExportCacheRepository exportCacheRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.exportCacheRepository = exportCacheRepository;
    }

    /**
     * Runs daily at 03:00 UTC.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    @Transactional
    public void purge() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(purgeAfterHours);
        idempotencyRecordRepository.deleteByCreatedAtBefore(cutoff);
        exportCacheRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Idempotency purge completed: purged records older than {}h", purgeAfterHours);
    }
}
