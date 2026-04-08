# API Contracts — US-01: Get Transaction History

**Base Path**: `/accounts/{accountId}/transactions`  
**Authentication**: JWT Bearer Token  
**Required Permission**: `TRANSACTION:READ`

---

## GET /accounts/{accountId}/transactions

### Request

```
GET /accounts/{accountId}/transactions?startDate={startDate}&endDate={endDate}
Authorization: Bearer {jwt}
```

| Parameter | In | Type | Required | Constraints |
|---|---|---|---|---|
| `accountId` | path | long | Yes | > 0 |
| `startDate` | query | ISO-8601 UTC date | No | Defaults to 28 days ago |
| `endDate` | query | ISO-8601 UTC date | No | Defaults to now; future dates overridden silently |

**Date range constraint**: `endDate - startDate` ≤ 366 days. Violation → 400.

### Response 200

```json
{
  "accountId": 101,
  "startDate": "2026-03-11T00:00:00Z",
  "endDate": "2026-04-08T14:00:00Z",
  "transactionCount": 3,
  "transactions": [
    {
      "transactionId": 5001,
      "amount": "250.00",
      "type": "WITHDRAW",
      "status": "PENDING",
      "timestamp": "2026-04-08T09:00:00Z",
      "description": "Direct debit — Utilities",
      "idempotencyKey": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    },
    {
      "transactionId": 4999,
      "amount": "1200.00",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "timestamp": "2026-04-01T08:30:00Z",
      "description": "Salary — Acme Corp",
      "idempotencyKey": null
    }
  ]
}
```

**Ordering**: PENDING transactions first, then POSTED in ascending `timestamp` order.

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Non-numeric or ≤ 0 |
| 400 | `ERR_INVALID_DATE_FORMAT` | `startDate` or `endDate` | Not valid ISO-8601 |
| 400 | `ERR_DATE_RANGE_EXCEEDED` | `endDate` | Range > 366 days |
| 401 | `ERR_UNAUTHORIZED` | — | Missing/invalid JWT or lacks `TRANSACTION:READ` |
| 401 | `ERR_OWNERSHIP` | — | CUSTOMER caller does not own account |
| 404 | `ERR_ACC_NOT_FOUND` | — | Account does not exist |
| 409 | `ERR_ACC_003` | — | CLOSED account, 90-day window expired |

---

## GET /accounts/{accountId}/transactions/export

### Request

```
GET /accounts/{accountId}/transactions/export?startDate={startDate}&endDate={endDate}
Authorization: Bearer {jwt}
```

| Parameter | In | Type | Required | Constraints |
|---|---|---|---|---|
| `accountId` | path | long | Yes | > 0 |
| `startDate` | query | ISO-8601 UTC date | Yes | |
| `endDate` | query | ISO-8601 UTC date | Yes | Must not exceed 366-day range |

### Response 200

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="statement-{accountId}-{startDate}-{endDate}.pdf"

[Binary PDF content]
```

**Idempotency**: Identical (accountId, effective startDate, effective endDate) returns the same cached PDF bytes.

### Error Responses

Same codes as GET /transactions, except 409 (CLOSED window) does not apply to export.
