# Group 3 — Feature Specification
## Digital Banking Platform

**Feature Branch**: `group3-spec`
**Created**: 2026-04-02
**Last Updated**: 2026-04-07
**Status**: Draft
**Team**: Suraj, Srikrishna, Emad

---

## Business Context

The Banking API is the core service for managing customer accounts and monetary
operations within a retail banking platform. This specification defines the
requirements for five Group 3 stories:

- **US-08** — Get Transaction History
- **US-09** — Standing Order Management
- **US-10** — Evaluate Notification Event
- **US-11** — Get Monthly Statement
- **US-12** — Spending Insights

These features provide customers with auditability over account activity,
automation of recurring payments, timely event awareness, formal account
records, and spending intelligence — while ensuring compliance with Canadian
financial regulations.

This document is the single source of truth for contract generation and
implementation alignment. It is specification-only and intentionally excludes
implementation detail.

---

## Shared Definitions

### Domain Objects

#### User

Represents an authenticated actor with a local application record linked to an
external identity subject.

- `userId` — system-generated unique identifier for the user
- `externalSubjectId` — links this record to the external Identity Provider.
  Used to match the JWT subject claim on every request
- `customerId` — long, foreign key linking this user to their Customer record.
  Used to resolve account ownership and access entitlement across all Group 3
  journeys
- `username` — string, lowercase, between 4 and 30 characters, must begin
  with a letter, letters digits underscores hyphens and periods only
- `password` — string. The hashed representation of the user credential.
  Raw passwords are never stored or returned by the API. Password hashing is
  enforced by the external Identity Service
- `isActive` — boolean. True when the user account is active and permitted to
  authenticate. False when the user has been deactivated. Inactive users must
  not be permitted to access any Group 3 journey
- `roles` — list of assigned Role objects. Effective permissions are derived
  from these roles
- `createdAt` — ISO-8601 UTC timestamp. System-managed, set at user creation
- `updatedAt` — ISO-8601 UTC timestamp. System-managed, updated on each change
  to the user record

Relationships: One user is linked to one Customer record. One user may own
multiple accounts through that customer relationship.

#### Role

A collection of permissions assigned to a user. Roles are the only mechanism
through which permissions are granted. Direct per-user permission grants are
out of scope.

- `roleId` — system-generated unique identifier for the role
- `name` — string, for example CUSTOMER, ADMIN, ACCOUNT_READ_ONLY
- `permissions` — list of permission strings assigned to this role

#### Customer

Represents a person or company that holds banking accounts. Customer records
are created and managed by Group 1. Group 3 stories read and depend on
customer and account data but do not create or delete customer records.

- `customerId` — long, system-generated unique identifier for the customer.
  Used to link accounts and transactions back to the owning customer
- `name` — string. The full name of the person or the registered name of the
  company
- `address` — string. The registered address of the customer
- `type` — string enum, either PERSON or COMPANY. Determines the nature of
  the customer record
- `accounts` — list of Account objects owned by this customer. A customer may
  hold zero or more accounts
- `createdAt` — ISO-8601 UTC timestamp. System-managed, set at creation
- `updatedAt` — ISO-8601 UTC timestamp. System-managed, updated on each change

Relationships: One customer may own many accounts. Customer records are
managed by Group 1 and consumed by Group 3.

#### Account

A customer-held banking product. Account type is immutable after creation.

- `accountId` — long, system-generated unique identifier for the account. Used
  as the path parameter in all account-level endpoints
- `customerId` — long, foreign key linking this account to its owning Customer
  record. Used by Group 3 stories to resolve ownership for access control
- `accountType` — string enum, either CHECKING or SAVINGS. Immutable after
  creation
- `balance` — BigDecimal at exactly two decimal places. The current ledger
  balance of the account in the single system currency
- `interestRate` — BigDecimal, Savings accounts only. Must be a non-negative
  decimal with at most four decimal places. Not present on CHECKING accounts
- `nextCheckNumber` — long, Checking accounts only. Must be a whole number
  greater than or equal to 0 and greater than the current stored value. Not
  present on SAVINGS accounts
- `status` — string enum, either ACTIVE or CLOSED. An account becomes CLOSED
  when it is deleted. CLOSED accounts are removed from normal operational use
  but their records are retained for audit and regulatory purposes
- `createdAt` — ISO-8601 UTC timestamp. System-managed, set at creation
- `updatedAt` — ISO-8601 UTC timestamp. System-managed, updated on each change

Relationships: One account belongs to one customer. One account may have many
transactions. One account may be the source account for many standing orders.
One account produces one statement per closed calendar month.

#### Transaction

A record of a monetary operation on an account. Both SUCCESS and FAILED
outcomes are persisted and retained. Transaction records are treated as
immutable once written.

- `transactionId` — long, system-generated unique identifier for the
  transaction
- `accountId` — long, foreign key linking this transaction to its account
- `amount` — BigDecimal at exactly two decimal places. The monetary value of
  the transaction in the single system currency. Must be greater than zero
- `type` — string enum, one of DEPOSIT, WITHDRAW, or TRANSFER. Determines the
  nature of the monetary operation
- `timestamp` — string in ISO-8601 UTC format. The date and time the
  transaction occurred. Used to determine which period the transaction belongs
  to for history filtering and statement generation
- `description` — string. A human-readable label describing the transaction
  such as a merchant name or payment reference
- `status` — string enum, either SUCCESS or FAILED. Both outcomes are persisted
- `idempotencyKey` — string, optional. Present only on transactions originating
  from Deposit, Withdraw, or Transfer requests that supplied an
  `Idempotency-Key` header. Used to detect and deduplicate repeated requests

Relationships: Many transactions belong to one account.

#### StandingOrder

A recurring payment instruction that tells the system to transfer funds from a
source account to a named payee on a defined schedule.

- `standingOrderId` — UUID string of exactly 36 characters, system-generated
  unique identifier for the standing order
- `sourceAccountId` — long, foreign key linking this order to the account
  funds are drawn from
- `payeeAccount` — string, maximum 34 characters. The account number of the
  recipient. Must pass Modulo 97 checksum validation
- `payeeName` — string, between 1 and 70 characters. The name of the payee
- `amount` — BigDecimal at exactly two decimal places. The fixed amount to
  transfer on each execution. Must be greater than zero and must not exceed the
  customer's daily transfer limit
- `frequency` — string enum, one of DAILY, WEEKLY, MONTHLY, or QUARTERLY.
  Determines how often the standing order executes
- `startDate` — DateTime in UTC. The date the standing order first runs. Must
  be at least 24 hours from the time of creation
- `endDate` — DateTime in UTC, optional. The date after which the standing
  order stops executing. If not provided the order continues until cancelled
- `reference` — string, between 1 and 18 alphanumeric characters. A payment
  reference visible to the payee
- `status` — string enum, one of ACTIVE, CANCELLED, LOCKED, or TERMINATED
- `nextRunDate` — DateTime in UTC. The next scheduled execution date calculated
  by the system based on frequency and holiday rules

Relationships: Many standing orders may belong to one source account.

#### NotificationEvent

A business event submitted for evaluation to determine whether a customer
notification should be raised.

- `eventId` — UUID string, system-generated unique identifier for the event.
  Used for deduplication
- `eventType` — string, must match a defined event type in the Event
  Classification Matrix
- `accountId` — long, identifies the account the event relates to
- `customerId` — long, identifies the customer linked to the account
- `businessTimestamp` — string in ISO-8601 UTC format. The time the business
  event occurred
- `payload` — object, optional. Event-specific contextual data

#### MonthlyStatement

The formal monthly account record for an eligible account and closed period.

- `accountId` — long, identifies the account the statement belongs to.
  Immutable
- `period` — string in ISO month format YYYY-MM, for example 2026-03
- `openingBalance` — BigDecimal at exactly two decimal places. Must match the
  closing balance of the prior period's issued statement
- `closingBalance` — BigDecimal at exactly two decimal places. The closing
  balance for the statement period
- `totalMoneyIn` — BigDecimal at exactly two decimal places. Sum of all DEPOSIT
  and TRANSFER-in transactions with status SUCCESS in the period
- `totalMoneyOut` — BigDecimal at exactly two decimal places. Sum of all
  WITHDRAW and TRANSFER-out transactions with status SUCCESS in the period
- `transactions` — list of all transaction records for the period including
  both SUCCESS and FAILED outcomes. Never truncated regardless of volume
- `versionNumber` — integer, starts at 1 and is incremented on each correction
- `correctionSummary` — string, present on corrected versions only. Lists every
  field that differs from the prior version
- `generatedAt` — string in ISO-8601 UTC format. System-managed timestamp of
  statement assembly

#### SpendingInsight

The insight record returned for a selected calendar month derived from WITHDRAW
and TRANSFER-out transactions with status SUCCESS only.

- `accountId` — long, references the account the insight was generated for
- `period.year` — integer, four-digit calendar year of the selected month
- `period.month` — integer between 1 and 12 representing the selected month
- `period.isComplete` — boolean. True when the selected month has ended. False
  when the month is still in progress
- `totalDebitSpend` — BigDecimal at exactly two decimal places. The sum of all
  eligible SUCCESS WITHDRAW and TRANSFER-out transaction amounts for the
  selected month
- `transactionCount` — integer, the number of eligible SUCCESS transactions
  included in the insight calculation
- `hasUncategorised` — boolean. True when one or more transactions could not
  be automatically assigned to one of the eight categories
- `hasExcludedDisputes` — boolean. True when one or more transactions were
  excluded because they are under dispute
- `dataFresh` — boolean. False when upstream transaction history data is
  delayed or incomplete at the time the insight is generated
- `categoryBreakdown` — list of all eight categories each with total amount and
  percentage. Always eight entries even where some are zero. Percentages sum
  to exactly 100
- `topTransactions` — the five largest individual eligible transactions in the
  selected month by amount
- `sixMonthTrend` — list of exactly six entries one per calendar month showing
  total debit spend. Always six entries even where some are zero

#### ErrorResponse

Returned for all 400, 401, 404, 409, 410, and 422 responses.

```
{
  "code": "string",
  "message": "string",
  "field": "string"
}
```

- `code` — string. A machine-readable error code identifying the type of
  failure, for example ACCOUNT_NOT_FOUND or INVALID_DATE_RANGE
- `message` — string. A human-readable explanation of what went wrong
- `field` — string, optional. Present when the error relates to a specific
  request field. For example field="amount" when the amount is invalid

---

### Enums

- `AccountType` — CHECKING, SAVINGS
- `AccountStatus` — ACTIVE, CLOSED
- `TransactionType` — DEPOSIT, WITHDRAW, TRANSFER
- `TransactionStatus` — SUCCESS, FAILED
- `StandingOrderStatus` — ACTIVE, CANCELLED, LOCKED, TERMINATED
- `Frequency` — DAILY, WEEKLY, MONTHLY, QUARTERLY
- `SpendingCategory` — Housing, Transport, Food & Drink, Entertainment,
  Shopping, Utilities, Health, Income

---

### Actors and Roles

Group 3 adopts the shared RBAC model defined in the Group 2 Banking API
specification. Roles are collections of permissions. Permissions are defined
separately from roles so that any role's access can be changed without
rewriting the role definition itself. Permission checks and ownership checks
are independent requirements and both must pass.

**CUSTOMER**
An authenticated end-user with self-service access to their own accounts only.

Allowed for Group 3 stories:
- View transaction history for their own accounts
- Create, read, and cancel standing orders on their own accounts
- Receive notifications for their own accounts
- View monthly statements for their own accounts
- View spending insights for their own accounts

Not allowed:
- Access another customer's resources
- Delete account records
- Grant or revoke roles

**ADMIN**
A privileged operator with unrestricted access across all resources. Bypasses
ownership checks. Permission and audit rules still apply.

Allowed for Group 3 stories:
- All operations that CUSTOMER may perform, on any account
- No ownership restriction applies

**ACCOUNT_READ_ONLY** (optional custom role)
- Permissions: `ACCOUNT:READ`, `TRANSACTION:READ`, `STATEMENT:READ`

**ACCOUNT_MANAGER** (optional custom role)
- Permissions: `ACCOUNT:CREATE`, `ACCOUNT:READ`, `ACCOUNT:UPDATE`,
  `SO:READ`

**Permission Assignment Table**

| Permission | CUSTOMER | ADMIN | ACCOUNT_READ_ONLY | ACCOUNT_MANAGER |
|---|---|---|---|---|
| `TRANSACTION:READ` | Own only | All | All | No |
| `TRANSACTION:DEPOSIT` | Own only | All | No | No |
| `TRANSACTION:WITHDRAW` | Own only | All | No | No |
| `TRANSACTION:TRANSFER` | Own only | All | No | No |
| `ACCOUNT:READ` | Own only | All | All | All |
| `ACCOUNT:UPDATE` | Own only | All | No | All |
| `SO:READ` | Own only | All | No | All |
| `SO:CREATE` | Own only | All | No | No |
| `SO:CANCEL` | Own only | All | No | No |
| `NOTIFICATION:READ` | Own only | All | No | No |
| `STATEMENT:READ` | Own only | All | All | No |
| `INSIGHTS:READ` | Own only | All | No | No |

**Role Assignment Rules**

- A user may hold more than one role
- Roles may be assigned at user creation or via admin-managed updates
- Only ADMIN may grant or revoke roles
- Direct per-user permission grants are out of scope. Effective permissions are
  derived from assigned roles only
- Role assignments must be persisted

---

### Idempotency

Monetary operations — standing order execution (which produces TRANSFER
transactions), and any deposit, withdraw, or transfer operations surfaced
through Group 3 journeys — must be idempotent when the caller supplies the
same `Idempotency-Key` header value on a repeated request.

- The `Idempotency-Key` header must be a non-empty string. A UUID is
  recommended
- When a request is received with an `Idempotency-Key` that has already been
  processed, the original response must be returned and no balance change or
  transaction record must be created again
- The `idempotencyKey` value is stored on the Transaction record when present
- Idempotency records may be retained for 24 to 72 hours and may be purged
  thereafter. Purging idempotency records must not affect the retention of
  transaction records, account records, or audit logs
- Standing order execution uses an internally generated idempotency key per
  scheduled occurrence so that duplicate execution triggers for the same cycle
  never result in a second payment

---

### Data Retention and Audit Policy

- All transaction records, standing order records, statement records, and audit
  data must be retained for 7 years in line with the shared platform data
  retention policy based on CRA and FINTRAC requirements
- The system must maintain an append-only audit log capturing all operations
  including both successful and failed actions
- Audit logs must include actor identity, role, action, resource type, resource
  identifier, timestamp, and outcome
- Transaction records must be treated as immutable once written
- Both SUCCESS and FAILED monetary operation outcomes must be persisted as
  transaction records and must not be deleted during the 7-year retention period
- Statement originals must never be overwritten. Corrections must create a new
  version with an incremented version number
- CLOSED account records must remain stored for audit and regulatory purposes
  even though they are removed from normal operational access

---

# US-08 — Get Transaction History

## Description

Returns the transaction history for a customer's eligible ACTIVE account for a
selected date range. Both PENDING and POSTED transactions are returned and
clearly distinguished. PENDING transactions are authorised but not yet cleared.
POSTED transactions are finalised. Both SUCCESS and FAILED transaction outcomes
are included in the history. Transaction history can only be retrieved by the
authenticated user for their own accounts. CLOSED accounts are accessible for
up to 90 days after closure. A PDF export of the statement is also available.

Transactions use the shared Transaction domain object. Each transaction may
carry an optional `idempotencyKey` field where the originating monetary
operation supplied an `Idempotency-Key` header.

---

## HTTP Contract

### Endpoint 1 — Retrieve Transaction History

- Method: `GET`
- Path: `/accounts/{accountId}/transactions`
- Query Parameters:
  - `startDate` (optional) — ISO-8601 UTC date. Defaults to 28 days before
    the current date if not supplied
  - `endDate` (optional) — ISO-8601 UTC date. Defaults to the current date
    and time if not supplied. If a future date is supplied the system overrides
    it to the current date and time silently
- Request Body: None
- Expected Response Codes:
  - `200` Transactions returned successfully
  - `400` Invalid `accountId` format, invalid date format, or date range
    exceeds 366 days
  - `401` Unauthenticated or caller not authorised to access this account
  - `404` Account not found
  - `409` Account is CLOSED and the 90-day post-closure access window has
    expired

### Endpoint 2 — Export PDF Statement

- Method: `GET`
- Path: `/accounts/{accountId}/transactions/export`
- Query Parameters:
  - `startDate` (required) — ISO-8601 UTC date
  - `endDate` (required) — ISO-8601 UTC date
- Request Body: None
- Expected Response Codes:
  - `200` PDF statement returned successfully
  - `400` Invalid parameters or date range exceeds 366 days
  - `401` Unauthenticated or caller not authorised
  - `404` Account not found

### Response Fields — Retrieve Transaction History (200)

- `accountId` — long, the account the history was retrieved for
- `startDate` — the effective start date used after any system overrides
- `endDate` — the effective end date used after any system overrides
- `transactionCount` — total number of transaction records returned
- `transactions` — list ordered with PENDING first then POSTED in chronological
  order. Each entry includes:
  - `transactionId` — long
  - `amount` — BigDecimal at exactly two decimal places
  - `type` — DEPOSIT, WITHDRAW, or TRANSFER
  - `status` — SUCCESS or FAILED
  - `timestamp` — ISO-8601 UTC datetime
  - `description` — string
  - `idempotencyKey` — string, present only when the originating request
    supplied an `Idempotency-Key` header

### Response Fields — Export PDF Statement (200)

- Content-Type: `application/pdf`
- PDF header includes Statement of Activity label, masked account identifier,
  and effective date range
- PDF body contains all transactions for the period in chronological order
- Identical export requests return the same PDF file

---

## Business Rules

- Transaction history is only returned for accounts the caller owns
- If no date range is supplied the system defaults to the last 28 days
- If the supplied `endDate` is in the future the system overrides it to the
  current date and time silently
- The maximum date range for a single request is 366 days
- PENDING transactions must appear at the top of the list and be clearly
  distinguishable from POSTED transactions
- POSTED transactions must be displayed in chronological order by effective date
- Both SUCCESS and FAILED transaction outcomes must appear in the history
- CLOSED accounts may be accessed for up to 90 days after the account status
  changed to CLOSED. After 90 days the history is no longer accessible through
  self-service
- Transaction records are retained for 7 years per CRA and FINTRAC requirements
- Transaction records are immutable and must not be modified after being written

---

## Security Constraints

- The caller must be authenticated. A valid JWT Bearer token must be present
- Required permission: `TRANSACTION:READ`
- CUSTOMER callers must own the specified account. Permission check and
  ownership check are independent and both must pass
- ADMIN callers may access any account regardless of ownership
- CUSTOMER caller accessing an account they do not own → `401`
- Caller without `TRANSACTION:READ` permission → `401`

---

## Validation Rules

- `accountId` must be a valid long greater than `0`
- `startDate` and `endDate` when supplied must be valid ISO-8601 UTC datetime
  values
- The difference between `endDate` and `startDate` must not exceed 366 days
- If `endDate` is in the future the system overrides it silently without error
- All monetary amounts in the response must use exactly two decimal places

---

## Error Mapping

- Invalid `accountId` format → `400`, `field="accountId"`
- Invalid `startDate` format → `400`, `field="startDate"`
- Invalid `endDate` format → `400`, `field="endDate"`
- Date range exceeds 366 days → `400`, `field="endDate"`
- Caller unauthenticated or lacks `TRANSACTION:READ` permission → `401`
- CUSTOMER caller does not own the account → `401`
- Account not found → `404`
- Account is CLOSED and 90-day window has expired → `409`,
  `code="ERR_ACC_003"`

---

## Edge Cases

- No transactions in the selected range → return empty list with
  `transactionCount` of `0`. Not an error
- `endDate` in the future → override to current datetime silently
- CLOSED account within 90-day window → return history normally
- CLOSED account after 90-day window → `409`
- PENDING transaction that later becomes POSTED → must appear correctly in
  subsequent requests
- Upstream data delayed → flag that the view may not reflect the most recent
  activity
- Identical PDF export requests → return the same file without regenerating
- A transaction carrying an `idempotencyKey` must display that key in the
  response so the customer can trace the originating request

---

## Constraints

1. History must only be returned for accounts the caller owns. A caller may
   never see transactions outside their entitlement
2. Maximum date range is hard-capped at 366 days with no exceptions
3. Transaction records are immutable. The system must not permit modification
   of any transaction record after it is written
4. Both SUCCESS and FAILED transactions must always be included. The system
   must not filter out either status without explicit business agreement
5. Transaction records must be retained for 7 years
6. PDF export must be idempotent. Identical requests must return the same
   output without regenerating content

---

## What the Customer View Requires from the Back End

- All transaction records for the effective date range ordered PENDING first
  then POSTED in chronological order
- For each transaction: `transactionId` (long), `amount`, `type`, `status`,
  `timestamp`, `description`, and `idempotencyKey` where present
- A clear signal distinguishing PENDING from POSTED
- The effective `startDate` and `endDate` after any system overrides
- A total transaction count for summary display
- A data freshness indicator when upstream data is delayed
- For PDF export: a file with Content-Type `application/pdf`, the Statement of
  Activity header, masked account identifier, and effective date range

---

## Acceptance Criteria

### Positive Scenario 1 — History returned for a valid date range

- Given an existing ACTIVE account with transaction activity in the selected
  range and a CUSTOMER caller with `TRANSACTION:READ` who owns the account
- When a GET request is submitted with a valid `accountId` and date range
- Then the API returns `200` with PENDING transactions listed first, POSTED
  transactions in chronological order, each transaction including all required
  fields including `idempotencyKey` where present, `transactionCount`
  reflecting total records, and the effective `startDate` and `endDate` used

### Positive Scenario 2 — No date range defaults to last 28 days

- Given an existing ACTIVE account and a CUSTOMER caller with `TRANSACTION:READ`
  who owns the account
- When a GET request is submitted with no `startDate` or `endDate`
- Then the API returns `200` with transactions for the last 28 days and
  `startDate` in the response reflecting the system-applied default

### Positive Scenario 3 — Future endDate overridden silently

- Given an existing ACTIVE account and a CUSTOMER caller with `TRANSACTION:READ`
- When a GET request is submitted with `endDate` set to a future date
- Then the API returns `200` with `endDate` overridden to the current datetime
  and no error or warning returned

### Positive Scenario 4 — PDF export is idempotent

- Given an existing ACTIVE account and a CUSTOMER caller with `TRANSACTION:READ`
- When two identical GET requests are submitted to the export endpoint
- Then both return `200` with the same PDF file content

### Negative Scenario 1 — Caller lacks TRANSACTION:READ permission

- Given a caller without `TRANSACTION:READ`
- When a GET request is submitted
- Then the API returns `401` with an `ErrorResponse` where `code` identifies
  the permission failure and `message` explains the caller is not authorised

### Negative Scenario 2 — CUSTOMER caller does not own the account

- Given a CUSTOMER caller and an account that does not belong to them
- When a GET request is submitted
- Then the API returns `401` with an `ErrorResponse` where `code` identifies
  the ownership failure

### Negative Scenario 3 — Date range exceeds 366 days

- Given a CUSTOMER caller with `TRANSACTION:READ` who owns the account
- When a GET request is submitted with a date range greater than 366 days
- Then the API returns `400` with an `ErrorResponse` where `code` identifies
  the range violation, `message` explains the maximum is 366 days, and `field`
  is set to `endDate`

### Negative Scenario 4 — CLOSED account outside 90-day window

- Given an account with status CLOSED more than 90 days ago
- When a GET request is submitted
- Then the API returns `409` with an `ErrorResponse` where `code` is
  `ERR_ACC_003` and `message` explains access is no longer available

### Negative Scenario 5 — Invalid accountId format

- Given a non-numeric value as `accountId`
- When a GET request is submitted
- Then the API returns `400` with an `ErrorResponse` where `code` identifies
  the format error, `message` explains `accountId` must be a valid long greater
  than zero, and `field` is set to `accountId`

---

# US-09 — Standing Order Management

## Description

Allows customers to create, view, and cancel recurring payment instructions
from an eligible ACTIVE source account to a named payee. Standing orders
execute automatically on a defined schedule. The system handles holiday
shifting and retry logic for insufficient funds. Standing order execution
produces TRANSFER transactions and is idempotent — a duplicate execution
trigger for the same scheduled occurrence must not result in a second payment.
Both successful and failed execution outcomes are persisted as transaction
records.

---

## HTTP Contract

### Endpoint 1 — Create Standing Order

- Method: `POST`
- Path: `/accounts/{accountId}/standing-orders`
- Request Body:
  - `payeeAccount` (string, required) — maximum 34 characters, must pass
    Modulo 97 checksum
  - `payeeName` (string, required) — between 1 and 70 characters
  - `amount` (BigDecimal, required) — greater than zero, exactly two decimal
    places, must not exceed the customer's daily transfer limit
  - `frequency` (string, required) — one of DAILY, WEEKLY, MONTHLY, QUARTERLY
  - `startDate` (DateTime UTC, required) — at least 24 hours from creation
  - `endDate` (DateTime UTC, optional) — must be after `startDate` if provided
  - `reference` (string, required) — between 1 and 18 alphanumeric characters
- Expected Response Codes:
  - `201` Standing order created and set to ACTIVE
  - `400` Invalid request format or field validation failure
  - `401` Unauthenticated or caller not authorised
  - `404` Account not found
  - `409` Identical active standing order already exists

### Endpoint 2 — List Standing Orders

- Method: `GET`
- Path: `/accounts/{accountId}/standing-orders`
- Request Body: None
- Expected Response Codes:
  - `200` Standing orders returned successfully
  - `400` Invalid `accountId` format
  - `401` Unauthenticated or caller not authorised
  - `404` Account not found

### Endpoint 3 — Cancel Standing Order

- Method: `DELETE`
- Path: `/standing-orders/{standingOrderId}`
- Request Body: None
- Expected Response Codes:
  - `200` Standing order cancelled successfully
  - `400` Invalid `standingOrderId` format
  - `401` Unauthenticated or caller not authorised
  - `403` Processing lock applies — cancellation within 24 hours of next run
  - `404` Standing order not found

### Response Fields — Create Standing Order (201)

- `standingOrderId` — UUID of exactly 36 characters, system-generated
- `sourceAccountId` — long, the account funds are drawn from
- `payeeAccount` — the validated payee account number
- `payeeName` — the payee name as submitted
- `amount` — BigDecimal at exactly two decimal places
- `frequency` — the selected frequency enum value
- `startDate` — the confirmed start date in UTC
- `endDate` — the end date if provided, null if the order runs indefinitely
- `reference` — the payment reference
- `status` — ACTIVE
- `nextRunDate` — the first scheduled execution date calculated accounting for
  weekends and Canadian Bank Holidays
- `message` — human-readable confirmation

### Response Fields — List Standing Orders (200)

- `accountId` — long, the account
- `standingOrderCount` — total number of standing orders returned
- `standingOrders` — list each including `standingOrderId`, `payeeName`,
  `amount`, `frequency`, `status`, `nextRunDate`, `startDate`, and `endDate`

---

## Business Rules

- A standing order may only be created from an ACTIVE account the caller owns
- `startDate` must be at least 24 hours from the time of creation
- `amount` must be greater than zero and must not exceed the customer's daily
  transfer limit
- `payeeAccount` must pass Modulo 97 checksum validation
- Creation must be blocked if an identical ACTIVE order already exists for the
  same payee account, amount, and frequency
- Cancellation is forbidden within 24 hours of `nextRunDate`
- If funds are insufficient on execution the attempt is retried once later the
  same day before being marked FAILED_INSUFFICIENT_FUNDS
- Both successful and failed execution outcomes must be persisted as transaction
  records with `type=TRANSFER`
- Each execution attempt uses an internally generated idempotency key so that
  duplicate execution triggers for the same scheduled cycle never result in a
  second payment
- If `nextRunDate` falls on a weekend or Canadian Bank Holiday, execution is
  shifted to the next business day. The original frequency cycle is unchanged
- Standing order records are retained for 7 years per CRA and FINTRAC
  requirements

---

## Execution Logic — Background Scheduler

The scheduler runs daily at 00:01 AM UTC and processes all standing orders
where `nextRunDate` matches the current date.

**Holiday Handling** — If `nextRunDate` falls on a weekend or Canadian Bank
Holiday, execution shifts to the next business day. The original frequency
cycle is not affected.

**Retry Pattern for Insufficient Funds**
- 08:00 AM UTC — First attempt. If insufficient funds the order is marked
  RETRY_PENDING
- 04:00 PM UTC — Final attempt. If still insufficient the order is marked
  FAILED_INSUFFICIENT_FUNDS, the cycle is skipped, a FAILED transaction record
  with `type=TRANSFER` is persisted, and a mandatory notification is triggered

---

## Security Constraints

- The caller must be authenticated. A valid JWT Bearer token must be present
- Required permission for create: `SO:CREATE`
- Required permission for list: `SO:READ`
- Required permission for cancel: `SO:CANCEL`
- CUSTOMER callers must own the specified account. Permission check and
  ownership check are independent and both must pass
- ADMIN callers may perform all operations on any account regardless of
  ownership
- CUSTOMER caller on an account they do not own → `401`
- Caller without the required permission → `401`
- Cancellation within 24 hours of `nextRunDate` → `403`

---

## Validation Rules

- `accountId` must be a valid long greater than `0`
- `standingOrderId` must be a valid UUID
- `payeeAccount` — non-empty, maximum 34 characters, must pass Modulo 97
  checksum
- `payeeName` — between 1 and 70 characters
- `amount` — positive BigDecimal at most two decimal places, must not exceed
  daily transfer limit
- `frequency` — one of DAILY, WEEKLY, MONTHLY, QUARTERLY
- `startDate` — valid UTC datetime at least 24 hours from the request time
- `endDate` — valid UTC datetime after `startDate` when provided
- `reference` — between 1 and 18 alphanumeric characters
- All monetary amounts in the response must use exactly two decimal places

---

## Error Mapping

- Invalid `accountId` format → `400`, `field="accountId"`
- Invalid `standingOrderId` format → `400`, `field="standingOrderId"`
- Invalid or missing `payeeAccount` → `400`, `field="payeeAccount"`
- `payeeAccount` fails Modulo 97 checksum → `400`, `field="payeeAccount"`
- Invalid or missing `payeeName` → `400`, `field="payeeName"`
- Missing or non-positive `amount` → `400`, `field="amount"`
- `amount` exceeds daily transfer limit → `400`, `field="amount"`
- Invalid or missing `frequency` → `400`, `field="frequency"`
- `startDate` less than 24 hours from now → `400`, `field="startDate"`
- `endDate` before `startDate` → `400`, `field="endDate"`
- Invalid `reference` → `400`, `field="reference"`
- Caller unauthenticated or lacks required permission → `401`
- CUSTOMER caller does not own the account → `401`
- Cancellation within 24 hours of `nextRunDate` → `403`,
  `code="ERR_SO_LOCKED"`
- Account not found → `404`
- Standing order not found → `404`
- Identical active standing order exists → `409`, `code="ERR_SO_DUPLICATE"`

---

## Edge Cases

- No standing orders for the account → return empty list with
  `standingOrderCount` of `0`. Not an error
- `nextRunDate` on a weekend or bank holiday → shift to next business day
  without changing the frequency cycle
- Insufficient funds at 08:00 AM → retry at 04:00 PM before marking as failed
- Account set to CLOSED after standing order creation but before execution →
  fail gracefully and persist a FAILED transaction record with `type=TRANSFER`
- Cancellation exactly 24 hours before `nextRunDate` → treat as within lock
  window and reject with `403`
- Identical creation request submitted twice → second must return `409`
- `endDate` passes without explicit cancellation → system sets status to
  TERMINATED and stops further execution
- Duplicate execution trigger for the same scheduled cycle → idempotency key
  prevents a second payment. Original execution outcome is returned

---

## Constraints

1. Standing orders may only be managed by callers who own the source account
2. `startDate` must always be at least 24 hours in the future with no
   exceptions
3. `payeeAccount` must always pass Modulo 97 checksum before acceptance
4. Duplicate detection compares payee account, amount, and frequency against
   all ACTIVE orders. Two identical ACTIVE orders must never coexist
5. Cancellation within 24 hours of `nextRunDate` is forbidden
6. Both successful and failed execution outcomes must be persisted as
   transaction records with `type=TRANSFER`
7. Standing order execution is idempotent. A duplicate trigger for the same
   cycle must never result in a second payment
8. Holiday shifting must not affect the frequency cycle
9. Standing order records must be retained for 7 years

---

## What the Customer View Requires from the Back End

- For creation: `standingOrderId`, `status` set to ACTIVE, `nextRunDate`
  calculated accounting for holidays, and a confirmation message
- For listing: all standing orders with `standingOrderId`, `payeeName`,
  `amount`, `frequency`, `status`, `nextRunDate`, `startDate`, `endDate`, and
  total count
- For cancellation: confirmation the order is CANCELLED, or if rejected a
  clear message explaining the processing lock and when cancellation can next
  be attempted
- For execution outcomes: a notification on successful execution and a
  notification on final failed retry with the transaction record carrying
  `type=TRANSFER` and `status=FAILED`

---

## Acceptance Criteria

### Positive Scenario 1 — Standing order created by CUSTOMER

- Given an existing ACTIVE account and a CUSTOMER caller with `SO:CREATE`
  who owns the account
- When a POST request is submitted with valid details
- Then the API returns `201` with a system-generated `standingOrderId`,
  `status` set to ACTIVE, `nextRunDate` calculated accounting for holidays,
  and a confirmation message

### Positive Scenario 2 — Standing order list returned

- Given an account with two ACTIVE standing orders and a CUSTOMER caller with
  `SO:READ` who owns the account
- When a GET request is submitted to the list endpoint
- Then the API returns `200` with `standingOrderCount` of `2` and both orders
  listed with correct fields

### Positive Scenario 3 — Standing order cancelled

- Given an ACTIVE standing order with `nextRunDate` more than 24 hours away
  and a CUSTOMER caller with `SO:CANCEL` who owns the account
- When a DELETE request is submitted
- Then the API returns `200` with `status` set to CANCELLED and a confirmation
  message

### Positive Scenario 4 — Execution is idempotent

- Given an ACTIVE standing order that has already executed for its scheduled
  cycle
- When a duplicate execution trigger arrives for the same cycle
- Then no second payment is made and no second transaction record is created.
  The original execution outcome is returned

### Negative Scenario 1 — startDate less than 24 hours from now

- Given a CUSTOMER caller with `SO:CREATE` who owns the account
- When a POST request is submitted with `startDate` less than 24 hours away
- Then the API returns `400` with `code` identifying the timing violation,
  `message` explaining the start date must be at least 24 hours in the future,
  and `field` set to `startDate`

### Negative Scenario 2 — Duplicate standing order

- Given an ACTIVE order for payee X with amount 100.00 and frequency MONTHLY
- When a POST request is submitted with the same payee, amount, and frequency
- Then the API returns `409` with `code` of `ERR_SO_DUPLICATE`

### Negative Scenario 3 — Cancellation within processing lock

- Given an ACTIVE order with `nextRunDate` less than 24 hours away
- When a DELETE request is submitted
- Then the API returns `403` with `code` of `ERR_SO_LOCKED`

### Negative Scenario 4 — Caller lacks SO:CREATE permission

- Given a caller without `SO:CREATE`
- When a POST request is submitted
- Then the API returns `401` with `code` identifying the permission failure

### Negative Scenario 5 — CUSTOMER caller does not own the account

- Given a CUSTOMER caller and an account that does not belong to them
- When a POST request is submitted
- Then the API returns `401` with `code` identifying the ownership failure

### Negative Scenario 6 — Amount exceeds daily transfer limit

- Given a CUSTOMER caller with `SO:CREATE` who owns the account
- When a POST request is submitted with `amount` exceeding the daily limit
- Then the API returns `400` with `code` identifying the limit violation and
  `field` set to `amount`

### Negative Scenario 7 — payeeAccount fails checksum

- Given a CUSTOMER caller with `SO:CREATE` who owns the account
- When a POST request is submitted with a `payeeAccount` failing Modulo 97
- Then the API returns `400` with `code` identifying the checksum failure and
  `field` set to `payeeAccount`

---

# US-10 — Evaluate Notification Event

## Description

Evaluates an incoming business event to determine whether a customer
notification should be raised. The system applies policy classification,
customer entitlement, permission checks, and preference rules to produce a
traceable decision outcome — raised, grouped, or suppressed. This operation is
invoked by upstream event producers such as payments, statements, and fraud
detection. It does not depend on any specific delivery channel.

---

## HTTP Contract

### Endpoint — Evaluate Notification Event

- Method: `POST`
- Path: `/notifications/evaluate`
- Request Body:
  - `eventId` (string UUID, required) — unique identifier for the event, used
    for deduplication
  - `eventType` (string, required) — must match a defined type in the Event
    Classification Matrix
  - `accountId` (long, required) — identifies the account the event relates to
  - `customerId` (long, required) — identifies the customer linked to the
    account
  - `businessTimestamp` (ISO-8601, required) — the time the business event
    occurred
  - `payload` (object, optional) — event-specific contextual data
- Expected Response Codes:
  - `200` Event evaluated and decision recorded
  - `400` Malformed request or missing required fields
  - `401` Unauthenticated or unregistered event source
  - `409` Duplicate `eventId` already processed
  - `422` Semantic validation failure such as unknown `eventType`

### Response Fields — Evaluate Notification Event (200)

- `eventId` — the UUID of the evaluated event
- `decision` — string enum, one of raised, grouped, or suppressed
- `decisionReason` — string. The business rule that produced this decision
- `customerId` — long, the customer the decision applies to
- `accountId` — long, the account the event relates to
- `evaluatedAt` — ISO-8601 UTC timestamp of when the decision was made
- `mandatoryOverride` — boolean. True when a mandatory event overrode a
  customer opt-out preference

---

## Event Classification Matrix

| Event Type | Classification | Customer Preference Applies | Notes |
|---|---|---|---|
| Standing Order Failure | Mandatory | No | Always triggers notification outcome |
| Statement Availability | Mandatory | No | Always triggers notification outcome |
| Unusual Account Activity | Mandatory | No | Always triggers notification outcome |
| Standing Order Creation | Optional | Yes | Confirmation only; customer may opt out |

---

## Business Rules

- Business events must be defined before any delivery method is considered
- The system must separate mandatory service notifications from optional
  informative notifications
- Standing order failure, statement availability, and unusual account activity
  are required notification events
- Standing order creation is an optional confirmation event
- Notification entitlement must be based on permissions and role bundles, not
  on role names alone
- The system must retain a decision record for every evaluation outcome
- A mandatory event must produce a notification outcome even if the customer
  has opted out of that event category
- Multiple events of the same type arriving close together may be grouped or
  suppressed, but mandatory events must never be suppressed
- Each evaluation must produce exactly one decision record

---

## Security Constraints

- The caller must be authenticated via mTLS or API Key
- The caller must be a registered upstream event producer with ServiceID
  validated against an allow-list
- CUSTOMER callers identified by their user `id` must own the account
  referenced in the event and hold the `NOTIFICATION:READ` permission through
  their assigned role
- ADMIN callers may access any account regardless of ownership

---

## Validation Rules

- `eventId` must be a valid UUID and is required
- `eventType` must be a non-empty string matching a defined type in the Event
  Classification Matrix
- `accountId` must be a valid long greater than `0` identifying an existing
  eligible account
- `customerId` must be a valid long greater than `0` identifying an existing
  customer linked to the account
- `businessTimestamp` must be a valid ISO-8601 datetime
- `payload` when provided must be a valid JSON object
- The request must not contain fields outside the defined request contract

---

## Error Mapping

- Missing or malformed `eventId` → `400`, `field="eventId"`
- Missing or empty `eventType` → `400`, `field="eventType"`
- Missing or invalid `accountId` → `400`, `field="accountId"`
- Missing or invalid `customerId` → `400`, `field="customerId"`
- Missing or malformed `businessTimestamp` → `400`,
  `field="businessTimestamp"`
- Unsupported field submitted in request body → `400`
- Unauthenticated caller or unregistered ServiceID → `401`
- Duplicate `eventId` already processed → `409`, `field="eventId"`
- Unknown `eventType` not in Event Classification Matrix → `422`,
  `field="eventType"`
- `customerId` not linked to the specified `accountId` → `422`,
  `field="customerId"`
- Customer not entitled to receive notifications for the account → `422`,
  `field="customerId"`

---

## Edge Cases

| Category | Edge Case | Expected Result | Forbidden |
|---|---|---|---|
| Timing & Concurrency | Same `eventId` received twice | Deduplicate using `eventId`. Discard second. Log duplicate receipt | Raise a second notification for the same event |
| Volume & Suppression | Grouping collapses a mandatory event with optional events | Evaluate mandatory or optional classification individually before grouping. Mandatory events must always produce a notification outcome | Suppress or collapse a mandatory notification |
| Data Integrity | Customer opted out of a category that later becomes mandatory | Treat policy classification as authoritative over preference. Raise the notification. Record that preference was overridden | Skip a mandatory notification because of a prior opt-out |
| Multi-Entity | Account with two customers where only one holds the notification permission | Evaluate entitlement independently per customer. Entitled customer receives outcome | Apply one customer's permission to another on the same account |
| Auditability | Notification grouped or suppressed | Record a decision record with `eventId`, `accountId`, `customerId`, outcome, rule applied, and timestamp | Suppress or group without producing a decision record |

---

## Constraints

| # | Constraint | Rule |
|---|---|---|
| C2 | Mandatory beats preference | Policy-mandated notifications must override customer opt-out preferences |
| C3 | Per-customer entitlement | Notification eligibility must be evaluated per customer, never inherited |
| C4 | Decision record required | Every notification outcome must produce a decision record |
| C5 | No delivery-method dependency | Business event evaluation must not depend on any delivery channel |
| C6 | Permission-based access only | Entitlement must be resolved through permissions and roles, never by role name |

---

## Acceptance Criteria

### Positive Scenario 1 — Mandatory event always raises a notification

- Given a mandatory business event such as standing order failure for an
  eligible customer
- When the event is submitted to POST /notifications/evaluate
- Then the API returns `200` with `decision` set to raised, `decisionReason`
  identifying the mandatory policy rule, `mandatoryOverride` set to true if
  the customer had previously opted out, and a decision record persisted

### Positive Scenario 2 — Optional event raised for opted-in customer

- Given an optional event such as standing order creation and a customer who
  holds the `NOTIFICATION:READ` permission and has opted in
- When the event is submitted
- Then the API returns `200` with `decision` set to raised

### Negative Scenario 1 — Optional event for customer without NOTIFICATION:READ

- Given an optional business event and a customer without `NOTIFICATION:READ`
- When the event is submitted
- Then the API returns `422` with `field` set to `customerId`

### Negative Scenario 2 — Duplicate eventId

- Given the same `eventId` has already been processed
- When the event is submitted again
- Then the API returns `409` with `field` set to `eventId`

### Negative Scenario 3 — Unauthenticated caller

- Given an unauthenticated caller or unregistered ServiceID
- When the event is submitted
- Then the API returns `401` with an `ErrorResponse`

### Negative Scenario 4 — Missing required field

- Given a request with a missing required field
- When the event is submitted
- Then the API returns `400` with an `ErrorResponse` indicating the missing
  field

### Negative Scenario 5 — Unknown eventType

- Given an `eventType` not in the Event Classification Matrix
- When the event is submitted
- Then the API returns `422` with `field` set to `eventType`

---

# US-11 — Get Monthly Statement

## Description

Retrieves the formal monthly statement for an eligible account and a specified
closed period. The statement is the official record of account activity and
includes opening balance, closing balance, total money in, total money out, and
all transactions for the period including both SUCCESS and FAILED outcomes.
Corrections to previously issued statements are tracked through immutable
versioning. Statement assembly from upstream period-end inputs is handled by an
internal system process and is not part of this contract.

---

## HTTP Contract

### Endpoint — Get Monthly Statement

- Method: `GET`
- Path: `/accounts/{accountId}/statements/{period}`
- Query Parameters:
  - `version` (optional, integer) — specific version number. Defaults to the
    latest version if not supplied
- Request Body: None
- Expected Response Codes:
  - `200` Statement returned successfully
  - `400` Invalid `accountId` or `period` format
  - `401` Unauthenticated or caller not authorised
  - `404` Account not found or no statement exists for the specified period
  - `409` Statement period has not yet closed
  - `410` Statement is beyond the self-service retention window

### Response Fields — Get Monthly Statement (200)

- `accountId` — long, the account. Immutable
- `period` — ISO month format YYYY-MM
- `openingBalance` — BigDecimal at exactly two decimal places. Must match the
  closing balance of the prior period's issued statement
- `closingBalance` — BigDecimal at exactly two decimal places
- `totalMoneyIn` — BigDecimal at exactly two decimal places. Sum of all DEPOSIT
  and TRANSFER-in transactions with status SUCCESS
- `totalMoneyOut` — BigDecimal at exactly two decimal places. Sum of all
  WITHDRAW and TRANSFER-out transactions with status SUCCESS
- `transactions` — list of all transaction records for the period including
  both SUCCESS and FAILED outcomes. Each entry includes `transactionId`,
  `amount`, `type`, `status`, `timestamp`, `description`, and `idempotencyKey`
  where present. Never truncated
- `versionNumber` — integer, starts at 1 and increments on each correction
- `correctionSummary` — string, present on corrected versions only
- `generatedAt` — ISO-8601 UTC timestamp

---

## Business Rules

- A monthly statement is the formal record for an eligible account and period
- The customer selects a specific month when requesting a statement
- The statement must include opening balance, closing balance, total money in,
  and total money out
- The statement includes all transactions for the period including both SUCCESS
  and FAILED records
- An account with no activity in the period still produces a statement showing
  zero totals where policy requires it
- Any correction to an issued statement must create a new version. The original
  is retained and never overwritten
- The `STATEMENT:READ` permission must be re-validated at the point of delivery
- Statements beyond the defined self-service retention period are not available
  through this API
- Statement records are retained for 7 years per CRA and FINTRAC requirements

---

## Security Constraints

- The caller must be authenticated. A valid JWT Bearer token must be present
- Required permission: `STATEMENT:READ`
- CUSTOMER callers must own the account. Permission check and ownership check
  are independent and both must pass
- ADMIN callers may access statements for any account regardless of ownership
- Caller without `STATEMENT:READ` → `401`
- CUSTOMER caller accessing an account they do not own → `401`
- The `STATEMENT:READ` permission must be re-validated at the point of delivery

---

## Validation Rules

- `accountId` must be a valid long greater than `0` identifying an existing
  account
- `period` must be a valid ISO month format YYYY-MM representing a closed
  statement period
- `version` when provided must be a positive integer

---

## Error Mapping

- Invalid or empty `accountId` → `400`, `field="accountId"`
- Invalid `period` format (not YYYY-MM) → `400`, `field="period"`
- Invalid `version` format → `400`, `field="version"`
- Caller unauthenticated or lacks `STATEMENT:READ` → `401`
- CUSTOMER caller does not own the account → `401`
- Account not found → `404`, `field="accountId"`
- No statement exists for the specified period → `404`, `field="period"`
- Requested version does not exist → `404`, `field="version"`
- Period has not reached its formal cut-off point → `409`, `field="period"`
- Statement period is beyond the self-service retention window → `410`,
  `field="period"`

---

## Edge Cases

| Category | Edge Case | Expected Result | Forbidden |
|---|---|---|---|
| Timing & Cut-off | Late-arriving period-end data | Block generation until all upstream inputs confirmed. Issue a versioned correction if generated prematurely | Generate before all inputs are confirmed |
| Data Integrity | Opening balance mismatch with prior closing balance | Halt generation. Log discrepancy. Raise operational alert | Produce statement with mismatched balances |
| Access | Permission revoked between request and retrieval | Re-validate permission at point of delivery. Return authorisation error if no longer held | Return statement content when permission is no longer held |
| Volume & Retention | Statement beyond retention window | Return specific error directing customer to contact support | Return generic 404 or empty response |
| Auditability | Correction without traceable version history | Retain original. Assign incremented version number. Record change summary | Overwrite original without retaining it |

---

## Constraints

| # | Constraint | Rule |
|---|---|---|
| C1 | Complete inputs before generation | Statement must not be generated until all upstream period-end inputs are confirmed |
| C3 | Point-of-delivery permission check | `STATEMENT:READ` must be re-validated at delivery not only at request time |
| C4 | Immutable originals | Corrections must create a new version. The original must not be overwritten |
| C5 | Retention boundary enforced | Requests beyond the retention window must return a specific error |
| C6 | Permission-based access only | Statement access must be resolved through permissions and roles |

---

## Acceptance Criteria

### Positive Scenario 1 — Statement returned for a closed period

- Given an eligible account with a closed period and a CUSTOMER caller with
  `STATEMENT:READ` who owns the account
- When GET /accounts/{accountId}/statements/2026-03 is submitted
- Then the API returns `200` with `openingBalance`, `closingBalance`,
  `totalMoneyIn`, `totalMoneyOut` at exactly two decimal places, all
  transactions including SUCCESS and FAILED in `transactions`, `versionNumber`
  of `1`, and `generatedAt` timestamp present

### Positive Scenario 2 — Corrected statement returned

- Given a corrected statement at version 2
- When the request is submitted
- Then the API returns `200` with `versionNumber` of `2` and
  `correctionSummary` listing every field that changed

### Positive Scenario 3 — Empty month returns zero totals

- Given an eligible account with no activity in the selected month
- When the request is submitted
- Then the API returns `200` with `totalMoneyIn` of `0.00`, `totalMoneyOut`
  of `0.00`, and `transactions` as an empty list

### Negative Scenario 1 — Caller lacks STATEMENT:READ

- Given a caller without `STATEMENT:READ`
- When the request is submitted
- Then the API returns `401` with an `ErrorResponse`

### Negative Scenario 2 — Account not found

- Given an account that does not exist
- When the request is submitted
- Then the API returns `404` with `field` set to `accountId`

### Negative Scenario 3 — Period not yet closed

- Given a period that has not reached its cut-off point
- When the request is submitted
- Then the API returns `409` with `field` set to `period`

### Negative Scenario 4 — Period beyond retention window

- Given a statement older than the self-service retention period
- When the request is submitted
- Then the API returns `410` with `field` set to `period` and `message`
  directing the customer to contact their branch or support team

### Negative Scenario 5 — Invalid period format

- Given an invalid period format
- When the request is submitted
- Then the API returns `400` with `field` set to `period`

---

# US-12 — Spending Insights

## Description

Returns a categorised breakdown of debit spending for a customer's eligible
ACTIVE account covering a selected calendar month and a six-month spending
trend. Spending is derived from WITHDRAW and TRANSFER-out transactions with
status SUCCESS only. DEPOSIT transactions are excluded from all spending totals.
Every eligible transaction is mapped to exactly one of the eight agreed spending
categories. The system returns data in a format ready for a Donut Chart showing
category name and percentage, and a Bar Chart showing total debit spend across
the last six calendar months. If a customer manually changes a transaction
category the insight view must recalculate and update in real time. Insights
can only be generated for the authenticated user's own accounts.

---

## Actors and Roles

| Permission | CUSTOMER | ADMIN |
|---|---|---|
| `INSIGHTS:READ` | Own accounts only | All accounts |

---

## Domain Objects

### SpendingCategory

One entry per category in the insight response. Always eight entries.

- `category` — string, one of the eight agreed values: Housing, Transport,
  Food & Drink, Entertainment, Shopping, Utilities, Health, or Income
- `totalAmount` — BigDecimal at exactly two decimal places. Sum of all eligible
  SUCCESS WITHDRAW and TRANSFER-out transactions in this category for the
  selected month. Zero when no transactions belong to this category
- `percentage` — BigDecimal at exactly two decimal places. This category's
  share of `totalDebitSpend`. All eight percentages must sum to exactly 100

### SixMonthTrendEntry

One entry per calendar month. Always exactly six entries.

- `year` — integer, four-digit calendar year
- `month` — integer between 1 and 12
- `totalDebitSpend` — BigDecimal at exactly two decimal places. Zero where no
  eligible activity exists
- `isComplete` — boolean. False when this is the current in-progress month
- `accountExisted` — boolean. False when the account did not yet exist during
  this month

---

## HTTP Contract

### Endpoint 1 — Get Spending Insights

- Method: `GET`
- Path: `/accounts/{accountId}/insights`
- Query Parameters:
  - `year` (required) — four-digit calendar year
  - `month` (required) — calendar month number 1 to 12
- Request Body: None
- Expected Response Codes:
  - `200` Insights returned successfully
  - `400` Invalid `accountId`, `year`, or `month` format
  - `401` Unauthenticated or caller not authorised
  - `404` Account not found
  - `409` Requested month has not yet started

### Endpoint 2 — Recategorise a Transaction

- Method: `PUT`
- Path: `/accounts/{accountId}/transactions/{transactionId}/category`
- Request Body: `category` (string, required) — must be one of the eight
  agreed category values, case-sensitive
- Expected Response Codes:
  - `200` Category updated and insight recalculated successfully
  - `400` Invalid `accountId`, `transactionId`, or missing request body
  - `401` Unauthenticated or caller not authorised
  - `404` Account or transaction not found
  - `422` Category value is not one of the eight agreed categories

### Response Fields — Get Spending Insights (200)

- `accountId` — long, the account
- `period` — selected year, month, and whether the month is complete or in
  progress
- `totalDebitSpend` — sum of all eligible SUCCESS WITHDRAW and TRANSFER-out
  transactions at exactly two decimal places
- `transactionCount` — number of eligible SUCCESS transactions included
- `hasUncategorised` — true when one or more transactions could not be
  automatically mapped
- `hasExcludedDisputes` — true when one or more transactions were excluded due
  to dispute
- `dataFresh` — false when upstream transaction data is delayed or incomplete
- `categoryBreakdown` — all eight categories with total amount and percentage.
  All eight always present. Percentages sum to exactly 100
- `topTransactions` — the five largest individual eligible transactions by
  amount, each including `transactionId` (long), `amount`, `type`, and
  `description`
- `sixMonthTrend` — exactly six entries one per calendar month. All six always
  present

### Response Fields — Recategorise a Transaction (200)

- `transactionId` — long, the transaction that was recategorised
- `previousCategory` — the category before the change
- `updatedCategory` — the category after the change
- `updatedCategoryBreakdown` — all eight categories with recalculated totals
  and percentages so the donut chart can update immediately
- `updatedTotalDebitSpend` — recalculated total debit spend for the month

---

## Business Rules

- Spending insights are derived from WITHDRAW and TRANSFER-out transactions
  with status SUCCESS only. DEPOSIT transactions are excluded regardless of
  their assigned category
- Every eligible transaction must be mapped to exactly one of the eight agreed
  categories: Housing, Transport, Food & Drink, Entertainment, Shopping,
  Utilities, Health, or Income. No transaction may be uncategorised or belong
  to more than one category
- Data is grouped by category and amounts are summed for the requested calendar
  month. No averaging or estimation is permitted
- All eight categories must always be returned. Category percentages must
  always sum to exactly 100
- The six-month trend must always contain exactly six entries covering the last
  six calendar months. All six must be returned even where spend was zero
- If a customer manually changes a transaction category the insight must
  recalculate immediately. Updated totals and percentages must be returned in
  the response
- Disputed transactions must not be included in spending totals until resolved
- FAILED transactions are excluded from spending totals. Only SUCCESS outcomes
  contribute
- Spending insights must never be presented as financial advice
- Spending insights must never be positioned as a formal account record. The
  monthly statement is the formal record
- The eight categories are fixed for this release
- Spending insight records are retained for 7 years per CRA and FINTRAC
  requirements

---

## Security Constraints

- The caller must be authenticated. A valid JWT Bearer token must be present
- Required permission: `INSIGHTS:READ`
- CUSTOMER callers must own the account. Permission check and ownership check
  are independent and both must pass
- ADMIN callers may access insights for any account regardless of ownership
- CUSTOMER caller accessing an account they do not own → `401`
- Caller without `INSIGHTS:READ` → `401`

---

## Validation Rules

- `accountId` must be a valid long greater than `0`
- `year` must be a valid four-digit calendar year
- `month` must be a whole number between `1` and `12` inclusive
- The combination of `year` and `month` must not refer to a month that has not
  yet started. A month currently in progress is permitted and returns a partial
  insight with the period marked as incomplete
- `transactionId` must be a valid long greater than `0`
- `category` must exactly match one of the eight agreed values. Case-sensitive
- All monetary amounts in the response must use exactly two decimal places

---

## Error Mapping

- Invalid `accountId` format → `400`, `field="accountId"`
- Invalid or missing `year` → `400`, `field="year"`
- Invalid or missing `month` → `400`, `field="month"`
- Invalid `transactionId` format → `400`, `field="transactionId"`
- Missing request body on recategorisation → `400`
- Account not found → `404`
- Transaction not found → `404`
- Unrecognised `category` value → `422`, `field="category"`
- Caller unauthenticated or lacks `INSIGHTS:READ` → `401`
- CUSTOMER caller does not own the account → `401`
- Requested month has not yet started → `409`

---

## Edge Cases

- No eligible transactions in the selected month → all eight categories
  returned with zero values and `totalDebitSpend` of `0.00`. Not an error
- All eligible transactions disputed or excluded → return zero totals and set
  `hasExcludedDisputes` to true
- Account did not exist for some trend months → those months appear with zero
  spend and `accountExisted` set to false
- Selected month still in progress → period marked as incomplete
- DEPOSIT transaction manually recategorised → remains excluded from spending
  totals. Exclusion is based on transaction type not category
- FAILED transaction manually recategorised → remains excluded from spending
  totals. Exclusion is based on transaction status not category
- Two transactions share the same amount and type → treated as separate and
  both counted individually
- Second recategorisation for the same transaction → overwrites the previous
  category. Only the most recently confirmed category applies
- Upstream transaction data delayed → `dataFresh` set to false
- Single category accounts for 90 percent or more of spend → all eight
  categories still returned

---

## Constraints

1. Spending insights must only use WITHDRAW and TRANSFER-out transactions with
   status SUCCESS. DEPOSIT and FAILED transactions must never contribute to
   spending totals
2. Every eligible transaction must be mapped to exactly one of the eight agreed
   categories
3. Donut chart percentages across all eight categories must always sum to
   exactly 100
4. The bar chart must always cover exactly the last six calendar months
5. Insights must only be generated for the authenticated customer's own
   accounts. Both `INSIGHTS:READ` permission and ownership check must pass
6. If a customer changes a transaction category the charts must update in real
   time. Stale data must not persist after a confirmed category change
7. Aggregation must sum the full amount of eligible transactions per category
   for the requested calendar month. No averaging or estimation
8. Disputed transactions must not appear in insights until resolved
9. The system must not invent or estimate category mappings. Uncertain
   transactions must be flagged as uncategorised
10. Insights must not be presented as financial advice or as a formal account
    record under any circumstances

---

## What the Customer View Requires from the Back End

For the donut chart:
- All eight category totals and percentages. All eight must be present even
  where zero
- A signal indicating whether the month is complete or in progress
- A flag when one or more transactions could not be automatically categorised
- A flag when disputed or excluded transactions exist in the period

For the bar chart:
- Total eligible spend per calendar month for the last six months. All six
  must be present even where zero
- A signal for months where the account did not yet exist
- A signal for the current in-progress month

For category management:
- For each eligible transaction: `transactionId` (long), current assigned
  category, and the full list of eight available categories
- After a category change: updated category totals and percentages immediately
- A pending signal when a recategorisation is still processing

For general display:
- Count of eligible transactions included
- The five largest individual eligible transactions including `transactionId`
  (long), `amount`, `type`, and `description`
- A data freshness indicator when upstream data is delayed
- A clear message when there are no eligible transactions

---

## Acceptance Criteria

### Positive Scenario 1 — Insights returned for a month with eligible activity

- Given an existing ACTIVE account with WITHDRAW or TRANSFER-out transactions
  with status SUCCESS and a CUSTOMER caller with `INSIGHTS:READ` who owns the
  account
- When a GET request is submitted with valid `accountId`, `year`, and `month`
- Then the API returns `200` with all eight categories in the breakdown,
  percentages summing to `100`, six entries in the six-month trend, and the
  top five eligible transactions

### Positive Scenario 2 — Empty month returns zero totals not an error

- Given an existing ACTIVE account with no eligible transactions in the
  selected month and a CUSTOMER caller with `INSIGHTS:READ`
- When a GET request is submitted for that month
- Then the API returns `200` with all eight categories showing zero values and
  `totalDebitSpend` of `0.00`

### Positive Scenario 3 — Real-time recategorisation updates the donut chart

- Given an existing WITHDRAW transaction assigned to Shopping and a CUSTOMER
  caller with `INSIGHTS:READ`
- When a PUT request is submitted with `category="Food & Drink"`
- Then the API returns `200` with the Shopping total decreased, the
  Food & Drink total increased, all percentages still summing to `100`, and
  the updated category breakdown returned immediately

### Positive Scenario 4 — Six-month trend always returns all six months

- Given an account with eligible activity in some but not all of the last six
  months and a CUSTOMER caller with `INSIGHTS:READ`
- When a GET request is submitted for the current month
- Then the six-month trend contains exactly six entries with zero spend for
  inactive months

### Negative Scenario 1 — Caller lacks INSIGHTS:READ permission

- Given a caller without `INSIGHTS:READ`
- When a GET request is submitted
- Then the API returns `401` with an `ErrorResponse` where `code` identifies
  the permission failure

### Negative Scenario 2 — CUSTOMER caller does not own the account

- Given a CUSTOMER caller and an account that does not belong to them
- When a GET request is submitted
- Then the API returns `401` with an `ErrorResponse` where `code` identifies
  the ownership failure

### Negative Scenario 3 — Account not found

- Given no account exists for the supplied `accountId`
- When a GET request is submitted
- Then the API returns `404` with an `ErrorResponse`

### Negative Scenario 4 — Unrecognised category in recategorisation

- Given an existing transaction
- When a PUT request is submitted with `category="Groceries"`
- Then the API returns `422` with `code` identifying the validation failure,
  `message` listing the eight accepted values, and `field` set to `category`

### Negative Scenario 5 — Future month requested

- Given a valid account
- When a GET request is submitted for a month that has not yet started
- Then the API returns `409` with an `ErrorResponse`

### Negative Scenario 6 — All transactions excluded

- Given an account where all eligible transactions in the selected month are
  under dispute or are FAILED
- When a GET request is submitted
- Then the API returns `200` with zero totals across all eight categories and
  `hasExcludedDisputes` set to true

### Negative Scenario 7 — Invalid accountId format

- Given a non-numeric value as `accountId`
- When a GET request is submitted
- Then the API returns `400` with `code` identifying the format error and
  `field` set to `accountId`
