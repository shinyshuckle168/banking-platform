package com.fdm.banking.mapper;

import com.fdm.banking.dto.response.StandingOrderResponse;
import com.fdm.banking.entity.StandingOrderEntity;
import org.springframework.stereotype.Component;

/**
 * Standing order mapper. (T049)
 */
@Component
public class StandingOrderMapper {

    public StandingOrderResponse toResponse(StandingOrderEntity entity) {
        StandingOrderResponse r = new StandingOrderResponse();
        r.setStandingOrderId(entity.getStandingOrderId());
        r.setSourceAccountId(entity.getSourceAccountId());
        r.setPayeeAccount(entity.getPayeeAccount());
        r.setPayeeName(entity.getPayeeName());
        r.setAmount(entity.getAmount());
        r.setFrequency(entity.getFrequency().name());
        r.setStartDate(entity.getStartDate());
        r.setEndDate(entity.getEndDate());
        r.setReference(entity.getReference());
        r.setStatus(entity.getStatus().name());
        r.setNextRunDate(entity.getNextRunDate());
        return r;
    }
}
