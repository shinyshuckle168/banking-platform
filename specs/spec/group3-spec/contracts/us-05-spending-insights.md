# API Contracts — US-05: Spending Insights

**Base Path**: `/accounts/{accountId}/insights` and `/accounts/{accountId}/transactions/{transactionId}/category`  
**Authentication**: JWT Bearer Token  
**Required Permission**: `INSIGHTS:READ`

---

## GET /accounts/{accountId}/insights

### Request

```
GET /accounts/{accountId}/insights?year=2026&month=4
Authorization: Bearer {jwt}
```

| Parameter | In | Type | Required | Constraints |
|---|---|---|---|---|
| `accountId` | path | long | Yes | > 0 |
| `year` | query | integer | Yes | 4-digit calendar year |
| `month` | query | integer | Yes | 1–12 |

**Constraint**: The requested `year`/`month` must not be a future month (month that has not started).

### Response 200

```json
{
  "accountId": 101,
  "period": {
    "year": 2026,
    "month": 4,
    "isComplete": false
  },
  "totalDebitSpend": "1800.00",
  "transactionCount": 12,
  "hasUncategorised": false,
  "hasExcludedDisputes": false,
  "dataFresh": true,
  "categoryBreakdown": [
    { "category": "Housing",       "totalAmount": "800.00",  "percentage": "44.44" },
    { "category": "Transport",     "totalAmount": "200.00",  "percentage": "11.11" },
    { "category": "Food & Drink",  "totalAmount": "300.00",  "percentage": "16.67" },
    { "category": "Entertainment", "totalAmount": "150.00",  "percentage": "8.33"  },
    { "category": "Shopping",      "totalAmount": "200.00",  "percentage": "11.11" },
    { "category": "Utilities",     "totalAmount": "100.00",  "percentage": "5.56"  },
    { "category": "Health",        "totalAmount": "50.00",   "percentage": "2.78"  },
    { "category": "Income",        "totalAmount": "0.00",    "percentage": "0.00"  }
  ],
  "topTransactions": [
    {
      "transactionId": 5010,
      "amount": "800.00",
      "type": "TRANSFER",
      "description": "Rent — April"
    }
  ],
  "sixMonthTrend": [
    { "year": 2025, "month": 11, "totalDebitSpend": "1650.00", "isComplete": true,  "accountExisted": true },
    { "year": 2025, "month": 12, "totalDebitSpend": "2100.00", "isComplete": true,  "accountExisted": true },
    { "year": 2026, "month": 1,  "totalDebitSpend": "1720.00", "isComplete": true,  "accountExisted": true },
    { "year": 2026, "month": 2,  "totalDebitSpend": "1490.00", "isComplete": true,  "accountExisted": true },
    { "year": 2026, "month": 3,  "totalDebitSpend": "1900.00", "isComplete": true,  "accountExisted": true },
    { "year": 2026, "month": 4,  "totalDebitSpend": "1800.00", "isComplete": false, "accountExisted": true }
  ]
}
```

**Rules**:
- `categoryBreakdown` always contains exactly 8 entries
- All `percentage` values sum to exactly 100.00 (or 0.00 each if no eligible transactions)
- `sixMonthTrend` always contains exactly 6 entries
- `topTransactions` contains up to 5 entries; fewer if < 5 eligible transactions exist

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Non-numeric or ≤ 0 |
| 400 | `ERR_INVALID_YEAR` | `year` | Missing or not a 4-digit year |
| 400 | `ERR_INVALID_MONTH` | `month` | Missing or not 1–12 |
| 401 | `ERR_UNAUTHORIZED` | — | Lacks `INSIGHTS:READ` or ownership |
| 404 | `ERR_ACC_NOT_FOUND` | — | Account not found |
| 409 | `ERR_FUTURE_MONTH` | — | Requested month has not started |

---

## PUT /accounts/{accountId}/transactions/{transactionId}/category

**Required Permission**: `INSIGHTS:READ`

### Request

```
PUT /accounts/{accountId}/transactions/{transactionId}/category
Authorization: Bearer {jwt}
Content-Type: application/json
```

```json
{
  "category": "Food & Drink"
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `category` | string | Yes | Must exactly match one of 8 agreed values (case-sensitive) |

**Valid categories**: `Housing`, `Transport`, `Food & Drink`, `Entertainment`, `Shopping`, `Utilities`, `Health`, `Income`

### Response 200

```json
{
  "transactionId": 5010,
  "previousCategory": "Shopping",
  "updatedCategory": "Food & Drink",
  "updatedTotalDebitSpend": "1800.00",
  "updatedCategoryBreakdown": [
    { "category": "Housing",       "totalAmount": "800.00",  "percentage": "44.44" },
    { "category": "Transport",     "totalAmount": "200.00",  "percentage": "11.11" },
    { "category": "Food & Drink",  "totalAmount": "500.00",  "percentage": "27.78" },
    { "category": "Entertainment", "totalAmount": "150.00",  "percentage": "8.33"  },
    { "category": "Shopping",      "totalAmount": "0.00",    "percentage": "0.00"  },
    { "category": "Utilities",     "totalAmount": "100.00",  "percentage": "5.56"  },
    { "category": "Health",        "totalAmount": "50.00",   "percentage": "2.78"  },
    { "category": "Income",        "totalAmount": "0.00",    "percentage": "0.00"  }
  ]
}
```

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Non-numeric or ≤ 0 |
| 400 | `ERR_INVALID_TRANSACTION_ID` | `transactionId` | Non-numeric or ≤ 0 |
| 400 | `ERR_MISSING_BODY` | — | Request body absent |
| 401 | `ERR_UNAUTHORIZED` | — | Lacks `INSIGHTS:READ` or ownership |
| 404 | `ERR_ACC_NOT_FOUND` | — | Account not found |
| 404 | `ERR_TX_NOT_FOUND` | — | Transaction not found |
| 422 | `ERR_INVALID_CATEGORY` | `category` | Not one of the 8 agreed values |
