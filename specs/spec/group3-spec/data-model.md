# Data Model: Group 3 — Digital Banking Platform

**Phase**: 1 — Design & Contracts  
**Feature**: Group 3 Stories (US-01 through US-05)  
**Date**: 2026-04-08

---

## Overview

Group 3 introduces five new persisted entities and one derived (non-persisted) model. Group 2 entities (`User`, `Role`, `Customer`, `Account`, `Transaction`) are extended as described below.

---

## Group 2 Entity Extensions

### Transaction.status — PENDING added (GAP-1 resolved)

Group 2's `TransactionStatus` enum gains a third value:

| Value | Meaning |
|---|---|
| `PENDING` | Transaction initiated but not yet settled |
| `SUCCESS` | Transaction completed successfully |
| `FAILED` | Transaction failed |

This is an **additive** change. All existing `SUCCESS`/`FAILED` values remain unchanged. A Flyway/Liquibase migration is required if the column uses a database-level ENUM type. Transactions are ordered by `timestamp ASC` across the full result set; no status-based grouping is applied.

### Account.daily_transfer_limit — new column (GAP-2 resolved)

Group 2's `AccountEntity` and `accounts` table gain one nullable column:

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `daily_transfer_limit` | `DECIMAL(19,2)` | NOT NULL DEFAULT 3000.00 | Maximum aggregate daily outgoing transfer amount in CAD |

This field is read by `StandingOrderService` to validate that a new standing order `amount` does not exceed the account's daily limit. Default is CAD 3,000.00. Requires Group 2 migration coordination.

---

## New Entities

### 1. StandingOrder

**Table**: `standing_orders`  
**Persisted**: Yes

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `standing_order_id` | `VARCHAR(36)` PK | NOT NULL | UUID, system-generated |
| `source_account_id` | `BIGINT` FK → `accounts.account_id` | NOT NULL | Account funds are drawn from |
| `payee_account` | `VARCHAR(34)` | NOT NULL | Modulo 97 validated |
| `payee_name` | `VARCHAR(70)` | NOT NULL | 1–70 chars |
| `amount` | `DECIMAL(19,2)` | NOT NULL, > 0 | Fixed payment amount |
| `frequency` | `VARCHAR(20)` | NOT NULL | DAILY, WEEKLY, MONTHLY, QUARTERLY |
| `start_date` | `DATETIME` | NOT NULL | UTC; must be ≥ 24 hours from creation |
| `end_date` | `DATETIME` | NULL | Optional termination date |
| `reference` | `VARCHAR(18)` | NOT NULL | 1–18 alphanumeric |
| `status` | `VARCHAR(30)` | NOT NULL | ACTIVE, CANCELLED, LOCKED, TERMINATED |
| `next_run_date` | `DATETIME` | NOT NULL | Calculated, holiday-shifted |
| `created_at` | `DATETIME` | NOT NULL | System-managed |
| `updated_at` | `DATETIME` | NOT NULL | System-managed |

**Indexes**:
- `idx_so_source_account_id` on `source_account_id`
- `idx_so_status` on `status`
- `idx_so_next_run_date` on `next_run_date` (used by scheduler)
- Unique index on `(source_account_id, payee_account, amount, frequency, status)` where `status = 'ACTIVE'` — enforced in service layer for duplicate detection (partial index not portable across MySQL/H2; use query-level check)

**Relationships**:
- Many `StandingOrder` → One `Account` (source_account_id)

**State Transitions**:
```
ACTIVE → CANCELLED   (customer cancel, > 24h before nextRunDate)
ACTIVE → LOCKED      (system lock, within 24h of nextRunDate)
ACTIVE → TERMINATED  (endDate passed without explicit cancel)
ACTIVE → FAILED_INSUFFICIENT_FUNDS  (transient state during retry, reverts to ACTIVE or TERMINATED)
LOCKED → ACTIVE      (after nextRunDate passes)
```

---

### 2. NotificationDecision

**Table**: `notification_decisions`  
**Persisted**: Yes

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `event_id` | `VARCHAR(36)` PK | NOT NULL | UUID from caller, used for deduplication |
| `event_type` | `VARCHAR(100)` | NOT NULL | Must match Event Classification Matrix |
| `account_id` | `BIGINT` | NOT NULL | Account the event relates to |
| `customer_id` | `BIGINT` | NOT NULL | Customer linked to the account |
| `business_timestamp` | `DATETIME` | NOT NULL | ISO-8601 UTC from caller |
| `payload` | `TEXT` | NULL | JSON object, serialised as string |
| `decision` | `VARCHAR(20)` | NOT NULL | RAISED, GROUPED, SUPPRESSED |
| `decision_reason` | `VARCHAR(500)` | NOT NULL | Business rule that produced the decision |
| `mandatory_override` | `BOOLEAN` | NOT NULL, DEFAULT false | True when mandatory event overrode opt-out |
| `evaluated_at` | `DATETIME` | NOT NULL | System-managed timestamp |

**Indexes**:
- `idx_nd_account_id` on `account_id`
- `idx_nd_customer_id` on `customer_id`
- `idx_nd_event_type` on `event_type`

**Relationships**:
- References `accounts.account_id` and `customers.customer_id` (foreign keys for referential integrity; no JPA `@ManyToOne` navigation needed since NotificationDecision is append-only)

---

### 3. MonthlyStatement

**Table**: `monthly_statements`  
**Persisted**: Yes (append-only, versioned)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `statement_id` | `BIGINT` PK AUTO_INCREMENT | NOT NULL | Surrogate PK |
| `account_id` | `BIGINT` FK → `accounts.account_id` | NOT NULL | Immutable |
| `period` | `VARCHAR(7)` | NOT NULL | YYYY-MM format |
| `opening_balance` | `DECIMAL(19,2)` | NOT NULL | Must match prior period closing balance |
| `closing_balance` | `DECIMAL(19,2)` | NOT NULL | |
| `total_money_in` | `DECIMAL(19,2)` | NOT NULL | Sum of DEPOSIT+TRANSFER-in SUCCESS |
| `total_money_out` | `DECIMAL(19,2)` | NOT NULL | Sum of WITHDRAW+TRANSFER-out SUCCESS |
| `transactions_json` | `LONGTEXT` | NOT NULL | JSON array of all period transactions |
| `version_number` | `INT` | NOT NULL, DEFAULT 1 | Starts at 1, increments on correction |
| `correction_summary` | `VARCHAR(2000)` | NULL | Present on corrected versions only |
| `generated_at` | `DATETIME` | NOT NULL | System-managed |

**Indexes**:
- Unique constraint on `(account_id, period, version_number)`
- `idx_ms_account_period` on `(account_id, period)` for latest-version lookups

**Relationships**:
- Many `MonthlyStatement` → One `Account`

**Notes**:
- `transactions_json` stores the serialised transaction list for the period so the statement record is self-contained and immutable even if transaction data changes upstream
- Originals are never updated. Corrections are new rows with incremented `version_number`

---

### 4. ExportCache (Idempotent PDF Export)

**Table**: `export_cache`  
**Persisted**: Yes (purged after 72 hours)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `cache_id` | `BIGINT` PK AUTO_INCREMENT | NOT NULL | Surrogate PK |
| `account_id` | `BIGINT` | NOT NULL | From request |
| `param_hash` | `VARCHAR(64)` | NOT NULL | SHA-256 of accountId+effectiveStartDate+effectiveEndDate |
| `pdf_data` | `LONGBLOB` | NOT NULL | Raw PDF bytes |
| `created_at` | `DATETIME` | NOT NULL | Used for TTL purge |

**Indexes**:
- Unique constraint on `(account_id, param_hash)`

---

### 5. IdempotencyRecord

**Table**: `idempotency_records`  
**Persisted**: Yes (purged after 24–72 hours)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `idempotency_key` | `VARCHAR(255)` PK | NOT NULL | Caller-supplied key |
| `response_body` | `TEXT` | NOT NULL | Serialised JSON response |
| `status_code` | `INT` | NOT NULL | HTTP status of original response |
| `created_at` | `DATETIME` | NOT NULL | For TTL purge |

---

## Transaction Entity Extension

Group 3 adds one column to the existing `transactions` table:

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `category` | `VARCHAR(50)` | NULL | Spending category, set by auto-categoriser or manual override |

The `category` column is the **only** Group 3-owned field that may be updated on a `Transaction` after creation. All other transaction fields remain immutable.

---

### 6. NotificationPreference (GAP-3 resolved)

**Table**: `notification_preferences`  
**Persisted**: Yes

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `preference_id` | `BIGINT` PK AUTO_INCREMENT | NOT NULL | Surrogate PK |
| `customer_id` | `BIGINT` FK → `customers.customer_id` | NOT NULL | Customer whose preference this is |
| `event_type` | `VARCHAR(100)` | NOT NULL | Matches Event Classification Matrix values |
| `opted_in` | `BOOLEAN` | NOT NULL DEFAULT true | `true` = receive notification; `false` = suppressed |
| `updated_at` | `DATETIME` | NOT NULL | System-managed |

**Indexes**:
- Unique constraint on `(customer_id, event_type)` — one preference record per customer per event type
- `idx_np_customer_id` on `customer_id`

**Relationships**:
- Many `NotificationPreference` → One `Customer`

**Behaviour**:
- If no record exists for `(customer_id, eventType)`, the customer is treated as **opted in** (default-open model)
- Mandatory events (`StandingOrderFailure`, `UnusualAccountActivity`) always produce a `RAISED` decision regardless of preference; `mandatoryOverride = true` is set when `opted_in = false`
- Optional events (`StatementAvailability`, `StandingOrderCreation`) produce `SUPPRESSED` when `opted_in = false`

---

### CategoryResolver Keyword Configuration (GAP-4 resolved)

Keywords are defined in `application.yml` under `banking.categories`. `CategoryResolver.resolve(String description)` performs **case-insensitive substring matching** against the description field. First match wins. Returns `null` (= uncategorised) if no keyword matches.

```yaml
banking:
  categories:
    Housing:        [rent, mortgage, lease, landlord, condo, property, housing]
    Transport:      [uber, lyft, taxi, transit, fuel, gas, parking, via rail, greyhound, air canada, westjet]
    "Food & Drink": [restaurant, cafe, coffee, starbucks, tim hortons, mcdonalds, grocery, loblaws, sobeys, metro, pizza, sushi, bar, pub]
    Entertainment:  [netflix, spotify, disney, cinema, movie, theatre, concert, ticket, steam, playstation, xbox]
    Shopping:       [amazon, ebay, walmart, zara, h&m, clothing, shoes, mall, online shop]
    Utilities:      [hydro, electricity, internet, rogers, bell, telus, shaw, heating, utility]
    Health:         [pharmacy, medical, doctor, dentist, hospital, clinic, gym, fitness, yoga, prescription]
    Income:         [salary, payroll, direct deposit, refund, cashback, interest, dividend]
```

Keywords are additive config — implementers may extend the list in `application-local.yml` without touching production config.

---

## Derived Model: SpendingInsight

**Persisted**: No — calculated on-request from `transactions`

Built by `SpendingInsightService` from:
- `transactions` WHERE `account_id = ?` AND `transaction_date` BETWEEN month start/end AND `type` IN (WITHDRAW, TRANSFER) AND `status` = SUCCESS

**Fields** (matches spec, see group3-spec.md US-12):
- `accountId`, `period`, `totalDebitSpend`, `transactionCount`, `hasUncategorised`, `hasExcludedDisputes`, `dataFresh`, `categoryBreakdown` (8 entries), `topTransactions` (5 entries), `sixMonthTrend` (6 entries)

---

## Audit Log

**Table**: `audit_log`  
**Persisted**: Yes (append-only, 7-year retention)

| Column | Type | Notes |
|---|---|---|
| `audit_id` | `BIGINT` PK AUTO_INCREMENT | |
| `actor_id` | `BIGINT` | userId or serviceId |
| `actor_role` | `VARCHAR(50)` | Role at time of action |
| `action` | `VARCHAR(100)` | e.g., GET_TRANSACTIONS, CREATE_STANDING_ORDER |
| `resource_type` | `VARCHAR(50)` | e.g., Account, StandingOrder |
| `resource_id` | `VARCHAR(100)` | Resource identifier |
| `timestamp` | `DATETIME` | UTC |
| `outcome` | `VARCHAR(20)` | SUCCESS, FAILURE |
| `detail` | `TEXT` | Optional additional context |

---

## Entity Relationship Summary

```
Account (Group 2)
  ├── Transaction (Group 2) [1:N]
  │     └── category (added by Group 3)
  ├── StandingOrder (Group 3) [1:N]
  ├── MonthlyStatement (Group 3) [1:N]  ← versioned
  └── ExportCache (Group 3) [1:N]       ← ephemeral

NotificationDecision (Group 3)          ← references Account, Customer; no JPA nav
NotificationPreference (Group 3)        ← references Customer; controls optional event routing
IdempotencyRecord (Group 3)             ← standalone table
AuditLog (Group 3)                      ← append-only
```
