# API Contracts — US-03: Evaluate Notification Event

**Path**: `POST /notifications/evaluate`  
**Authentication**: mTLS (client certificate) OR `X-Api-Key` header (allow-listed ServiceID)  
**Note**: This endpoint is NOT accessed by end-users. It is an internal service-to-service API.

---

## POST /notifications/evaluate

### Request Headers

```
Content-Type: application/json
X-Api-Key: {serviceApiKey}          ← if mTLS not used
```

### Request Body

```json
{
  "eventId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "eventType": "StandingOrderFailure",
  "accountId": 101,
  "customerId": 55,
  "businessTimestamp": "2026-04-08T08:00:00Z",
  "payload": {
    "standingOrderId": "550e8400-e29b-41d4-a716-446655440000",
    "failureReason": "INSUFFICIENT_FUNDS"
  }
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `eventId` | UUID string | Yes | Unique; used for deduplication |
| `eventType` | string | Yes | Must be in Event Classification Matrix |
| `accountId` | long | Yes | > 0; must reference an existing account |
| `customerId` | long | Yes | > 0; must be linked to `accountId` |
| `businessTimestamp` | ISO-8601 UTC | Yes | When the business event occurred |
| `payload` | object | No | Event-specific context |

### Event Classification Matrix

| eventType | Classification | Customer Preference Applies |
|---|---|---|
| `StandingOrderFailure` | Mandatory | No |
| `StatementAvailability` | Mandatory | No |
| `UnusualAccountActivity` | Mandatory | No |
| `StandingOrderCreation` | Optional | Yes |

### Response 200

```json
{
  "eventId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "decision": "RAISED",
  "decisionReason": "Mandatory event: StandingOrderFailure overrides customer preference",
  "customerId": 55,
  "accountId": 101,
  "evaluatedAt": "2026-04-08T08:00:05Z",
  "mandatoryOverride": true
}
```

**Decision values**: `RAISED`, `GROUPED`, `SUPPRESSED`

### Error Responses

| HTTP | Code | field | Condition |
|---|---|---|---|
| 400 | `ERR_MISSING_EVENT_ID` | `eventId` | Missing or malformed UUID |
| 400 | `ERR_MISSING_EVENT_TYPE` | `eventType` | Missing or empty |
| 400 | `ERR_INVALID_ACCOUNT_ID` | `accountId` | Missing or ≤ 0 |
| 400 | `ERR_INVALID_CUSTOMER_ID` | `customerId` | Missing or ≤ 0 |
| 400 | `ERR_INVALID_TIMESTAMP` | `businessTimestamp` | Not valid ISO-8601 |
| 401 | `ERR_UNAUTHORIZED` | — | Unauthenticated or unregistered ServiceID |
| 409 | `ERR_DUPLICATE_EVENT` | `eventId` | EventId already processed |
| 422 | `ERR_UNKNOWN_EVENT_TYPE` | `eventType` | Not in Event Classification Matrix |
| 422 | `ERR_CUSTOMER_ACCOUNT_MISMATCH` | `customerId` | Not linked to `accountId` |
| 422 | `ERR_NOT_ENTITLED` | `customerId` | Customer not entitled to receive notifications |
