# Tasks: Group 3 — Digital Banking Platform

**Branch**: `spec/group3-spec` | **Date**: 2026-04-08  
**Spec**: [specs/group3-spec.md](../../group3-spec.md) | **Plan**: [plan.md](plan.md)  
**Stories**: US-01 (Transaction History), US-02 (Standing Orders), US-03 (Notification Evaluation), US-04 (Monthly Statement), US-05 (Spending Insights)

> **[P]** = task can run in parallel with other [P] tasks once its listed predecessors are complete.  
> **[USn]** = user story label; required for all Phase 3+ tasks.  
> Tasks without [P] must be completed in sequence before dependent tasks start.

---

## Gap Resolution Record

All four gaps identified during initial analysis have been resolved. No stubs or TODO markers remain in implementation tasks.

| # | Issue | Resolution | Data Impact |
|---|---|---|---|
| GAP-1 | PENDING vs POSTED transaction ordering | `PENDING` added to `TransactionStatus` enum (Group 2 coordination). Transactions sorted by `timestamp ASC` — no status-based grouping. | Group 2 `TransactionStatus` enum + Flyway migration |
| GAP-2 | Daily transfer limit source | `daily_transfer_limit DECIMAL(19,2) NOT NULL DEFAULT 3000.00` added to Group 2 `AccountEntity`. Standing order creation validates `amount ≤ account.dailyTransferLimit`. | Group 2 `accounts` table + Flyway migration |
| GAP-3 | Customer notification preference storage | New `notification_preferences` table (`customer_id`, `event_type`, `opted_in`). `NotificationEvaluationService` reads this table for all preference checks. | New Group 3 entity + table |
| GAP-4 | Auto-categorisation keyword list | Keywords defined in `application.yml` under `banking.categories` (8 categories, 7–13 keywords each). See T093 for full config. | `application.yml` config only |

---

## Phase 1 — Setup

*Goal*: Initialise the project structure so that all five stories can be developed without rework.

**Independent test criteria**: `./mvnw compile` passes; `npm run build` passes; H2 in-memory database starts correctly with `local` profile.

- [x] T001 Create backend Spring Boot 3 Maven project under `backend/` with dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, h2 (test/local scope), mysql-connector-j (runtime scope), itext7-core 7.x (implementation scope), junit-5 (test scope), mockito-core (test scope)
- [x] T002 Create frontend Vite + React 18 project under `frontend/` with dependencies: axios, @tanstack/react-query v5, react-router-dom; dev dependencies: jest, @testing-library/react, @testing-library/jest-dom, vitest or jest-environment-jsdom
- [x] T003 Create `backend/src/main/resources/application.yml` with Spring datasource placeholders, JPA settings, and scheduler enabled; create `application-local.yml` (H2 in-memory, show-sql=true) and `application-test.yml` (H2 in-memory) as defined in `quickstart.md`
- [x] T004 Create package skeleton under `backend/src/main/java/com/fdm/banking/`: `config/`, `controller/`, `service/`, `repository/`, `entity/`, `dto/request/`, `dto/response/`, `mapper/`, `security/`, `scheduler/`, `util/` — empty placeholder classes are acceptable at this stage
- [x] T005 Create frontend folder structure under `frontend/src/`: `api/`, `hooks/`, `components/transactions/`, `components/standingorders/`, `components/statements/`, `components/insights/`, `components/shared/`, `tests/components/`, `tests/hooks/`
- [x] T006 Verify Group 2 published entity classes (`UserEntity`, `RoleEntity`, `CustomerEntity`, `AccountEntity`, `TransactionEntity`) and repository beans are accessible by import from the Group 2 module/package; document the import path in a comment at the top of `backend/src/main/java/com/fdm/banking/config/SecurityConfig.java`

---

## Phase 2 — Foundational

*Goal*: All shared utilities, security infrastructure, cross-cutting concerns, and audit infrastructure complete. All five story phases depend on these tasks.

**Independent test criteria**: `Mod97ValidatorTest`, `CanadianHolidayServiceTest`, and `OwnershipValidatorTest` all green; SecurityConfig loads without errors; H2 schema creates on startup.

- [x] T007 Implement `ErrorResponse` DTO (fields: `code`, `message`, `field`) in `backend/src/main/java/com/fdm/banking/dto/response/ErrorResponse.java`
- [x] T008 Implement `GlobalExceptionHandler` (`@ControllerAdvice`) mapping all custom exceptions to the correct HTTP status and `ErrorResponse` body, covering: `OwnershipException` → 401, `PermissionDeniedException` → 401, `LockException` → 403, `ResourceNotFoundException` → 404, `BusinessStateException` → 409, `RetentionWindowException` → 410, `SemanticValidationException` → 422, `MethodArgumentNotValidException` → 400 in `backend/src/main/java/com/fdm/banking/controller/GlobalExceptionHandler.java`
- [x] T009 [P] Implement `Mod97Validator` utility class (pure IBAN Modulo 97 algorithm) and `@ValidPayeeAccount` custom `@Constraint` annotation in `backend/src/main/java/com/fdm/banking/util/Mod97Validator.java`
- [x] T010 [P] Implement `CanadianHolidayService` with hard-coded federal statutory holidays (New Year's Day, Good Friday, Victoria Day, Canada Day, Civic Holiday, Labour Day, Thanksgiving, Remembrance Day, Christmas Day, Boxing Day) and `isHoliday(LocalDate)`, `isWeekend(LocalDate)`, `nextBusinessDay(LocalDateTime)` methods in `backend/src/main/java/com/fdm/banking/util/CanadianHolidayService.java`
- [x] T011 [P] Implement `OwnershipValidator` (`assertOwnership(long accountId, UserPrincipal caller)`) that bypasses for ADMIN role and throws `OwnershipException` (→ 401) when CUSTOMER does not own account in `backend/src/main/java/com/fdm/banking/security/OwnershipValidator.java`
- [x] T012 [P] Implement `AuditLogEntity` (`@Entity`, table `audit_log`), `AuditLogRepository`, and `AuditService.log(actorId, actorRole, action, resourceType, resourceId, outcome)` in `backend/src/main/java/com/fdm/banking/` (entity/repository/service sub-packages respectively)
- [x] T013 [P] Implement `IdempotencyRecordEntity` and `IdempotencyRecordRepository` (with `deleteByCreatedAtBefore`) in `backend/src/main/java/com/fdm/banking/entity/` and `repository/`
- [x] T014 [P] Implement `ExportCacheEntity` and `ExportCacheRepository` (with `findByAccountIdAndParamHash` and `deleteByCreatedAtBefore`) in `backend/src/main/java/com/fdm/banking/entity/` and `repository/`
- [x] T015 [P] Implement `ServiceApiKeyFilter` that reads `X-Api-Key` header, SHA-256 hashes it, and validates against configured `banking.notifications.allowed-service-ids` map; on success populate `SecurityContext` with `ServiceAuthentication` principal in `backend/src/main/java/com/fdm/banking/security/ServiceApiKeyFilter.java`
- [x] T016 Implement `SecurityConfig` with two `SecurityFilterChain` beans: (1) order 1 — matches `/notifications/**`, applies `ServiceApiKeyFilter`, requires authenticated; (2) order 2 — all other paths, applies Group 2 `JwtAuthenticationFilter`, requires authenticated; enable `@EnableMethodSecurity` in `backend/src/main/java/com/fdm/banking/config/SecurityConfig.java`
- [x] T017 [P] Implement `SchedulerConfig` with `@EnableScheduling` and thread pool configuration for background jobs in `backend/src/main/java/com/fdm/banking/config/SchedulerConfig.java`
- [x] T018 [P] Implement shared `ErrorMessage.jsx` component (accepts `code`, `message`, `field` props; renders field when present) in `frontend/src/components/shared/ErrorMessage.jsx`
- [x] T019 [P] Implement shared `LoadingSpinner.jsx` component in `frontend/src/components/shared/LoadingSpinner.jsx`
- [x] T020 [P] Configure Axios instance with base URL from `VITE_API_BASE_URL`, JWT Authorization header injection, and global 401 interceptor (redirect to login) in `frontend/src/api/axiosInstance.js`
- [x] T021 [P] Unit test `Mod97Validator`: valid IBAN-style account passes; transposition error fails; length > 34 fails; all-numeric short string fails in `backend/src/test/java/com/fdm/banking/util/Mod97ValidatorTest.java`
- [x] T022 [P] Unit test `CanadianHolidayService`: Saturday shifted; Sunday shifted; statutory holiday shifted; valid business day unchanged; Friday before Monday holiday lands on Tuesday in `backend/src/test/java/com/fdm/banking/util/CanadianHolidayServiceTest.java`
- [x] T023 [P] Unit test `OwnershipValidator`: ADMIN role bypasses check; CUSTOMER own account passes; CUSTOMER foreign account throws `OwnershipException` in `backend/src/test/java/com/fdm/banking/security/OwnershipValidatorTest.java`

---

## Phase 3 — US-01: Get Transaction History

*Story goal*: Authenticated CUSTOMER with `TRANSACTION:READ` can retrieve paginated transaction history for a date range from their own ACTIVE account (or CLOSED account within 90-day window), ordered by timestamp ascending; an idempotent PDF export is also available.

**Independent test criteria**: `GET /accounts/1/transactions` returns 200 with transactions in timestamp ascending order for a valid CUSTOMER JWT; returns 401 for missing permission; returns 400 for date range > 366 days; returns 409 for CLOSED account outside 90-day window; PENDING status transactions appear in their natural timestamp position. PDF export returns `application/pdf`. All `TransactionHistoryServiceTest` cases green.

- [x] T119 [US1] Add `PENDING` value to Group 2 `TransactionStatus` enum (alongside `SUCCESS` and `FAILED`); coordinate with Group 2 team to update the Flyway/Liquibase migration script if the column is a database-level ENUM; document the three enum values in the Group 2 enum file *(Group 2 coordination required)* in `backend/src/main/java/com/fdm/banking/entity/TransactionStatus.java`
- [x] T024 [P] [US1] Create `TransactionHistoryResponse` DTO (fields: `accountId`, `startDate`, `endDate`, `transactionCount`, `transactions` list) and `TransactionItemResponse` DTO (fields: `transactionId`, `amount`, `type`, `status`, `timestamp`, `description`, `idempotencyKey`) in `backend/src/main/java/com/fdm/banking/dto/response/`
- [x] T025 [US1] Add query method to Group 2 `TransactionRepository`: `findByAccountIdAndTimestampBetweenOrderByTimestampAsc(Long accountId, LocalDateTime start, LocalDateTime end)` — orders all transactions (including PENDING) by timestamp ascending; if Group 2 repository cannot be modified, add a custom `@Query` in a Group 3-owned `TransactionQueryRepository` in `backend/src/main/java/com/fdm/banking/repository/TransactionQueryRepository.java` — **do not redefine the entity**
- [x] T026 [US1] Implement `TransactionHistoryService.getHistory(long accountId, LocalDate start, LocalDate end, UserPrincipal caller)`: assert `TRANSACTION:READ` permission; assert ownership via `OwnershipValidator`; apply default start (now minus 28 days) and default end (now) when absent; if supplied `endDate` is future override to now silently; validate range ≤ 366 days (throw `BusinessStateException` → 400 with `field=endDate`); check account status — if CLOSED and closure date > 90 days ago throw `BusinessStateException` → 409, code `ERR_ACC_003`; fetch and return ordered list; write audit log entry in `backend/src/main/java/com/fdm/banking/service/TransactionHistoryService.java`
- [x] T027 [US1] Implement `PdfStatementService.buildPdf(long accountId, LocalDate start, LocalDate end, List<TransactionEntity> transactions)` using iText 7: create in-memory `PdfDocument`; write header (Statement of Activity, masked account ID, effective date range); write transaction table (all fields, chronological); return `byte[]` in `backend/src/main/java/com/fdm/banking/service/PdfStatementService.java`
- [x] T028 [US1] Implement `TransactionHistoryService.exportPdf(long accountId, LocalDate start, LocalDate end, UserPrincipal caller)`: assert `TRANSACTION:READ` + ownership; compute SHA-256 hash of `accountId + effectiveStartDate + effectiveEndDate`; look up `ExportCacheRepository.findByAccountIdAndParamHash`; if hit return cached bytes; else call `PdfStatementService.buildPdf`, persist result to `ExportCacheEntity`, return bytes in `backend/src/main/java/com/fdm/banking/service/TransactionHistoryService.java`
- [x] T029 [US1] Implement `TransactionController`: `GET /accounts/{accountId}/transactions` (calls `getHistory`, returns 200 `TransactionHistoryResponse`); `GET /accounts/{accountId}/transactions/export` (calls `exportPdf`, returns 200 with `Content-Type: application/pdf`, `Content-Disposition: attachment`) in `backend/src/main/java/com/fdm/banking/controller/TransactionController.java`
- [x] T030 [P] [US1] Unit test `TransactionHistoryService`: default date range applied; future endDate overridden silently; date range > 366 days → 400; CLOSED account within 90 days → 200 history; CLOSED account after 90 days → 409 ERR_ACC_003; caller without `TRANSACTION:READ` → 401; CUSTOMER on foreign account → 401 ownership; empty result set → 200 with count=0; verify all returned transactions ordered by `timestamp ASC` with PENDING appearing in natural chronological position in `backend/src/test/java/com/fdm/banking/service/TransactionHistoryServiceTest.java`
- [x] T031 [P] [US1] Unit test `PdfStatementService`: returns non-empty byte array for valid inputs; byte array parses as valid PDF; identical inputs return structurally identical output in `backend/src/test/java/com/fdm/banking/service/PdfStatementServiceTest.java`
- [x] T032 [US1] Integration test `TransactionController` (MockMvc, H2): 200 with correct JSON for valid CUSTOMER JWT and in-range dates; 401 when JWT lacks `TRANSACTION:READ`; 400 when date range > 366 days (`field=endDate`); 409 ERR_ACC_003 for CLOSED account outside window; PDF export returns `application/pdf`; identical export request returns same bytes (cache hit) in `backend/src/test/java/com/fdm/banking/controller/TransactionControllerTest.java`
- [x] T033 [P] [US1] Implement `transactionApi.js`: `getTransactionHistory(accountId, startDate, endDate)` (GET); `exportTransactionPdf(accountId, startDate, endDate)` (GET, responseType blob) using Axios instance in `frontend/src/api/transactionApi.js`
- [x] T034 [P] [US1] Implement `useTransactionHistory(accountId, startDate, endDate)` React Query `useQuery` hook (key: `['transactions', accountId, startDate, endDate]`) in `frontend/src/hooks/useTransactionHistory.js`
- [x] T035 [P] [US1] Implement `useTransactionExport(accountId, startDate, endDate)` React Query `useMutation` hook that triggers blob download on invoke in `frontend/src/hooks/useTransactionExport.js`
- [x] T036 [US1] Implement `TransactionItem.jsx` (renders all transaction fields; shows `idempotencyKey` row when present, hides when null) in `frontend/src/components/transactions/TransactionItem.jsx`
- [x] T037 [US1] Implement `TransactionList.jsx` (receives `transactions` array pre-sorted by timestamp ascending from the API; renders each transaction in order using `TransactionItem`; PENDING status displays amber badge, SUCCESS green badge, FAILED red badge; renders empty state message when count = 0) in `frontend/src/components/transactions/TransactionList.jsx`
- [x] T038 [US1] Implement `ExportButton.jsx` (calls `useTransactionExport` on click; shows loading state; triggers browser download on success; shows `ErrorMessage` on failure) in `frontend/src/components/transactions/ExportButton.jsx`
- [x] T039 [US1] Implement `TransactionHistoryPage.jsx` (date pickers for startDate/endDate; renders `LoadingSpinner`, `ErrorMessage`, or `TransactionList + ExportButton` based on query state) in `frontend/src/components/transactions/TransactionHistoryPage.jsx`
- [x] T040 [P] [US1] Jest + RTL test `TransactionItem`: renders all fields; idempotencyKey visible when present; hidden when null in `frontend/src/tests/components/TransactionItemTest.jsx`
- [x] T041 [P] [US1] Jest + RTL test `TransactionList`: renders transactions in the order received from props; PENDING status renders amber badge; SUCCESS renders green badge; renders empty state when array is empty; correct transaction count rendered in `frontend/src/tests/components/TransactionListTest.jsx`
- [x] T042 [P] [US1] Jest + RTL test `ExportButton`: calls mutation on click; shows loading text during pending state; triggers download on success in `frontend/src/tests/components/ExportButtonTest.jsx`
- [x] T043 [P] [US1] Jest test `useTransactionHistory`: called with correct query key; returns mocked data on success; exposes error on 401 response in `frontend/src/tests/hooks/useTransactionHistoryTest.js`

---

## Phase 4 — US-02: Standing Order Management

*Story goal*: CUSTOMER with `SO:CREATE/READ/CANCEL` can create, list, and cancel standing orders on their own accounts. Background scheduler executes orders daily with holiday shifting and two-attempt retry on insufficient funds. Execution is idempotent per scheduled cycle.

**Independent test criteria**: `POST /accounts/1/standing-orders` returns 201 with ACTIVE status and holiday-shifted `nextRunDate`; Modulo 97 failure returns 400; duplicate returns 409; amount exceeding `dailyTransferLimit` (CAD 3,000 default) returns 422; cancellation within 24h returns 403 ERR_SO_LOCKED. Scheduler unit tests green. All `StandingOrderServiceTest` cases green.

- [x] T120 [US2] Add `daily_transfer_limit DECIMAL(19,2) NOT NULL DEFAULT 3000.00` column to Group 2 `AccountEntity` and `accounts` table; coordinate with Group 2 team for Flyway/Liquibase migration script; expose `getDailyTransferLimit()` getter on `AccountEntity` *(Group 2 coordination required)* in `backend/src/main/java/com/fdm/banking/entity/AccountEntity.java`

- [x] T044 [P] [US2] Define Java enums `Frequency` (DAILY, WEEKLY, MONTHLY, QUARTERLY) and `StandingOrderStatus` (ACTIVE, CANCELLED, LOCKED, TERMINATED) in `backend/src/main/java/com/fdm/banking/entity/`
- [x] T045 [P] [US2] Implement `StandingOrderEntity` (`@Entity`, table `standing_orders`) with all columns and `@PrePersist`/`@PreUpdate` lifecycle hooks as defined in `data-model.md` in `backend/src/main/java/com/fdm/banking/entity/StandingOrderEntity.java`
- [x] T046 [US2] Implement `StandingOrderRepository` with: `findBySourceAccountId`; `findByStatusAndNextRunDate`; duplicate-check query `findBySourceAccountIdAndPayeeAccountAndAmountAndFrequencyAndStatus` in `backend/src/main/java/com/fdm/banking/repository/StandingOrderRepository.java`
- [x] T047 [P] [US2] Create `CreateStandingOrderRequest` DTO with Bean Validation: `@NotBlank`, `@Size` on payeeName and reference; `@NotNull @Positive` on amount; `@NotNull` on frequency; `@ValidPayeeAccount` on payeeAccount (triggers Mod97); `@FutureOrPresent` + custom 24h validator on startDate in `backend/src/main/java/com/fdm/banking/dto/request/CreateStandingOrderRequest.java`
- [x] T048 [P] [US2] Create `StandingOrderResponse`, `StandingOrderListResponse`, `CancelStandingOrderResponse` DTOs in `backend/src/main/java/com/fdm/banking/dto/response/`
- [x] T049 [P] [US2] Create `StandingOrderMapper` (entity → response DTO, request DTO → entity) in `backend/src/main/java/com/fdm/banking/mapper/StandingOrderMapper.java`
- [x] T050 [US2] Implement `StandingOrderService.create(long accountId, CreateStandingOrderRequest req, UserPrincipal caller)`: assert `SO:CREATE` + ownership; validate Modulo 97 (already enforced by `@ValidPayeeAccount` but check again in service for defence-in-depth); validate `startDate >= now + 24h`; validate `req.getAmount().compareTo(account.getDailyTransferLimit()) <= 0` (throw `SemanticValidationException` → 422 with `field=amount`, code `ERR_AMOUNT_EXCEEDS_LIMIT` if exceeded); check `findBySourceAccountIdAndPayeeAccountAndAmountAndFrequencyAndStatus` for ACTIVE duplicate → 409 ERR_SO_DUPLICATE if found; calculate `nextRunDate` via `CanadianHolidayService.nextBusinessDay`; persist `StandingOrderEntity` with status ACTIVE; write audit log; return 201 StandingOrderResponse in `backend/src/main/java/com/fdm/banking/service/StandingOrderService.java`
- [x] T051 [US2] Implement `StandingOrderService.list(long accountId, UserPrincipal caller)`: assert `SO:READ` + ownership; fetch by `sourceAccountId`; return `StandingOrderListResponse` with count; write audit log in `backend/src/main/java/com/fdm/banking/service/StandingOrderService.java`
- [x] T052 [US2] Implement `StandingOrderService.cancel(String standingOrderId, UserPrincipal caller)`: assert `SO:CANCEL`; fetch entity (404 if not found); assert ownership on `sourceAccountId`; if `nextRunDate` is within 24 hours of now throw `LockException` → 403 ERR_SO_LOCKED; set status CANCELLED; persist; write audit log; return `CancelStandingOrderResponse` in `backend/src/main/java/com/fdm/banking/service/StandingOrderService.java`
- [x] T053 [US2] Implement `StandingOrderController` (`POST /accounts/{accountId}/standing-orders` → 201, `GET /accounts/{accountId}/standing-orders` → 200, `DELETE /standing-orders/{standingOrderId}` → 200) in `backend/src/main/java/com/fdm/banking/controller/StandingOrderController.java`
- [x] T054 [US2] Implement `StandingOrderExecutionJob`: `@Scheduled(cron = "1 0 0 * * *", zone = "UTC") void processOrders()` — fetch ACTIVE orders with `nextRunDate = today (UTC)`; for each order call `attemptExecution`; `@Scheduled(cron = "0 0 8 * * *") void firstAttempt()` — execute RETRY_PENDING orders; `@Scheduled(cron = "0 0 16 * * *") void finalAttempt()` — execute remaining RETRY_PENDING; on final failure persist FAILED TRANSFER transaction record and call `NotificationEvaluationService.evaluate` with `eventType=StandingOrderFailure` in `backend/src/main/java/com/fdm/banking/scheduler/StandingOrderExecutionJob.java`
- [x] T055 [US2] Implement `StandingOrderExecutionJob.attemptExecution(StandingOrderEntity order)`: generate per-cycle idempotency key (`standingOrderId + ":" + nextRunDate.toLocalDate()`); check `IdempotencyRecordRepository` for existing record with that key — if found return original outcome without new transaction; if not found check balance sufficiency; on success create `TransactionEntity` (type=TRANSFER, status=SUCCESS), store idempotency record, advance `nextRunDate` via `CanadianHolidayService`, write audit log; on insufficiency mark order RETRY_PENDING or FAILED_INSUFFICIENT_FUNDS in `backend/src/main/java/com/fdm/banking/scheduler/StandingOrderExecutionJob.java`
- [x] T056 [P] [US2] Unit test `StandingOrderService`: valid creation → 201 with ACTIVE status; Modulo 97 failure → 400 `field=payeeAccount`; startDate < 24h → 400 `field=startDate`; amount exceeds `dailyTransferLimit` → 422 `field=amount` ERR_AMOUNT_EXCEEDS_LIMIT; amount exactly equal to `dailyTransferLimit` → 201 (boundary passes); duplicate ACTIVE order → 409 ERR_SO_DUPLICATE; list empty account → 200 with count 0; cancel > 24h before nextRunDate → 200 CANCELLED; cancel within 24h → 403 ERR_SO_LOCKED; caller without `SO:CREATE` → 401; CUSTOMER on foreign account → 401 in `backend/src/test/java/com/fdm/banking/service/StandingOrderServiceTest.java`
- [x] T057 [P] [US2] Unit test `StandingOrderExecutionJob`: successful execution creates TRANSFER transaction and advances nextRunDate; duplicate trigger for same cycle returns original outcome without second transaction; insufficient funds at first attempt → RETRY_PENDING; insufficient funds at final attempt → FAILED transaction created + notification triggered; nextRunDate on bank holiday shifted to next business day in `backend/src/test/java/com/fdm/banking/scheduler/StandingOrderExecutionJobTest.java`
- [x] T058 [US2] Integration test `StandingOrderController` (MockMvc, H2): POST with valid body → 201; POST with invalid payeeAccount → 400 ERR_CHECKSUM_FAILURE; POST with startDate < 24h → 400 ERR_START_DATE_TOO_SOON; POST with amount exceeding `dailyTransferLimit` → 422 ERR_AMOUNT_EXCEEDS_LIMIT; POST duplicate → 409 ERR_SO_DUPLICATE; DELETE within lock window → 403 ERR_SO_LOCKED; GET returns ordered list in `backend/src/test/java/com/fdm/banking/controller/StandingOrderControllerTest.java`
- [x] T059 [P] [US2] Implement `standingOrderApi.js`: `createStandingOrder(accountId, payload)` (POST), `listStandingOrders(accountId)` (GET), `cancelStandingOrder(standingOrderId)` (DELETE) in `frontend/src/api/standingOrderApi.js`
- [x] T060 [P] [US2] Implement `useStandingOrders(accountId)` (`useQuery`, key: `['standingOrders', accountId]`), `useCreateStandingOrder()` (`useMutation`, on success invalidate `['standingOrders', accountId]`), `useCancelStandingOrder()` (`useMutation`, on success invalidate `['standingOrders', accountId]`) in `frontend/src/hooks/`
- [x] T061 [US2] Implement `StandingOrderItem.jsx` (renders order fields; cancel button disabled and shows tooltip when within 24h of `nextRunDate`; cancel button enabled otherwise; calls `useCancelStandingOrder` on click) in `frontend/src/components/standingorders/StandingOrderItem.jsx`
- [x] T062 [US2] Implement `StandingOrderList.jsx` (renders list of `StandingOrderItem`; renders empty state when count = 0) in `frontend/src/components/standingorders/StandingOrderList.jsx`
- [x] T063 [US2] Implement `CreateStandingOrderForm.jsx` (controlled form; all fields from spec; submit disabled until required fields present; renders server `ErrorResponse` field-level errors below each field; calls `useCreateStandingOrder` on submit) in `frontend/src/components/standingorders/CreateStandingOrderForm.jsx`
- [x] T064 [US2] Implement `StandingOrdersPage.jsx` (renders `CreateStandingOrderForm` + `StandingOrderList`; passes `accountId` from route params) in `frontend/src/components/standingorders/StandingOrdersPage.jsx`
- [x] T065 [P] [US2] Jest + RTL test `StandingOrderItem`: cancel button disabled when nextRunDate within 24h; enabled and calls mutation when > 24h; renders all expected fields in `frontend/src/tests/components/StandingOrderItemTest.jsx`
- [x] T066 [P] [US2] Jest + RTL test `CreateStandingOrderForm`: submit button disabled when required fields empty; displays field-level error when server returns 400 with `field` in `frontend/src/tests/components/CreateStandingOrderFormTest.jsx`
- [x] T067 [P] [US2] Jest test `useCreateStandingOrder` and `useCancelStandingOrder`: confirm correct endpoint called; invalidation triggered on success in `frontend/src/tests/hooks/useStandingOrdersTest.js`

---

## Phase 5 — US-03: Evaluate Notification Event

*Story goal*: Allow-listed upstream services (authenticated via mTLS or API Key) can submit business events for evaluation. Mandatory events always produce a RAISED decision; optional events depend on customer opt-in stored in `notification_preferences`. Every evaluation produces exactly one persisted `NotificationDecisionEntity`. Duplicate `eventId` is rejected with 409.

**Independent test criteria**: POST with valid mandatory event → 200 RAISED, `mandatoryOverride=true` when customer opted out; POST with valid optional event and opted-in customer → 200 RAISED; POST with valid optional event and opted-out customer → 200 SUPPRESSED; POST with duplicate eventId → 409; POST with unknown eventType → 422; POST from unauthenticated caller → 401. `NotificationDecisionRepository` contains exactly one record per unique eventId.

- [x] T121 [P] [US3] Implement `NotificationPreferenceEntity` (`@Entity`, table `notification_preferences`; columns: `preference_id` BIGINT PK AUTO_INCREMENT, `customer_id` BIGINT NOT NULL FK → `customers.customer_id`, `event_type` VARCHAR(100) NOT NULL, `opted_in` BOOLEAN NOT NULL DEFAULT true, `updated_at` DATETIME NOT NULL; unique constraint on `(customer_id, event_type)`) in `backend/src/main/java/com/fdm/banking/entity/NotificationPreferenceEntity.java`
- [x] T122 [US3] Implement `NotificationPreferenceRepository` with `findByCustomerIdAndEventType(Long customerId, String eventType)` and `findAllByCustomerId(Long customerId)` in `backend/src/main/java/com/fdm/banking/repository/NotificationPreferenceRepository.java`

- [x] T068 [P] [US3] Define `NotificationDecision` enum (RAISED, GROUPED, SUPPRESSED) in `backend/src/main/java/com/fdm/banking/entity/NotificationDecision.java`
- [x] T069 [P] [US3] Implement `NotificationDecisionEntity` (`@Entity`, table `notification_decisions`) with all columns as defined in `data-model.md` in `backend/src/main/java/com/fdm/banking/entity/NotificationDecisionEntity.java`
- [x] T070 [US3] Implement `NotificationDecisionRepository` with `existsByEventId(String eventId)` in `backend/src/main/java/com/fdm/banking/repository/NotificationDecisionRepository.java`
- [x] T071 [P] [US3] Create `NotificationEventRequest` DTO with Bean Validation: `@NotBlank` on `eventId` and `eventType`; `@NotNull @Positive` on `accountId` and `customerId`; `@NotBlank` on `businessTimestamp`; `payload` optional in `backend/src/main/java/com/fdm/banking/dto/request/NotificationEventRequest.java`
- [x] T072 [P] [US3] Create `NotificationDecisionResponse` DTO (fields: `eventId`, `decision`, `decisionReason`, `customerId`, `accountId`, `evaluatedAt`, `mandatoryOverride`) in `backend/src/main/java/com/fdm/banking/dto/response/NotificationDecisionResponse.java`
- [x] T073 [P] [US3] Create `NotificationDecisionMapper` (entity → response DTO) in `backend/src/main/java/com/fdm/banking/mapper/NotificationDecisionMapper.java`
- [x] T074 [US3] Implement `NotificationEvaluationService.evaluate(NotificationEventRequest req)`: check `existsByEventId(req.eventId)` → 409 ERR_DUPLICATE_EVENT if true; validate `eventType` against classification matrix (StandingOrderFailure, StatementAvailability, UnusualAccountActivity, StandingOrderCreation) → 422 ERR_UNKNOWN_EVENT_TYPE if unknown; validate `customerId` is linked to `accountId` → 422 ERR_CUSTOMER_ACCOUNT_MISMATCH if not; determine mandatory vs optional classification; **if mandatory**: call `NotificationPreferenceRepository.findByCustomerIdAndEventType` — if record found with `opted_in=false` set `mandatoryOverride=true` (decision is RAISED regardless); **if optional**: call `NotificationPreferenceRepository.findByCustomerIdAndEventType` — if absent or `opted_in=true` decision RAISED; if `opted_in=false` decision SUPPRESSED; check NOTIFICATION:READ entitlement for optional events; persist `NotificationDecisionEntity`; write audit log; return `NotificationDecisionResponse` in `backend/src/main/java/com/fdm/banking/service/NotificationEvaluationService.java`
- [x] T075 [US3] Implement `NotificationController` (`POST /notifications/evaluate` → 200 `NotificationDecisionResponse`; security context populated by `ServiceApiKeyFilter`) in `backend/src/main/java/com/fdm/banking/controller/NotificationController.java`
- [x] T076 [P] [US3] Unit test `NotificationEvaluationService`: duplicate eventId → 409; unknown eventType → 422; customerId not linked to accountId → 422; mandatory event with preference record `opted_in=false` → RAISED with `mandatoryOverride=true`; mandatory event with no preference record → RAISED with `mandatoryOverride=false`; optional event with preference record `opted_in=true` → RAISED; optional event with no preference record → RAISED (default open); optional event with preference record `opted_in=false` → SUPPRESSED; one NotificationDecisionEntity persisted per evaluation in `backend/src/test/java/com/fdm/banking/service/NotificationEvaluationServiceTest.java`
- [x] T077 [US3] Integration test `NotificationController` (MockMvc, H2): valid mandatory event with API Key header → 200 RAISED; mandatory event with opted-out customer preference record → 200 RAISED with `mandatoryOverride=true`; optional event with opted-out customer preference record → 200 SUPPRESSED; duplicate eventId → 409; unknown eventType → 422; no API Key header → 401; malformed request body → 400 in `backend/src/test/java/com/fdm/banking/controller/NotificationControllerTest.java`

---

## Phase 6 — US-04: Get Monthly Statement

*Story goal*: CUSTOMER with `STATEMENT:READ` can retrieve the formal monthly statement for a closed period from their own account, with optional version selection. Corrections produce new versions; originals are never overwritten. Empty months return a statement with zero totals. Periods beyond 7-year retention return 410.

**Independent test criteria**: GET for a closed period → 200 with all required fields; GET for open period → 409 ERR_PERIOD_NOT_CLOSED; GET for period older than 7 years → 410; GET with specific version → correct version returned; GET with no statement for period → 404. Permission re-validated at delivery. `MonthlyStatementServiceTest` green.

- [x] T078 [P] [US4] Implement `MonthlyStatementEntity` (`@Entity`, table `monthly_statements`, unique constraint on `(account_id, period, version_number)`) with all columns from `data-model.md` in `backend/src/main/java/com/fdm/banking/entity/MonthlyStatementEntity.java`
- [x] T079 [US4] Implement `MonthlyStatementRepository` with: `findTopByAccountIdAndPeriodOrderByVersionNumberDesc`; `findByAccountIdAndPeriodAndVersionNumber` in `backend/src/main/java/com/fdm/banking/repository/MonthlyStatementRepository.java`
- [x] T080 [P] [US4] Create `MonthlyStatementResponse` DTO (fields: `accountId`, `period`, `openingBalance`, `closingBalance`, `totalMoneyIn`, `totalMoneyOut`, `transactions` list of `TransactionItemResponse`, `versionNumber`, `correctionSummary`, `generatedAt`) in `backend/src/main/java/com/fdm/banking/dto/response/MonthlyStatementResponse.java`
- [x] T081 [P] [US4] Create `MonthlyStatementMapper` (entity → response DTO; deserialises `transactionsJson` to `List<TransactionItemResponse>`) in `backend/src/main/java/com/fdm/banking/mapper/MonthlyStatementMapper.java`
- [x] T082 [US4] Implement `MonthlyStatementService.getStatement(long accountId, String period, Integer version, UserPrincipal caller)`: assert `STATEMENT:READ` permission (re-validate at this point, not only at controller level); assert ownership; validate `period` format YYYY-MM (else 400); validate period is before current month (else 409 ERR_PERIOD_NOT_CLOSED); check retention window — if period is more than 7 years before today throw `RetentionWindowException` → 410; resolve statement: if `version` supplied use `findByAccountIdAndPeriodAndVersionNumber` (404 if not found); else use `findTopByAccountIdAndPeriodOrderByVersionNumberDesc` (404 if absent); deserialise and return `MonthlyStatementResponse`; write audit log in `backend/src/main/java/com/fdm/banking/service/MonthlyStatementService.java`
- [x] T083 [US4] Implement `StatementController` (`GET /accounts/{accountId}/statements/{period}?version={version}` → 200 `MonthlyStatementResponse`) in `backend/src/main/java/com/fdm/banking/controller/StatementController.java`
- [x] T084 [P] [US4] Unit test `MonthlyStatementService`: closed period → 200; open period → 409 ERR_PERIOD_NOT_CLOSED; period > 7 years → 410; specific version returned when version param provided; version not found → 404; no statement for period → 404; empty month → 200 with zero totals; permission re-validated at delivery (mock removes permission between controller and service call); corrected version returns correctionSummary in `backend/src/test/java/com/fdm/banking/service/MonthlyStatementServiceTest.java`
- [x] T085 [US4] Integration test `StatementController` (MockMvc, H2): 200 with full JSON for valid request; 409 for open period; 410 for expired period; 401 for missing STATEMENT:READ; 404 for nonexistent account; 400 for invalid period format in `backend/src/test/java/com/fdm/banking/controller/StatementControllerTest.java`
- [x] T086 [P] [US4] Implement `statementApi.js`: `getMonthlyStatement(accountId, period, version)` (GET with `version` as optional query param) in `frontend/src/api/statementApi.js`
- [x] T087 [P] [US4] Implement `useMonthlyStatement(accountId, period, version)` (`useQuery`, key: `['statement', accountId, period, version]`) in `frontend/src/hooks/useMonthlyStatement.js`
- [x] T088 [US4] Implement `StatementViewer.jsx` (renders `openingBalance`, `closingBalance`, `totalMoneyIn`, `totalMoneyOut`, `versionNumber`; renders `correctionSummary` section only when `versionNumber > 1` and `correctionSummary` is non-null; renders `transactions` table with all fields) in `frontend/src/components/statements/StatementViewer.jsx`
- [x] T089 [US4] Implement `MonthlyStatementPage.jsx` (period input in YYYY-MM format; optional version input; renders `LoadingSpinner`, `ErrorMessage`, or `StatementViewer`) in `frontend/src/components/statements/MonthlyStatementPage.jsx`
- [x] T090 [P] [US4] Jest + RTL test `StatementViewer`: renders all fields; `correctionSummary` visible when versionNumber > 1; hidden on version 1; transactions table renders correct row count in `frontend/src/tests/components/StatementViewerTest.jsx`
- [x] T091 [P] [US4] Jest test `useMonthlyStatement`: correct query key including version; returns data on success; exposes error on 410 response in `frontend/src/tests/hooks/useMonthlyStatementTest.js`

---

## Phase 7 — US-05: Spending Insights

*Story goal*: CUSTOMER with `INSIGHTS:READ` can retrieve a categorised spending breakdown (8 fixed categories) and a 6-month trend for their own account. Percentages sum to exactly 100. Manual recategorisation updates the breakdown in real time. DEPOSIT and FAILED transactions are always excluded.

**Independent test criteria**: GET for a month with eligible transactions → 200 with exactly 8 categories summing to 100%; GET for an empty month → 200 with all 8 zero categories; GET for a future month → 409; PUT with invalid category → 422 `field=category`; PUT with valid category → 200 with updated breakdown still summing to 100%. `SpendingInsightServiceTest` green.

> **CategoryResolver configuration** — Keywords are defined in `application.yml` under `banking.categories`. Matching is case-insensitive substring search on the transaction `description` field. First match wins. Returns `null` if no keyword matches (= uncategorised). The spec requires `hasUncategorised=true` when any eligible transaction has a null category after resolution — never assign a default fallback category.

- [x] T092 [US5] Add nullable `category` column (`VARCHAR(50)`, nullable) to Group 2 `TransactionEntity`; coordinate with Group 2 team to add this column via a Flyway/Liquibase migration script; add the `@Column` annotation in the entity; do not change any other field on `TransactionEntity` in `backend/src/main/java/com/fdm/banking/entity/TransactionEntity.java` *(Group 2 coordination required)*
- [x] T093 [P] [US5] Implement `CategoryResolver` using case-insensitive substring keyword-matching against `banking.categories` map injected from `application.yml`; `resolve(String description)` iterates the 8 categories in declaration order, returns the first matching category string or `null` for no match; define the following initial keyword config in `application.yml`: `Housing: [rent, mortgage, lease, landlord, condo, property, housing]`; `Transport: [uber, lyft, taxi, transit, fuel, gas, parking, via rail, greyhound, air canada, westjet]`; `"Food & Drink": [restaurant, cafe, coffee, starbucks, tim hortons, mcdonalds, grocery, loblaws, sobeys, metro, pizza, sushi, bar, pub]`; `Entertainment: [netflix, spotify, disney, cinema, movie, theatre, concert, ticket, steam, playstation, xbox]`; `Shopping: [amazon, ebay, walmart, zara, h&m, clothing, shoes, mall, online shop]`; `Utilities: [hydro, electricity, internet, rogers, bell, telus, shaw, heating, utility]`; `Health: [pharmacy, medical, doctor, dentist, hospital, clinic, gym, fitness, yoga, prescription]`; `Income: [salary, payroll, direct deposit, refund, cashback, interest, dividend]`; `null` means uncategorised — **do not assign a default category** in `backend/src/main/java/com/fdm/banking/util/CategoryResolver.java`
- [x] T094 [P] [US5] Create `SpendingInsightResponse` DTO (fields: `accountId`, `period` object with `year`/`month`/`isComplete`, `totalDebitSpend`, `transactionCount`, `hasUncategorised`, `hasExcludedDisputes`, `dataFresh`, `categoryBreakdown` list of 8 `CategoryBreakdownItem`, `topTransactions` list of up to 5, `sixMonthTrend` list of exactly 6) in `backend/src/main/java/com/fdm/banking/dto/response/SpendingInsightResponse.java`
- [x] T095 [P] [US5] Create `RecategoriseRequest` DTO with `@NotBlank String category`; no enum annotation — validation to one of 8 values is done in service layer to allow proper 422 response in `backend/src/main/java/com/fdm/banking/dto/request/RecategoriseRequest.java`
- [x] T096 [P] [US5] Create `RecategoriseResponse` DTO (fields: `transactionId`, `previousCategory`, `updatedCategory`, `updatedTotalDebitSpend`, `updatedCategoryBreakdown` list of 8 `CategoryBreakdownItem`) in `backend/src/main/java/com/fdm/banking/dto/response/RecategoriseResponse.java`
- [x] T097 [US5] Implement `SpendingInsightService.getInsights(long accountId, int year, int month, UserPrincipal caller)`: assert `INSIGHTS:READ` + ownership; validate year and month (1–12); validate that requested year/month is not a future month (else 409 ERR_FUTURE_MONTH); compute month start (inclusive) and month end (exclusive) as `LocalDateTime`; fetch eligible transactions: `accountId`, `type IN (WITHDRAW, TRANSFER)`, `status = SUCCESS`, `timestamp` in month range; auto-categorise uncategorised transactions via `CategoryResolver` (write `category` back only if null — do not overwrite manual overrides); aggregate by category for total amounts; compute percentages — **all 8 categories must appear; if totalDebitSpend is zero set all percentages to 0.00 (not divide-by-zero)**; compute `sixMonthTrend` for the 6 months ending at the requested month (always 6 entries; zero for months with no eligible transactions; set `accountExisted=false` for months before account `createdAt`); set `hasUncategorised=true` if any eligible transaction has null category after resolution; assemble and return `SpendingInsightResponse`; write audit log in `backend/src/main/java/com/fdm/banking/service/SpendingInsightService.java`
- [x] T098 [US5] Implement `SpendingInsightService.recategorise(long accountId, long transactionId, String category, UserPrincipal caller)`: assert `INSIGHTS:READ` + ownership; validate `category` is exactly one of the 8 agreed values: Housing, Transport, Food & Drink, Entertainment, Shopping, Utilities, Health, Income (case-sensitive; else throw `SemanticValidationException` → 422 `field=category`); fetch transaction (404 if not found); record `previousCategory = transaction.getCategory()`; set `transaction.category = category`; persist; re-run category aggregation for the transaction's month (same logic as `getInsights` without sixMonthTrend); return `RecategoriseResponse` with updated breakdown; write audit log in `backend/src/main/java/com/fdm/banking/service/SpendingInsightService.java`
- [x] T099 [US5] Implement `InsightController` (`GET /accounts/{accountId}/insights?year&month` → 200; `PUT /accounts/{accountId}/transactions/{transactionId}/category` → 200 or 422) in `backend/src/main/java/com/fdm/banking/controller/InsightController.java`
- [x] T100 [P] [US5] Unit test `CategoryResolver`: keyword match assigns correct category; unrecognised description → null; empty description → null; case-insensitive keyword match in `backend/src/test/java/com/fdm/banking/util/CategoryResolverTest.java`
- [x] T101 [P] [US5] Unit test `SpendingInsightService`: all 8 categories returned for month with activity; percentages sum to 100; empty month → 8 zero categories, totalDebitSpend=0.00; future month → 409; DEPOSIT transactions excluded from totals; FAILED transactions excluded from totals; hasUncategorised=true when any transaction has null category post-resolution; sixMonthTrend always 6 entries; recategorise valid category → updatedCategory reflected in breakdown, percentages still sum to 100; recategorise invalid category → 422 field=category; caller without INSIGHTS:READ → 401; CUSTOMER on foreign account → 401 in `backend/src/test/java/com/fdm/banking/service/SpendingInsightServiceTest.java`
- [x] T102 [US5] Integration test `InsightController` (MockMvc, H2): GET with eligible transactions → 200 with 8 categories; GET empty month → 200 with zeros; GET future month → 409; PUT valid category → 200 with updated breakdown; PUT invalid category → 422 `field=category`; 401 without INSIGHTS:READ in `backend/src/test/java/com/fdm/banking/controller/InsightControllerTest.java`
- [x] T103 [P] [US5] Implement `insightApi.js`: `getSpendingInsights(accountId, year, month)` (GET); `recategoriseTransaction(accountId, transactionId, category)` (PUT) in `frontend/src/api/insightApi.js`
- [x] T104 [P] [US5] Implement `useSpendingInsights(accountId, year, month)` (`useQuery`, key: `['insights', accountId, year, month]`) and `useRecategorise(accountId, year, month)` (`useMutation`, on success invalidate `['insights', accountId, year, month]`) in `frontend/src/hooks/`
- [x] T105 [P] [US5] Implement `CategoryDonutChart.jsx` (receives `categoryBreakdown` array of 8 items; renders all 8 segments including zero-value; shows category name and percentage label) in `frontend/src/components/insights/CategoryDonutChart.jsx`
- [x] T106 [P] [US5] Implement `SixMonthBarChart.jsx` (receives `sixMonthTrend` array of exactly 6 items; renders all 6 bars; zero-height bar for months with zero spend; visual indicator for `isComplete=false` on current month) in `frontend/src/components/insights/SixMonthBarChart.jsx`
- [x] T107 [US5] Implement `TransactionCategoryRow.jsx` (renders transaction `description`, `amount`, current `category`; presents dropdown with exactly 8 category options; on selection calls `useRecategorise`; shows pending state during mutation) in `frontend/src/components/insights/TransactionCategoryRow.jsx`
- [x] T108 [US5] Implement `SpendingInsightsPage.jsx` (year and month picker; renders `CategoryDonutChart`, `SixMonthBarChart`, and list of `TransactionCategoryRow` for top transactions; renders `LoadingSpinner` and `ErrorMessage` appropriately) in `frontend/src/components/insights/SpendingInsightsPage.jsx`
- [x] T109 [P] [US5] Jest + RTL test `CategoryDonutChart`: always renders 8 segments; zero-value category label shows "0%" not error; total percentage label equals 100% in `frontend/src/tests/components/CategoryDonutChartTest.jsx`
- [x] T110 [P] [US5] Jest + RTL test `SixMonthBarChart`: always renders 6 bars; zero-spend month renders 0-height bar; in-progress month has visual indicator in `frontend/src/tests/components/SixMonthBarChartTest.jsx`
- [x] T111 [P] [US5] Jest + RTL test `TransactionCategoryRow`: dropdown has exactly 8 options; selecting new category calls `useRecategorise`; shows loading state during mutation in `frontend/src/tests/components/TransactionCategoryRowTest.jsx`
- [x] T112 [P] [US5] Jest test `useRecategorise`: calls correct PUT endpoint with category; invalidates insight query on success in `frontend/src/tests/hooks/useRecategoriseTest.js`

---

## Phase 8 — Polish and Cross-Cutting Concerns

*Goal*: Idempotency purge, audit wiring, and final contract validation across all five stories.

**Independent test criteria**: All 9 endpoints contract assertions green. `IdempotencyPurgeJob` purge test passes. CI pipeline runs clean end-to-end.

- [x] T113 [P] Implement `IdempotencyPurgeJob`: `@Scheduled(cron = "0 0 3 * * *", zone = "UTC") void purge()` deletes `IdempotencyRecordRepository` records older than 72 hours and `ExportCacheRepository` records older than 72 hours; write audit log entry for purge operation in `backend/src/main/java/com/fdm/banking/scheduler/IdempotencyPurgeJob.java`
- [x] T114 [P] Verify `AuditService.log()` is called from all five service classes for every operation (success and failure paths); add any missing call sites identified by reviewing each service method in `backend/src/main/java/com/fdm/banking/service/`
- [x] T115 End-to-end contract check: for each of the 9 endpoints verify HTTP method, path, success status code, error status codes, error code strings, and response schema match `contracts/` documents exactly; create contract assertion tests in `backend/src/test/java/com/fdm/banking/controller/ContractValidationTest.java`
- [x] T116 [P] Confirm all `ErrorResponse` bodies across all integration tests use `code`, `message`, and `field` (when applicable) — no other shape; fix any divergence found in `backend/src/test/java/com/fdm/banking/controller/`
- [x] T117 [P] Run `npm run lint` and `npm test -- --run` on frontend; resolve any failing tests or lint errors in `frontend/`
- [x] T118 [P] Run `./mvnw test` backend full suite; resolve any failures; confirm service layer coverage ≥ 80% via JaCoCo report in `backend/`

---

## Dependencies

```
Phase 1 (T001–T006)
  └─► Phase 2 (T007–T023)
        ├─► Phase 3 — US-01 (T119, T024–T043)  [T119 must complete before T025; others start after T007–T023]
        ├─► Phase 4 — US-02 (T120, T044–T067)  [T120 must complete before T050; others start after T007–T023]
        │     └─► T054, T055 depend on T074 (NotificationEvaluationService) for failure notification
        ├─► Phase 5 — US-03 (T121, T122, T068–T077)  [T121–T122 must complete before T074]
        ├─► Phase 6 — US-04 (T078–T091)  [can start after T007–T023 complete]
        └─► Phase 7 — US-05 (T092–T112)
              └─► T092 (category column) depends on Group 2 coordination
                    [remaining US-05 tasks can start after T007–T023 + T092 complete]

Phase 8 (T113–T118) ─► depends on all Phase 3–7 tasks complete
```

**Cross-story dependency**: US-02 scheduler (T054) calls `NotificationEvaluationService.evaluate()` directly. US-03 service (T074) must be implemented before the scheduler's failure notification path can be wired.

### Parallel Execution Examples

**US-01 once Phase 2 complete**:
- T119 runs first (Group 2 coordination for PENDING status)
- T024, T025, T033, T034, T035 run in parallel once T119 complete
- T026, T027 follow T024/T025
- T028 follows T027
- T029 follows T028; T032 follows T029
- Frontend tasks T036–T039 run in parallel after T033–T035

**US-02 once Phase 2 complete**:
- T120 runs first (Group 2 coordination for dailyTransferLimit)
- T044, T045, T047, T048, T049 run in parallel once T120 complete
- T046 follows T045; T050 follows T045+T046
- T051 depends on T050; T052 depends on T050
- Frontend T059–T063 run in parallel after T044/T047/T048

**US-03 once Phase 2 complete**:
- T121 (NotificationPreferenceEntity) then T122 (NotificationPreferenceRepository)
- T068, T069, T071, T072, T073 run in parallel once T122 complete
- T070, T074 follow T069+T122

**US-05 once T092 complete**:
- T093, T094, T095, T096 run in parallel
- T097 follows T093+T094+T095; T098 follows T097
- Frontend T103–T108 run in parallel after T094+T095+T096

---

## Implementation Strategy

**MVP scope (deliver first)**:
1. Phase 1 + Phase 2 complete — foundation for all stories
2. US-01 backend complete (T024–T032) — transaction history endpoint working end-to-end with H2
3. US-03 backend complete (T068–T077) — notification evaluation independent of other stories

**Incremental order**:
- Sprint 1: Phase 1 + Phase 2 + US-01 backend
- Sprint 2: US-02 backend + scheduler + US-04 backend
- Sprint 3: US-03 backend + US-05 backend
- Sprint 4: All frontend components and hooks
- Sprint 5: Tests, contract checks, Polish (Phase 8)

---

## Summary

| Metric | Count |
|---|---|
| Total tasks | 122 |
| Setup tasks | 6 |
| Foundational tasks | 17 |
| US-01 tasks | 21 (incl. T119) |
| US-02 tasks | 25 (incl. T120) |
| US-03 tasks | 12 (incl. T121, T122) |
| US-04 tasks | 14 |
| US-05 tasks | 21 |
| Polish tasks | 6 |
| Parallelizable [P] tasks | 70 |
| Spec/Plan gaps resolved | 4 (GAP-1 through GAP-4 — all closed) |
| Blocking gaps remaining | 0 |
| Group 2 coordination tasks | 3 (T119, T120, T092) |

**Format validation**: All 122 tasks follow the checklist format `- [x] T{nnn} [P]? [USn]? description with file path`. No tasks without IDs, no tasks without file paths in user story phases. All four gaps resolved — no stub TODO markers remain.
