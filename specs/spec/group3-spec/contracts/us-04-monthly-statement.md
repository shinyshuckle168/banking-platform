# API Contracts — US-04: Get Monthly Statement

**Path**: `GET /accounts/{accountId}/statements/{period}`  
**Authentication**: JWT Bearer Token  
**Required Permission**: `STATEMENT:READ` (re-validated at delivery)

---

## GET /accounts/{accountId}/statements/{period}

### Request

```
GET /accounts/{accountId}/statements/2026-03?version=1
Authorization: Bearer {jwt}
```

| Parameter | In | Type | Required | Constraints |
|---|---|---|---|---|
| `accountId` | path | long | Yes | > 0 |
| `period` | path | string | Yes | YYYY-MM format; must be a closed period |
| `version` | query | integer | No | Positive integer; defaults to latest if omitted |

### Response 200

```json
{
  "accountId": 101,
  "period": "2026-03",
  "openingBalance": "1500.00",
  "closingBalance": "2300.00",
  "totalMoneyIn": "2000.00",
  "totalMoneyOut": "1200.00",
  "versionNumber": 1,
  "correctionSummary": null,
  "generatedAt": "2026-04-01T02:00:00Z",
  "transactions": [
    {
      "transactionId": 4999,
      "amount": "2000.00",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "timestamp": "2026-03-01T08:30:00Z",
      "description": "Salary — Acme Corp",
      "idempotencyKey": null
    },
    {
      "transactionId": 5001,
      "amount": "250.00",
      "type": "WITHDRAW",
      "status": "FAILED",
      "timestamp": "2026-03-15T11:00:00Z",
      "description": "Direct debit — failed",
      "idempotencyKey": null
    }
  ]
}
```

**Note**: `transactions` includes all SUCCESS and FAILED records for the period. Never truncated.  
**Note**: `correctionSummary` is `null` on version 1. Present on version ≥ 2.

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Non-numeric or ≤ 0 |
| 400 | `ERR_INVALID_PERIOD` | `period` | Not YYYY-MM format |
| 400 | `ERR_INVALID_VERSION` | `version` | Not a positive integer |
| 401 | `ERR_UNAUTHORIZED` | — | Lacks `STATEMENT:READ` or ownership |
| 404 | `ERR_ACC_NOT_FOUND` | `accountId` | Account not found |
| 404 | `ERR_STATEMENT_NOT_FOUND` | `period` | No statement for period |
| 404 | `ERR_VERSION_NOT_FOUND` | `version` | Requested version does not exist |
| 409 | `ERR_PERIOD_NOT_CLOSED` | `period` | Period has not reached cut-off |
| 410 | `ERR_RETENTION_EXPIRED` | `period` | Beyond self-service retention window |
