package com.fdm.banking.repository;

import com.fdm.banking.entity.NotificationDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDecisionRepository extends JpaRepository<NotificationDecisionEntity, String> {
    boolean existsByEventId(String eventId);
}
