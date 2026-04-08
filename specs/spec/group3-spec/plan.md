# Implementation Plan: Group 3 — Digital Banking Platform

**Branch**: `spec/group3-spec` | **Date**: 2026-04-08 | **Spec**: [group3-spec.md](../../group3-spec.md)  
**Input**: [specs/group3-spec.md](../../group3-spec.md)

## Summary

Group 3 implements five stories on top of the Digital Banking Platform shared domain (User, Role, Customer, Account, Transaction defined by Group 2). The stories deliver: transaction history retrieval with PDF export (US-01), standing order lifecycle management with a background scheduler (US-02), notification event evaluation with mandatory/optional classification (US-03), versioned monthly statement retrieval (US-04), and categorised spending insights with real-time recategorisation (US-05).

The technical approach is a Spring Boot 3.x REST API in Java 21 backed by MySQL (H2 for local/test), paired with a React 18 + Vite frontend using React Query v5.

---

## Technical Context

**Language/Version**: Java 21 (backend), JavaScript ES2022 (frontend)  
**Primary Dependencies**: Spring Boot 3.x (Web, Data JPA, Security, Validation), iText 7 Community (PDF), React 18, React Query v5, Axios, Vite  
**Storage**: MySQL 8 (production), H2 (local + test)  
**Testing**: JUnit 5, Mockito (backend), Jest, React Testing Library (frontend)  
**Target Platform**: Web service (REST API) + Single-page application  
**Project Type**: Web application (backend API + frontend SPA)  
**Performance Goals**: Standard REST response times; PDF export idempotency required  
**Constraints**: 7-year data retention (CRA/FINTRAC); transaction immutability; statement original preservation; permission + ownership checks both enforced  
**Scale/Scope**: Retail banking — accounts, transactions, standing orders, statements, insights for CUSTOMER and ADMIN roles

---

## Constitution Check

*GATE: Checked before Phase 0; re-checked after Phase 1 design.*

- [x] **Single Source of Truth** — `specs/group3-spec.md` is the authoritative contract. This plan derives from it without adding fields or operations not present in the spec.
- [x] **BA-First Language** — Spec reviewed and accepted. Plan moves to technical design in compliance with that baseline.
- [x] **Multi-Discipline Input** — Spec authored by team (Suraj, Srikrishna, Emad); plan aligns with both backend and frontend requirements.
- [x] **Gate Completion** — All 5 stories in the spec have complete HTTP contracts, business rules, error mappings, acceptance criteria, and security constraints.
- [x] **Privileges Over Roles** — Permission checks (`TRANSACTION:READ`, `SO:CREATE`, etc.) are enforced independently of role names. Ownership checks are independent from permission checks.
- [x] **No Spec Mutations** — No new fields or endpoints have been introduced during planning. `category` column on `Transaction` is the only additive change, and it is explicitly specified in US-05.

**Post-Phase 1 re-check**: All design artifacts (data-model.md, contracts/, quickstart.md) are consistent with the spec. No violations identified. Plan proceeds.

---

## Project Structure

### Documentation (this feature)

```text
specs/spec/group3-spec/
├── plan.md              # This file
├── research.md          # Phase 0 — decisions on holidays, Modulo 97, PDF, mTLS, categorisation
├── data-model.md        # Phase 1 — entity definitions and relationships
├── quickstart.md        # Phase 1 — local dev and test setup
├── contracts/
│   ├── us-01-transaction-history.md
│   ├── us-02-standing-orders.md
│   ├── us-03-notification-evaluate.md
│   ├── us-04-monthly-statement.md
│   └── us-05-spending-insights.md
└── tasks.md             # Phase 2 output — created by /speckit.tasks, not this command
```

### Source Code (repository root)

```text
backend/
├── src/
│   └── main/
│       └── java/com/fdm/banking/
│           ├── config/
│           │   ├── SecurityConfig.java
│           │   └── SchedulerConfig.java
│           ├── controller/
│           │   ├── TransactionController.java        # US-01
│           │   ├── StandingOrderController.java      # US-02
│           │   ├── NotificationController.java       # US-03
│           │   ├── StatementController.java          # US-04
│           │   └── InsightController.java            # US-05
│           ├── service/
│           │   ├── TransactionHistoryService.java    # US-01
│           │   ├── PdfStatementService.java          # US-01 export
│           │   ├── StandingOrderService.java         # US-02
│           │   ├── StandingOrderScheduler.java       # US-02 background
│           │   ├── NotificationEvaluationService.java# US-03
│           │   ├── MonthlyStatementService.java      # US-04
│           │   └── SpendingInsightService.java       # US-05
│           ├── repository/
│           │   ├── StandingOrderRepository.java
│           │   ├── NotificationDecisionRepository.java
│           │   ├── MonthlyStatementRepository.java
│           │   ├── ExportCacheRepository.java
│           │   └── IdempotencyRecordRepository.java
│           ├── entity/
│           │   ├── StandingOrderEntity.java
│           │   ├── NotificationDecisionEntity.java
│           │   ├── MonthlyStatementEntity.java
│           │   ├── ExportCacheEntity.java
│           │   └── IdempotencyRecordEntity.java
│           ├── dto/
│           │   ├── request/
│           │   │   ├── CreateStandingOrderRequest.java
│           │   │   ├── NotificationEventRequest.java
│           │   │   └── RecategoriseRequest.java
│           │   └── response/
│           │       ├── TransactionHistoryResponse.java
│           │       ├── StandingOrderResponse.java
│           │       ├── StandingOrderListResponse.java
│           │       ├── CancelStandingOrderResponse.java
│           │       ├── NotificationDecisionResponse.java
│           │       ├── MonthlyStatementResponse.java
│           │       ├── SpendingInsightResponse.java
│           │       ├── RecategoriseResponse.java
│           │       └── ErrorResponse.java
│           ├── mapper/
│           │   ├── StandingOrderMapper.java
│           │   ├── NotificationDecisionMapper.java
│           │   └── MonthlyStatementMapper.java
│           ├── security/
│           │   ├── JwtAuthenticationFilter.java      # Reuse from Group 2
│           │   ├── ServiceAuthenticationProvider.java# US-03 mTLS/API Key
│           │   └── OwnershipValidator.java
│           ├── scheduler/
│           │   └── StandingOrderExecutionJob.java
│           └── util/
│               ├── Mod97Validator.java
│               ├── CanadianHolidayService.java
│               └── CategoryResolver.java
│   └── resources/
│       ├── application.yml
│       ├── application-local.yml
│       └── application-test.yml
└── src/
    └── test/
        └── java/com/fdm/banking/
            ├── controller/          # MockMvc integration tests
            ├── service/             # JUnit 5 unit tests
            └── util/               # Mod97Validator, CategoryResolver tests

frontend/
├── src/
│   ├── api/
│   │   ├── transactionApi.js
│   │   ├── standingOrderApi.js
│   │   ├── statementApi.js
│   │   └── insightApi.js
│   ├── hooks/
│   │   ├── useTransactionHistory.js
│   │   ├── useTransactionExport.js
│   │   ├── useStandingOrders.js
│   │   ├── useCreateStandingOrder.js
│   │   ├── useCancelStandingOrder.js
│   │   ├── useMonthlyStatement.js
│   │   ├── useSpendingInsights.js
│   │   └── useRecategorise.js
│   ├── components/
│   │   ├── transactions/
│   │   │   ├── TransactionHistoryPage.jsx
│   │   │   ├── TransactionList.jsx
│   │   │   ├── TransactionItem.jsx
│   │   │   └── ExportButton.jsx
│   │   ├── standingorders/
│   │   │   ├── StandingOrdersPage.jsx
│   │   │   ├── StandingOrderList.jsx
│   │   │   ├── StandingOrderItem.jsx
│   │   │   └── CreateStandingOrderForm.jsx
│   │   ├── statements/
│   │   │   ├── MonthlyStatementPage.jsx
│   │   │   └── StatementViewer.jsx
│   │   ├── insights/
│   │   │   ├── SpendingInsightsPage.jsx
│   │   │   ├── CategoryDonutChart.jsx
│   │   │   ├── SixMonthBarChart.jsx
│   │   │   └── TransactionCategoryRow.jsx
│   │   └── shared/
│   │       ├── ErrorMessage.jsx
│   │       └── LoadingSpinner.jsx
│   └── tests/
│       ├── components/
│       └── hooks/
└── vite.config.js
```

**Structure Decision**: Web application (Option 2 from template). Backend is a Spring Boot REST API in `backend/`. Frontend is a React 18 SPA in `frontend/`. Both live at the repository root. Group 2 entity classes and security infrastructure are imported by package reference, not copied.

---

## Complexity Tracking

> No constitution violations. No justification required.

---

## 1. Backend Project Structure

Package root: `com.fdm.banking`

| Package | Contents |
|---|---|
| `controller` | One `@RestController` per story |
| `service` | One `@Service` per story plus scheduler |
| `repository` | Spring Data JPA interfaces for Group 3 entities only |
| `entity` | `@Entity` classes for Group 3 persisted tables |
| `dto.request` | Request body POJOs with Bean Validation annotations |
| `dto.response` | Response body POJOs |
| `mapper` | Manual mapper classes (entity ↔ DTO); no MapStruct |
| `security` | `ServiceAuthenticationProvider`, `OwnershipValidator` |
| `scheduler` | `StandingOrderExecutionJob` |
| `util` | `Mod97Validator`, `CanadianHolidayService`, `CategoryResolver` |
| `config` | `SecurityConfig`, `SchedulerConfig` |

Group 2 packages (`com.fdm.banking.entity.User`, etc.) are referenced by import, not redefined.

---

## 2. Entity and JPA Mapping

### StandingOrderEntity

```java
@Entity
@Table(name = "standing_orders")
public class StandingOrderEntity {
    @Id
    private String standingOrderId;          // UUID, set at creation
    @Column(nullable = false)
    private Long sourceAccountId;
    @Column(nullable = false, length = 34)
    private String payeeAccount;
    @Column(nullable = false, length = 70)
    private String payeeName;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = true)
    private LocalDateTime endDate;
    @Column(nullable = false, length = 18)
    private String reference;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StandingOrderStatus status;
    @Column(nullable = false)
    private LocalDateTime nextRunDate;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
```

**Enums** (`Frequency`, `StandingOrderStatus`) defined as Java enums in the `entity` package.

---

### NotificationDecisionEntity

```java
@Entity
@Table(name = "notification_decisions")
public class NotificationDecisionEntity {
    @Id
    private String eventId;                  // UUID from caller
    @Column(nullable = false, length = 100)
    private String eventType;
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private LocalDateTime businessTimestamp;
    @Column(columnDefinition = "TEXT")
    private String payload;                  // Serialised JSON string
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationDecision decision;
    @Column(nullable = false, length = 500)
    private String decisionReason;
    @Column(nullable = false)
    private boolean mandatoryOverride;
    @Column(nullable = false)
    private LocalDateTime evaluatedAt;
}
```

**Enum** `NotificationDecision`: `RAISED`, `GROUPED`, `SUPPRESSED`

---

### MonthlyStatementEntity

```java
@Entity
@Table(
    name = "monthly_statements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"account_id","period","version_number"})
)
public class MonthlyStatementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false, length = 7)
    private String period;                   // YYYY-MM
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyIn;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyOut;
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String transactionsJson;          // Serialised list
    @Column(nullable = false)
    private int versionNumber;
    @Column(length = 2000)
    private String correctionSummary;
    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
```

---

### ExportCacheEntity and IdempotencyRecordEntity

See `data-model.md` for full column definitions. JPA mapping follows the same pattern: `@Entity`, `@Table`, `@Id`, `@Column` with constraints matching the data model.

---

### Transaction.category (additive column on Group 2 entity)

Group 2's `Transaction` entity gains one nullable `@Column`:

```java
@Column(length = 50)
private String category;   // null = uncategorised; set by CategoryResolver or manual override
```

This is the **only** modification to a Group 2 entity. It must be coordinated with Group 2 to add the migration script.

---

## 3. Repository Layer

```java
// US-01  (GAP-1 resolved: PENDING is now a valid status; sort by timestamp only)
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByAccountIdAndTimestampBetweenOrderByTimestampAsc(
        Long accountId, LocalDateTime start, LocalDateTime end);
}

// US-02
public interface StandingOrderRepository extends JpaRepository<StandingOrderEntity, String> {
    List<StandingOrderEntity> findBySourceAccountId(Long accountId);
    List<StandingOrderEntity> findByStatusAndNextRunDate(StandingOrderStatus status, LocalDateTime date);
    Optional<StandingOrderEntity> findBySourceAccountIdAndPayeeAccountAndAmountAndFrequencyAndStatus(
        Long accountId, String payeeAccount, BigDecimal amount, Frequency frequency, StandingOrderStatus status);
}

// US-03
public interface NotificationDecisionRepository extends JpaRepository<NotificationDecisionEntity, String> {
    boolean existsByEventId(String eventId);
}

// US-04
public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatementEntity, Long> {
    Optional<MonthlyStatementEntity> findTopByAccountIdAndPeriodOrderByVersionNumberDesc(
        Long accountId, String period);
    Optional<MonthlyStatementEntity> findByAccountIdAndPeriodAndVersionNumber(
        Long accountId, String period, int versionNumber);
}

// US-01 export cache
public interface ExportCacheRepository extends JpaRepository<ExportCacheEntity, Long> {
    Optional<ExportCacheEntity> findByAccountIdAndParamHash(Long accountId, String paramHash);
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}

// Idempotency
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}

// US-03 — Notification opt-in preference (GAP-3 resolved)
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, Long> {
    Optional<NotificationPreferenceEntity> findByCustomerIdAndEventType(Long customerId, String eventType);
    List<NotificationPreferenceEntity> findAllByCustomerId(Long customerId);
}
```

---

## 4. Service Layer

### TransactionHistoryService (US-01)

| Method | Signature | Logic |
|---|---|---|
| `getHistory` | `TransactionHistoryResponse getHistory(long accountId, LocalDate start, LocalDate end, UserPrincipal caller)` | Validate caller owns account + has TRANSACTION:READ; apply date defaults and overrides; check CLOSED window; query TransactionRepository ordered by `timestamp ASC`; PENDING transactions appear in their natural chronological position; return response DTO |
| `exportPdf` | `byte[] exportPdf(long accountId, LocalDate start, LocalDate end, UserPrincipal caller)` | Validate caller; compute param hash (SHA-256); check ExportCacheRepository; if hit return cached bytes; else build PDF via PdfStatementService and cache result |

### PdfStatementService (US-01)

| Method | Signature | Logic |
|---|---|---|
| `buildPdf` | `byte[] buildPdf(long accountId, LocalDate start, LocalDate end, List<TransactionEntity> transactions)` | Use iText 7 to construct in-memory PDF; include header (account masked ID, date range), body (transactions table); return bytes |

### StandingOrderService (US-02)

| Method | Signature | Logic |
|---|---|---|
| `createStandingOrder` | `StandingOrderResponse create(long accountId, CreateStandingOrderRequest req, UserPrincipal caller)` | Validate caller ownership + SO:CREATE; validate all fields including Modulo 97 and startDate ≥ now+24h; validate `req.amount ≤ account.dailyTransferLimit` (throw `SemanticValidationException` → 422 `field=amount`, code `ERR_AMOUNT_EXCEEDS_LIMIT` if exceeded — GAP-2 resolved); check for duplicate ACTIVE order; calculate nextRunDate (holiday-shifted); persist; return 201 DTO |
| `listStandingOrders` | `StandingOrderListResponse list(long accountId, UserPrincipal caller)` | Validate caller ownership + SO:READ; fetch by sourceAccountId; return list DTO |
| `cancelStandingOrder` | `CancelStandingOrderResponse cancel(String standingOrderId, UserPrincipal caller)` | Validate caller ownership + SO:CANCEL; check standing order exists; check not within 24h of nextRunDate; set status CANCELLED; persist; return DTO |

### StandingOrderScheduler (US-02)

| Method | Signature | Logic |
|---|---|---|
| `processOrders` | `@Scheduled(cron = "1 0 0 * * *") void processOrders()` | Find all ACTIVE orders where nextRunDate = today; for each attempt execution at 08:00 UTC (first attempt); if RETRY_PENDING, schedule retry at 16:00 UTC; on final failure persist FAILED transaction + trigger notification event |
| `attemptExecution` | `void attemptExecution(StandingOrderEntity order)` | Check balance; if sufficient debit account, create TRANSFER transaction with idempotency key, update nextRunDate; if insufficient mark RETRY_PENDING or FAILED_INSUFFICIENT_FUNDS |
| `calculateNextRunDate` | `LocalDateTime calculateNextRunDate(LocalDateTime current, Frequency frequency)` | Apply frequency increment; shift to next Canadian business day via CanadianHolidayService |

### NotificationEvaluationService (US-03)

| Method | Signature | Logic |
|---|---|---|
| `evaluate` | `NotificationDecisionResponse evaluate(NotificationEventRequest req)` | Check eventId deduplication (existsByEventId → 409); validate eventType against classification matrix (unknown → 422); validate customerId linked to accountId (mismatch → 422); determine mandatory vs optional via classification matrix; **if mandatory**: lookup `NotificationPreferenceRepository.findByCustomerIdAndEventType` — if record found with `opted_in=false` set `mandatoryOverride=true`, decision always RAISED regardless; **if optional**: lookup preference — if absent or `opted_in=true` decision RAISED; if `opted_in=false` decision SUPPRESSED (GAP-3 resolved); persist NotificationDecisionEntity; return response DTO |

### MonthlyStatementService (US-04)

| Method | Signature | Logic |
|---|---|---|
| `getStatement` | `MonthlyStatementResponse getStatement(long accountId, String period, Integer version, UserPrincipal caller)` | Validate caller ownership + STATEMENT:READ (re-validate at delivery); validate period is YYYY-MM and closed (before current month); check retention window (>7 years → 410); find statement entity (by version or latest); return response DTO |

### SpendingInsightService (US-05)

| Method | Signature | Logic |
|---|---|---|
| `getInsights` | `SpendingInsightResponse getInsights(long accountId, int year, int month, UserPrincipal caller)` | Validate caller ownership + INSIGHTS:READ; validate year/month (not future); fetch eligible transactions (WITHDRAW + TRANSFER-out, SUCCESS, in month range); auto-categorise via CategoryResolver; aggregate by category; compute percentages (sum = 100); compute sixMonthTrend (6 entries, holiday/existence aware); assemble response DTO |
| `recategorise` | `RecategoriseResponse recategorise(long accountId, long transactionId, String category, UserPrincipal caller)` | Validate caller ownership + INSIGHTS:READ; validate category is one of 8 values (else 422); update transaction.category; re-run insight aggregation for the month; return updated breakdown DTO |

---

## 5. Controller Layer

### TransactionController

```java
@RestController
@RequestMapping("/accounts/{accountId}/transactions")
public class TransactionController {

    @GetMapping
    // Permission: TRANSACTION:READ | Returns: 200 TransactionHistoryResponse
    public ResponseEntity<TransactionHistoryResponse> getHistory(
        @PathVariable long accountId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @AuthenticationPrincipal UserPrincipal caller) { ... }

    @GetMapping("/export")
    // Permission: TRANSACTION:READ | Returns: 200 application/pdf
    public ResponseEntity<byte[]> exportPdf(
        @PathVariable long accountId,
        @RequestParam String startDate,
        @RequestParam String endDate,
        @AuthenticationPrincipal UserPrincipal caller) { ... }
}
```

### StandingOrderController

```java
@RestController
public class StandingOrderController {

    @PostMapping("/accounts/{accountId}/standing-orders")
    // Permission: SO:CREATE | Returns: 201 StandingOrderResponse
    public ResponseEntity<StandingOrderResponse> create(
        @PathVariable long accountId,
        @Valid @RequestBody CreateStandingOrderRequest request,
        @AuthenticationPrincipal UserPrincipal caller) { ... }

    @GetMapping("/accounts/{accountId}/standing-orders")
    // Permission: SO:READ | Returns: 200 StandingOrderListResponse
    public ResponseEntity<StandingOrderListResponse> list(
        @PathVariable long accountId,
        @AuthenticationPrincipal UserPrincipal caller) { ... }

    @DeleteMapping("/standing-orders/{standingOrderId}")
    // Permission: SO:CANCEL | Returns: 200 CancelStandingOrderResponse
    public ResponseEntity<CancelStandingOrderResponse> cancel(
        @PathVariable String standingOrderId,
        @AuthenticationPrincipal UserPrincipal caller) { ... }
}
```

### NotificationController

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @PostMapping("/evaluate")
    // Auth: mTLS or X-Api-Key | Returns: 200 NotificationDecisionResponse
    public ResponseEntity<NotificationDecisionResponse> evaluate(
        @Valid @RequestBody NotificationEventRequest request) { ... }
}
```

### StatementController

```java
@RestController
@RequestMapping("/accounts/{accountId}/statements")
public class StatementController {

    @GetMapping("/{period}")
    // Permission: STATEMENT:READ | Returns: 200 MonthlyStatementResponse
    public ResponseEntity<MonthlyStatementResponse> getStatement(
        @PathVariable long accountId,
        @PathVariable String period,
        @RequestParam(required = false) Integer version,
        @AuthenticationPrincipal UserPrincipal caller) { ... }
}
```

### InsightController

```java
@RestController
@RequestMapping("/accounts/{accountId}")
public class InsightController {

    @GetMapping("/insights")
    // Permission: INSIGHTS:READ | Returns: 200 SpendingInsightResponse
    public ResponseEntity<SpendingInsightResponse> getInsights(
        @PathVariable long accountId,
        @RequestParam int year,
        @RequestParam int month,
        @AuthenticationPrincipal UserPrincipal caller) { ... }

    @PutMapping("/transactions/{transactionId}/category")
    // Permission: INSIGHTS:READ | Returns: 200 RecategoriseResponse | 422 on invalid category
    public ResponseEntity<RecategoriseResponse> recategorise(
        @PathVariable long accountId,
        @PathVariable long transactionId,
        @Valid @RequestBody RecategoriseRequest request,
        @AuthenticationPrincipal UserPrincipal caller) { ... }
}
```

**Global exception handler**: `@ControllerAdvice GlobalExceptionHandler` maps `MethodArgumentNotValidException` → 400, custom `OwnershipException` → 401, `PermissionDeniedException` → 401, `LockException` → 403, `ResourceNotFoundException` → 404, `BusinessStateException` → 409, `RetentionWindowException` → 410, `SemanticValidationException` → 422. All responses use `ErrorResponse` schema.

---

## 6. Scheduler

### StandingOrderExecutionJob

```java
@Component
public class StandingOrderExecutionJob {

    @Scheduled(cron = "1 0 0 * * *", zone = "UTC")
    public void processOrders() {
        // 1. Find all ACTIVE StandingOrders where nextRunDate = today (UTC date)
        // 2. For each order: lock it (set status LOCKED), attempt execution
        // 3. On success: persist SUCCESS TRANSFER transaction, calculate new nextRunDate,
        //    update status to ACTIVE (or TERMINATED if endDate passed)
        // 4. On insufficient funds at first attempt: set RETRY_PENDING
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "UTC")
    public void firstAttemptRetry() {
        // Execute orders with status RETRY_PENDING where scheduledDate = today
    }

    @Scheduled(cron = "0 0 16 * * *", zone = "UTC")
    public void finalAttemptRetry() {
        // Execute orders still RETRY_PENDING
        // On failure: persist FAILED TRANSFER transaction (type=TRANSFER, status=FAILED)
        //             call NotificationEvaluationService.evaluate(StandingOrderFailure event)
        //             set order status back to ACTIVE for next cycle
    }
}
```

### CanadianHolidayService

```java
@Service
public class CanadianHolidayService {
    // Hard-coded set of federal statutory holidays loaded from configuration
    public boolean isHoliday(LocalDate date) { ... }
    public boolean isWeekend(LocalDate date) { ... }
    public LocalDateTime nextBusinessDay(LocalDateTime date) {
        // Advance by 1 day until isHoliday() == false && isWeekend() == false
    }
}
```

### IdempotencyPurgeJob

```java
@Component
public class IdempotencyPurgeJob {
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void purge() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(72);
        idempotencyRecordRepository.deleteByCreatedAtBefore(cutoff);
        exportCacheRepository.deleteByCreatedAtBefore(cutoff);
    }
}
```

---

## 7. Security

### JWT Bearer (User-Facing Endpoints)

All user-facing endpoints (`/accounts/**`, `/standing-orders/**`) use the Group 2 `JwtAuthenticationFilter`. The filter extracts the JWT subject claim, resolves the `UserPrincipal` (userId, roles, permissions, customerId), and populates the `SecurityContext`.

### Permission Enforcement

`SecurityConfig` configures `HttpSecurity` with method-level security (`@EnableMethodSecurity`). Each service method is annotated:

```java
@PreAuthorize("hasAuthority('TRANSACTION:READ')")
public TransactionHistoryResponse getHistory(...) { ... }

@PreAuthorize("hasAuthority('SO:CREATE')")
public StandingOrderResponse create(...) { ... }

@PreAuthorize("hasAuthority('SO:READ')")
public StandingOrderListResponse list(...) { ... }

@PreAuthorize("hasAuthority('SO:CANCEL')")
public CancelStandingOrderResponse cancel(...) { ... }

@PreAuthorize("hasAuthority('STATEMENT:READ')")
public MonthlyStatementResponse getStatement(...) { ... }

@PreAuthorize("hasAuthority('INSIGHTS:READ')")
public SpendingInsightResponse getInsights(...) { ... }
```

`NOTIFICATION:READ` for US-03 is not applied via `@PreAuthorize` on the evaluate endpoint itself (which uses service-to-service auth). It is checked inside `NotificationEvaluationService.evaluate()` for optional events where the customer's entitlement must be validated.

### Ownership Enforcement

`OwnershipValidator` is called inside each service method after the permission check passes:

```java
public void assertOwnership(long accountId, UserPrincipal caller) {
    if (caller.hasRole("ADMIN")) return;
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    if (!account.getCustomerId().equals(caller.getCustomerId())) {
        throw new OwnershipException("Caller does not own account " + accountId);
    }
}
```

`OwnershipException` maps to HTTP 401 in `GlobalExceptionHandler`.

### Service-to-Service Auth (US-03 `/notifications/evaluate`)

`SecurityConfig` applies a separate security filter chain for `/notifications/**` that uses `ServiceAuthenticationProvider`:

```java
@Bean
@Order(1)
public SecurityFilterChain notificationFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/notifications/**")
        .addFilterBefore(new ServiceApiKeyFilter(serviceRegistry), UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    return http.build();
}
```

`ServiceApiKeyFilter` reads `X-Api-Key` header, hashes it (SHA-256), and looks up the `serviceId` in the configured allow-list. If found, creates a `ServiceAuthentication` principal. mTLS is enabled at the transport layer via `application.yml` when running with the `prod` profile.

---

## 8. Frontend

### Component Structure

| Component | Story | Purpose |
|---|---|---|
| `TransactionHistoryPage` | US-01 | Page shell; renders date pickers, TransactionList, ExportButton |
| `TransactionList` | US-01 | Renders list of TransactionItem; PENDING group at top |
| `TransactionItem` | US-01 | Single row with all transaction fields |
| `ExportButton` | US-01 | Triggers PDF download; calls `useTransactionExport` |
| `StandingOrdersPage` | US-02 | Page shell; renders CreateStandingOrderForm + StandingOrderList |
| `StandingOrderList` | US-02 | Renders StandingOrderItem per order |
| `StandingOrderItem` | US-02 | Displays order details + cancel button (disabled within 24h lock) |
| `CreateStandingOrderForm` | US-02 | Controlled form; client-side validation before submit |
| `MonthlyStatementPage` | US-04 | Page shell; period/version picker + StatementViewer |
| `StatementViewer` | US-04 | Renders statement fields and transaction table |
| `SpendingInsightsPage` | US-05 | Page shell; year/month picker + charts + category table |
| `CategoryDonutChart` | US-05 | Donut chart from categoryBreakdown; always 8 segments |
| `SixMonthBarChart` | US-05 | Bar chart from sixMonthTrend; always 6 bars |
| `TransactionCategoryRow` | US-05 | Editable category for a single transaction |
| `ErrorMessage` | Shared | Displays ErrorResponse.message with code |
| `LoadingSpinner` | Shared | Loading state |

### React Query Hooks

```javascript
// US-01
useTransactionHistory(accountId, startDate, endDate)
// useQuery(['transactions', accountId, startDate, endDate], ...)

useTransactionExport(accountId, startDate, endDate)
// useMutation — triggers PDF download on invoke

// US-02
useStandingOrders(accountId)
// useQuery(['standingOrders', accountId], ...)

useCreateStandingOrder()
// useMutation; on success: queryClient.invalidateQueries(['standingOrders', accountId])

useCancelStandingOrder()
// useMutation; on success: queryClient.invalidateQueries(['standingOrders', accountId])

// US-04
useMonthlyStatement(accountId, period, version)
// useQuery(['statement', accountId, period, version], ...)

// US-05
useSpendingInsights(accountId, year, month)
// useQuery(['insights', accountId, year, month], ...)

useRecategorise()
// useMutation; on success: queryClient.invalidateQueries(['insights', accountId, year, month])
```

### Error Handling

All API calls via Axios include an interceptor that extracts `{ code, message, field }` from error response bodies. Each page-level component renders `<ErrorMessage>` when the query/mutation is in error state. HTTP 401 redirects to the login page via a global Axios interceptor.

---

## 9. Testing Plan

### Backend — Unit Tests (JUnit 5 + Mockito)

| Class Under Test | Key Test Cases |
|---|---|
| `TransactionHistoryService` | Default date range applied; future endDate overridden silently; CLOSED account within 90 days returns history; CLOSED account after 90 days throws 409; caller without TRANSACTION:READ throws 401; CUSTOMER on foreign account throws 401 |
| `PdfStatementService` | PDF bytes returned for valid inputs; identical inputs return cached bytes (ExportCacheRepository mock) |
| `StandingOrderService` | Valid creation returns StandingOrderEntity with ACTIVE status and holiday-shifted nextRunDate; Modulo 97 failure → 400; startDate < 24h → 400; amount exceeds dailyTransferLimit → 422 ERR_AMOUNT_EXCEEDS_LIMIT; duplicate ACTIVE order → 409; cancel > 24h before nextRunDate → 200 with CANCELLED; cancel < 24h → 403 ERR_SO_LOCKED |
| `StandingOrderExecutionJob` | Successful execution creates TRANSFER transaction and advances nextRunDate; insufficient funds at 08:00 sets RETRY_PENDING; insufficient funds at 16:00 creates FAILED transaction and triggers notification event; idempotency key prevents duplicate execution |
| `NotificationEvaluationService` | Duplicate eventId → 409; unknown eventType → 422; mandatory event with opted-out customer → RAISED with mandatoryOverride=true (reads NotificationPreferenceRepository); optional event with opted-in customer → RAISED; optional event with opted-out customer (opted_in=false in preference table) → SUPPRESSED; optional event for ineligible customer → 422 |
| `MonthlyStatementService` | Returns latest version by default; returns specific version when requested; period not closed → 409; beyond retention window → 410; permission re-validated at delivery; empty month returns zero totals |
| `SpendingInsightService` | All 8 categories returned even with zero totals; percentages sum to 100; 6-month trend always 6 entries; recategorise updates transaction.category and recalculates breakdown; DEPOSIT excluded from totals; FAILED excluded from totals; invalid category → 422 |
| `Mod97Validator` | Valid IBAN-style account passes; transposition error fails; too long fails |
| `CanadianHolidayService` | Weekend shifted; statutory holiday shifted; valid business day not shifted; Friday before a Monday holiday shifted to Tuesday |
| `CategoryResolver` | Keyword match assigns correct category; unrecognised description → null (uncategorised) |
| `OwnershipValidator` | ADMIN bypasses ownership; CUSTOMER on own account passes; CUSTOMER on foreign account throws OwnershipException |

### Backend — Integration Tests (MockMvc + H2)

| Scenario | Method | Expectation |
|---|---|---|
| GET /accounts/1/transactions with JWT lacking TRANSACTION:READ | GET | 401 ErrorResponse |
| GET /accounts/1/transactions with valid JWT and date range > 366 days | GET | 400 ERR_DATE_RANGE_EXCEEDED |
| POST /accounts/1/standing-orders with payeeAccount failing Modulo 97 | POST | 400 ERR_CHECKSUM_FAILURE |
| DELETE /standing-orders/{id} within 24h of nextRunDate | DELETE | 403 ERR_SO_LOCKED |
| POST /notifications/evaluate with unknown eventType | POST | 422 ERR_UNKNOWN_EVENT_TYPE |
| POST /notifications/evaluate with duplicate eventId | POST | 409 ERR_DUPLICATE_EVENT |
| GET /accounts/1/statements/2026-03 for open period | GET | 409 ERR_PERIOD_NOT_CLOSED |
| GET /accounts/1/statements/2018-01 (beyond retention) | GET | 410 ERR_RETENTION_EXPIRED |
| GET /accounts/1/insights?year=2030&month=1 (future month) | GET | 409 ERR_FUTURE_MONTH |
| PUT /accounts/1/transactions/5/category with invalid category | PUT | 422 ERR_INVALID_CATEGORY |

### Frontend — Component Tests (Jest + React Testing Library)

| Component | Key Test Cases |
|---|---|
| `TransactionList` | Renders transactions in timestamp ascending order; PENDING status renders with amber badge; renders empty state when count = 0 |
| `TransactionItem` | Displays all fields; idempotencyKey shown when present; hidden when null |
| `ExportButton` | Calls useTransactionExport on click; shows loading state during request |
| `CreateStandingOrderForm` | Submit disabled when required fields empty; displays field-level errors from server 400 response |
| `StandingOrderItem` | Cancel button disabled when within 24h of nextRunDate; enabled otherwise |
| `CategoryDonutChart` | Always renders 8 segments; segment for zero-value category has zero percentage label |
| `SixMonthBarChart` | Always renders 6 bars; bar for accountExisted=false has 0 height |
| `TransactionCategoryRow` | Dropdown has exactly 8 options; selecting new category calls useRecategorise |
| `StatementViewer` | Renders correctionSummary when versionNumber > 1; hides on version 1 |
| `ErrorMessage` | Displays message from ErrorResponse; shows field when present |

### Frontend — Hook Tests

| Hook | Key Test Cases |
|---|---|
| `useTransactionHistory` | Called with correct query key; returns data on success; returns error object on 401 |
| `useRecategorise` | Calls correct PUT endpoint; invalidates insight query on success |
| `useCancelStandingOrder` | Calls DELETE endpoint; invalidates standingOrders query on success |

### Contract Test Approach

Backend controllers are tested with `@SpringBootTest` + `MockMvc`. Each controller test asserts:
- HTTP status code matches the contract
- Response body fields match the contract schema (using `jsonPath` assertions)
- `ErrorResponse` structure (`code`, `message`, `field`) is correct on all error paths

No external contract testing framework is introduced. Assertions are hand-written using MockMvc `jsonPath`.

---

## 10. Git Workflow

### Branch Naming

| Type | Pattern | Example |
|---|---|---|
| Feature story | `feature/us-{nn}-{short-name}` | `feature/us-01-transaction-history` |
| Bug fix | `fix/{short-description}` | `fix/modulo97-edge-case` |
| Spec branch | `spec/{feature-name}` | `spec/group3-spec` (current) |

### PR Requirements

- Title references story number: `[US-01] Implement transaction history endpoint`
- PR description includes: story reference, what was changed, how to test
- At least one reviewer approval required
- All CI checks must pass before merge
- Draft PRs allowed for work-in-progress; must be marked ready before review

### CI Checks

The following must all pass on every PR:

1. **Compile** — `./mvnw compile` (backend); `npm run build` (frontend)
2. **Unit tests** — `./mvnw test` — all JUnit 5 unit tests green
3. **Frontend tests** — `npm test -- --run` — all Jest tests green
4. **Code coverage** — `./mvnw test jacoco:report` — service layer ≥ 80% line coverage
5. **Integration tests** — `./mvnw test -Dgroups=Integration` — MockMvc tests green

### Merge Strategy

- Squash merge to `main` — one commit per PR
- Merge commit message format: `[US-{nn}] {brief description} (#PR_NUMBER)`
- Do not force-push to shared branches
- Delete feature branch after merge
