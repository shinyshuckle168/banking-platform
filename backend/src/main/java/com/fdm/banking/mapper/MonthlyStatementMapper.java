package com.fdm.banking.mapper;

import com.fdm.banking.dto.response.MonthlyStatementResponse;
import com.fdm.banking.dto.response.TransactionItemResponse;
import com.fdm.banking.entity.MonthlyStatementEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Monthly statement mapper. (T081)
 * Deserializes transactionsJson to TransactionItemResponse list.
 */
@Component
public class MonthlyStatementMapper {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    public MonthlyStatementResponse toResponse(MonthlyStatementEntity entity) {
        MonthlyStatementResponse r = new MonthlyStatementResponse();
        r.setAccountId(entity.getAccountId());
        r.setPeriod(entity.getPeriod());
        r.setOpeningBalance(entity.getOpeningBalance());
        r.setClosingBalance(entity.getClosingBalance());
        r.setTotalMoneyIn(entity.getTotalMoneyIn());
        r.setTotalMoneyOut(entity.getTotalMoneyOut());
        r.setVersionNumber(entity.getVersionNumber());
        r.setCorrectionSummary(entity.getCorrectionSummary());
        r.setGeneratedAt(entity.getGeneratedAt());
        r.setTransactions(deserializeTransactions(entity.getTransactionsJson()));
        return r;
    }

    private List<TransactionItemResponse> deserializeTransactions(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<TransactionItemResponse>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
