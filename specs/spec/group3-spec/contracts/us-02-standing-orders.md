# API Contracts — US-02: Standing Order Management

**Base Path**: `/accounts/{accountId}/standing-orders` and `/standing-orders/{standingOrderId}`  
**Authentication**: JWT Bearer Token

---

## POST /accounts/{accountId}/standing-orders

**Required Permission**: `SO:CREATE`

### Request

```json
{
  "payeeAccount": "GB29NWBK60161331926819",
  "payeeName": "John Smith",
  "amount": "500.00",
  "frequency": "MONTHLY",
  "startDate": "2026-05-01T00:00:00Z",
  "endDate": "2027-05-01T00:00:00Z",
  "reference": "RENT2026"
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `payeeAccount` | string | Yes | Max 34 chars; Modulo 97 checksum valid |
| `payeeName` | string | Yes | 1–70 chars |
| `amount` | BigDecimal | Yes | > 0, exactly 2 decimal places, ≤ daily transfer limit |
| `frequency` | string | Yes | DAILY, WEEKLY, MONTHLY, QUARTERLY |
| `startDate` | ISO-8601 UTC | Yes | ≥ 24 hours from request time |
| `endDate` | ISO-8601 UTC | No | Must be after `startDate` if provided |
| `reference` | string | Yes | 1–18 alphanumeric chars |

### Response 201

```json
{
  "standingOrderId": "550e8400-e29b-41d4-a716-446655440000",
  "sourceAccountId": 101,
  "payeeAccount": "GB29NWBK60161331926819",
  "payeeName": "John Smith",
  "amount": "500.00",
  "frequency": "MONTHLY",
  "startDate": "2026-05-01T00:00:00Z",
  "endDate": "2027-05-01T00:00:00Z",
  "reference": "RENT2026",
  "status": "ACTIVE",
  "nextRunDate": "2026-05-01T00:00:00Z",
  "message": "Standing order created successfully."
}
```

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Non-numeric or ≤ 0 |
| 400 | `ERR_INVALID_PAYEE_ACCOUNT` | `payeeAccount` | Empty, > 34 chars |
| 400 | `ERR_CHECKSUM_FAILURE` | `payeeAccount` | Modulo 97 fails |
| 400 | `ERR_INVALID_PAYEE_NAME` | `payeeName` | Out of 1–70 range |
| 400 | `ERR_INVALID_AMOUNT` | `amount` | ≤ 0 or wrong precision |
| 400 | `ERR_AMOUNT_EXCEEDS_LIMIT` | `amount` | Exceeds daily transfer limit |
| 400 | `ERR_INVALID_FREQUENCY` | `frequency` | Not a valid enum value |
| 400 | `ERR_START_DATE_TOO_SOON` | `startDate` | < 24 hours from now |
| 400 | `ERR_INVALID_END_DATE` | `endDate` | Before `startDate` |
| 400 | `ERR_INVALID_REFERENCE` | `reference` | Not 1–18 alphanumeric |
| 401 | `ERR_UNAUTHORIZED` | — | Missing/invalid JWT or lacks `SO:CREATE` |
| 401 | `ERR_OWNERSHIP` | — | CUSTOMER does not own account |
| 404 | `ERR_ACC_NOT_FOUND` | — | Account not found |
| 409 | `ERR_SO_DUPLICATE` | — | Identical ACTIVE order exists |

---

## GET /accounts/{accountId}/standing-orders

**Required Permission**: `SO:READ`

### Request

```
GET /accounts/{accountId}/standing-orders
Authorization: Bearer {jwt}
```

### Response 200

```json
{
  "accountId": 101,
  "standingOrderCount": 2,
  "standingOrders": [
    {
      "standingOrderId": "550e8400-e29b-41d4-a716-446655440000",
      "payeeName": "John Smith",
      "amount": "500.00",
      "frequency": "MONTHLY",
      "status": "ACTIVE",
      "nextRunDate": "2026-05-01T00:00:00Z",
      "startDate": "2026-05-01T00:00:00Z",
      "endDate": "2027-05-01T00:00:00Z"
    }
  ]
}
```

### Error Responses

| HTTP | Code | Condition |
|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | Non-numeric accountId |
| 401 | `ERR_UNAUTHORIZED` | Lacks `SO:READ` or ownership |
| 404 | `ERR_ACC_NOT_FOUND` | Account not found |

---

## DELETE /standing-orders/{standingOrderId}

**Required Permission**: `SO:CANCEL`

### Request

```
DELETE /standing-orders/{standingOrderId}
Authorization: Bearer {jwt}
```

| Parameter | In | Type | Required | Constraints |
|---|---|---|---|---|
| `standingOrderId` | path | UUID string | Yes | Valid UUID format |

### Response 200

```json
{
  "standingOrderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CANCELLED",
  "message": "Standing order cancelled successfully."
}
```

### Error Responses

| HTTP | Code | Condition |
|---|---|---|
| 400 | `ERR_INVALID_SO_ID` | Invalid UUID format |
| 401 | `ERR_UNAUTHORIZED` | Lacks `SO:CANCEL` or ownership |
| 403 | `ERR_SO_LOCKED` | Within 24 hours of `nextRunDate` |
| 404 | `ERR_SO_NOT_FOUND` | Standing order not found |
