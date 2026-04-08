# Tasks: Banking API (Spring Boot + React)

**Input**: Design documents from `specs/`
**Prerequisites**: plan.md ✅, spec.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no blocking dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths are included in every task description

## User Story Map

| Story | Operations | Priority |
|-------|-----------|----------|
| US1 | Create Account, Retrieve Account Details, List Customer Accounts | P1 — MVP |
| US2 | Update Account, Delete Account, Delete Customer | P2 |
| US3 | Deposit, Withdraw, Transfer Funds | P3 |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and directory scaffolding

- [ ] T001 Create Maven Spring Boot 3.x project in `backend/` with `pom.xml` declaring spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, flyway-core, mysql-connector-j, h2, and jjwt
- [ ] T002 Create Vite + React 18 + JavaScript project in `frontend/` with `package.json` declaring react-query v5, axios, react-router-dom v6
- [ ] T003 [P] Configure `backend/src/main/resources/application.yml` with MySQL runtime datasource settings, an H2 local/test profile, Flyway locations, JPA ddl-auto=validate, and server port
- [ ] T004 [P] Configure `frontend/vite.config.js` with dev server proxy to backend

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before any user story can start

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Create Flyway migration `backend/src/main/resources/db/migration/V1__init.sql` defining `customer` and `account` tables with deletion status columns (`deleted_at` or equivalent deletion field), `account` `version` column for optimistic locking, and `transaction` table with unique `idempotency_key`, using numeric(19,2) for monetary columns
- [ ] T006 Create Java enums `CustomerType`, `AccountType`, `TransactionType`, `TransactionStatus` in `backend/src/main/java/com/bank/model/`
- [ ] T007 [P] Create `Customer` JPA entity in `backend/src/main/java/com/bank/model/Customer.java` with all fields from spec including `@OneToMany accounts` and deletion status field(s)
- [ ] T008 [P] Create `Account` JPA entity with `@Version` field for optimistic locking and deletion status field(s) in `backend/src/main/java/com/bank/model/Account.java`
- [ ] T009 [P] Create `Transaction` JPA entity with unique constraint on `idempotencyKey` in `backend/src/main/java/com/bank/model/Transaction.java`
- [ ] T010 [P] Create `CustomerRepository` extending JpaRepository in `backend/src/main/java/com/bank/repository/CustomerRepository.java`
- [ ] T011 [P] Create `AccountRepository` with `findByCustomerId` query in `backend/src/main/java/com/bank/repository/AccountRepository.java`
- [ ] T012 [P] Create `TransactionRepository` with `findByIdempotencyKey` query in `backend/src/main/java/com/bank/repository/TransactionRepository.java`
- [ ] T013 Create `ErrorResponse` DTO and `BankingException` subclass hierarchy (NotFoundException, ConflictException, UnprocessableException) in `backend/src/main/java/com/bank/exception/`
- [ ] T014 Implement `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping NotFoundException→404, ConflictException→409, UnprocessableException→422, MethodArgumentNotValidException→400 using `ErrorResponse` in `backend/src/main/java/com/bank/exception/GlobalExceptionHandler.java`
- [ ] T015 Implement `SecurityConfig` with JWT `OncePerRequestFilter` validating Bearer token; unauthenticated requests return 401; add ownership authorization check returning 401 when authenticated caller does not own the requested customer or account in `backend/src/main/java/com/bank/config/SecurityConfig.java`
- [ ] T074 [P] Create RBAC schema support in `backend/src/main/resources/db/migration/` and `backend/src/main/java/com/bank/model/` for persisted roles, permissions, and user-role assignments keyed to the authenticated external principal identifier
- [ ] T075 [P] Create RBAC repositories and seed/default role-permission mappings (`CUSTOMER`, `ADMIN`, optional custom roles) in `backend/src/main/java/com/bank/repository/` and `backend/src/main/resources/`
- [ ] T076 Implement `AuthorizationService` in `backend/src/main/java/com/bank/service/AuthorizationService.java` to resolve effective permissions for the authenticated caller from JWT claims and/or persisted role assignments
- [ ] T077 Implement endpoint-level permission enforcement in `backend/src/main/java/com/bank/config/SecurityConfig.java` or `backend/src/main/java/com/bank/security/` so each route enforces the required permission(s) from the RBAC matrix before ownership checks
- [ ] T016 [P] Create JS shape constants and enum value objects for `Customer`, `Account`, `Transaction`, `ErrorResponse` mirroring spec domain objects in `frontend/src/types/index.js`
- [ ] T017 [P] Configure Axios instance with base URL and Bearer token auth request interceptor; handle 401 responses in `frontend/src/api/axiosClient.js`

**Checkpoint**: Foundation ready — all three user stories can now be implemented in parallel

---

## Phase 3: User Story 1 — Core Account Management (Priority: P1) 🎯 MVP

**Goal**: Users can create accounts for existing customers and retrieve or list account details.

**Independent Test**: `POST /customers/{id}/accounts` with valid SAVINGS body returns 201 with account details; `GET /accounts/{id}` returns 200 with the same account; `GET /customers/{id}/accounts` returns 200 with a non-empty list.

### Implementation for User Story 1

- [ ] T018 [US1] Create `CreateAccountRequest` (accountType, balance, interestRate) and `AccountResponse` DTOs in `backend/src/main/java/com/bank/dto/`
- [ ] T019 [US1] Implement `AccountService.createAccount()`: validate field-type compatibility (interestRate forbidden on CHECKING), save entity, return `AccountResponse` in `backend/src/main/java/com/bank/service/AccountService.java`
- [ ] T020 [P] [US1] Implement `AccountService.getAccount()`: load by accountId, throw NotFoundException if missing in `backend/src/main/java/com/bank/service/AccountService.java`
- [ ] T021 [P] [US1] Implement `AccountService.listCustomerAccounts()`: verify customer exists, return all accounts (empty list allowed) in `backend/src/main/java/com/bank/service/AccountService.java`
- [ ] T022 [US1] Implement `POST /customers/{customerId}/accounts` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 201/400/404/422
- [ ] T023 [P] [US1] Implement `GET /accounts/{accountId}` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404
- [ ] T024 [P] [US1] Implement `GET /customers/{customerId}/accounts` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404
- [ ] T025 [P] [US1] Unit tests for `AccountService.createAccount()` (positive + incompatible fields 422 + customer not found 404) in `backend/src/test/unit/AccountServiceTest.java`
- [ ] T026 [P] [US1] Unit tests for `AccountService.getAccount()` and `listCustomerAccounts()` (positive + 404 scenarios) in `backend/src/test/unit/AccountServiceTest.java`
- [ ] T027 [P] [US1] Controller and service JUnit/Mockito tests for POST/GET account flows covering all spec acceptance scenarios in `backend/src/test/unit/AccountControllerTest.java`
- [ ] T078 [P] [US1] Add authorization JUnit/Mockito tests for Create Account, Retrieve Account Details, and List Customer Accounts covering missing required permission and admin override behavior in `backend/src/test/unit/AccountControllerTest.java`
- [ ] T028 [P] [US1] Implement `useCreateAccount` React Query mutation hook in `frontend/src/hooks/useCreateAccount.js`
- [ ] T029 [P] [US1] Implement `useGetAccount` React Query query hook in `frontend/src/hooks/useGetAccount.js`
- [ ] T030 [P] [US1] Implement `useListCustomerAccounts` React Query query hook in `frontend/src/hooks/useListCustomerAccounts.js`
- [ ] T031 [P] [US1] Implement Create Account form page with accountType-conditional field handling for `interestRate` and inline ErrorResponse field errors in `frontend/src/pages/CreateAccountPage.jsx`
- [ ] T032 [P] [US1] Implement Account Detail page displaying all Account fields in `frontend/src/pages/AccountDetailPage.jsx`
- [ ] T033 [P] [US1] Implement Account List page displaying customer's accounts with links to detail page in `frontend/src/pages/AccountListPage.jsx`

**Checkpoint**: US1 complete — Create Account, Retrieve Account Details, and List Customer Accounts are fully functional and independently testable

---

## Phase 4: User Story 2 — Account & Customer Mutations (Priority: P2)

**Goal**: Users can update mutable account attributes and delete accounts (zero balance only) and customers (no active accounts only) while retaining underlying records.

**Independent Test**: `PUT /accounts/{id}` with valid `interestRate` on SAVINGS returns 200 with updated details; `DELETE /accounts/{id}` with balance=0 returns 200 and deletes the account; `DELETE /customers/{id}` with no accounts returns 200 and deletes the customer.

### Implementation for User Story 2

- [ ] T034 [US2] Create `UpdateAccountRequest` DTO (interestRate nullable) in `backend/src/main/java/com/bank/dto/UpdateAccountRequest.java`
- [ ] T035 [P] [US2] Implement `AccountService.updateAccount()`: reject type-incompatible fields with UnprocessableException, persist allowed field only in `backend/src/main/java/com/bank/service/AccountService.java`
- [ ] T036 [P] [US2] Implement `AccountService.deleteAccount()`: reject non-zero balance with ConflictException and perform deletion by setting the deletion status field while retaining the record in `backend/src/main/java/com/bank/service/AccountService.java`
- [ ] T037 [P] [US2] Implement `CustomerService.deleteCustomer()`: reject customer with active accounts with ConflictException and perform deletion by setting the deletion status field while retaining the record in `backend/src/main/java/com/bank/service/CustomerService.java`
- [ ] T038 [P] [US2] Implement `PUT /accounts/{accountId}` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/401/404/422
- [ ] T039 [P] [US2] Implement `DELETE /accounts/{accountId}` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404/409
- [ ] T040 [P] [US2] Implement `DELETE /customers/{customerId}` in `backend/src/main/java/com/bank/controller/CustomerController.java` returning 200/400/404/409
- [ ] T041 [P] [US2] Unit tests for `AccountService.updateAccount()` (positive + wrong-type field 422 + not found 404) in `backend/src/test/unit/AccountServiceTest.java`
- [ ] T042 [P] [US2] Unit tests for `AccountService.deleteAccount()` (positive zero-balance delete + non-zero 409 + not found 404) in `backend/src/test/unit/AccountServiceTest.java`
- [ ] T043 [P] [US2] Unit tests for `CustomerService.deleteCustomer()` (positive delete + has accounts 409 + not found 404) in `backend/src/test/unit/CustomerServiceTest.java`
- [ ] T044 [P] [US2] Controller and service JUnit/Mockito tests for PUT/DELETE account and DELETE customer flows covering all spec acceptance scenarios in `backend/src/test/unit/CustomerControllerTest.java`
- [ ] T079 [P] [US2] Add authorization JUnit/Mockito tests for Update Account and Delete Account in `backend/src/test/unit/AccountControllerTest.java` and for Delete Customer in `backend/src/test/unit/CustomerControllerTest.java`, covering missing required permission and ownership interaction
- [ ] T045 [P] [US2] Implement `useUpdateAccount` React Query mutation hook in `frontend/src/hooks/useUpdateAccount.js`
- [ ] T046 [P] [US2] Implement `useDeleteAccount` React Query mutation hook in `frontend/src/hooks/useDeleteAccount.js`
- [ ] T047 [P] [US2] Implement `useDeleteCustomer` React Query mutation hook in `frontend/src/hooks/useDeleteCustomer.js`
- [ ] T048 [P] [US2] Add Update Account form (type-conditional fields) and Delete Account button with confirmation and delete success messaging to `frontend/src/pages/AccountDetailPage.jsx`
- [ ] T049 [P] [US2] Implement Customer page with Delete Customer action, delete success messaging, and error display in `frontend/src/pages/CustomerPage.jsx`

**Checkpoint**: US2 complete — Update Account, Delete Account, and Delete Customer are fully functional and independently testable

---

## Phase 5: User Story 3 — Money Movement (Priority: P3)

**Goal**: Users can deposit to accounts, withdraw from accounts (balance permitting), and transfer funds between two different accounts atomically — with idempotency guarantees on all three operations.

**Independent Test**: `POST /accounts/{id}/deposit` with `amount=50.00` increases balance by 50.00 and records a SUCCESS transaction; second identical request with same `Idempotency-Key` returns original response without changing balance again; `POST /accounts/transfer` failure leaves both balances unchanged.

### Implementation for User Story 3

- [ ] T050 [US3] Create `DepositRequest`, `WithdrawRequest` (amount, description), and `TransferRequest` (fromAccountId, toAccountId, amount, description) DTOs in `backend/src/main/java/com/bank/dto/`
- [ ] T051 [US3] Implement `TransactionService.deposit()`: check idempotency key (return cached if found), validate amount > 0, update balance, persist Transaction with SUCCESS or FAILED status in `backend/src/main/java/com/bank/service/TransactionService.java`
- [ ] T052 [P] [US3] Implement `TransactionService.withdraw()`: check idempotency key, validate amount > 0, enforce balance ≥ amount (ConflictException otherwise), apply optimistic locking via `@Version`, persist Transaction in `backend/src/main/java/com/bank/service/TransactionService.java`
- [ ] T053 [P] [US3] Implement `TransactionService.transfer()`: `@Transactional`, check idempotency key, guard fromAccountId ≠ toAccountId (UnprocessableException), guard balance, atomically debit source and credit destination, persist two Transaction records in `backend/src/main/java/com/bank/service/TransactionService.java`
- [ ] T054 [US3] Implement `POST /accounts/{accountId}/deposit` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404/422
- [ ] T055 [P] [US3] Implement `POST /accounts/{accountId}/withdraw` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404/409/422
- [ ] T056 [P] [US3] Implement `POST /accounts/transfer` in `backend/src/main/java/com/bank/controller/AccountController.java` returning 200/400/404/409/422
- [ ] T057 [P] [US3] Unit tests for `TransactionService.deposit()`: positive balance update, amount=0 → 422, account not found → 404, duplicate idempotency key → original response in `backend/src/test/unit/TransactionServiceTest.java`
- [ ] T058 [P] [US3] Unit tests for `TransactionService.withdraw()`: positive balance debit, insufficient funds → 409 (balance unchanged), negative amount → 422, duplicate idempotency key → original response in `backend/src/test/unit/TransactionServiceTest.java`
- [ ] T059 [P] [US3] Unit tests for `TransactionService.transfer()`: positive atomicity, insufficient funds → 409 (both balances unchanged), same account → 422, duplicate idempotency key → original response in `backend/src/test/unit/TransactionServiceTest.java`
- [ ] T060 [P] [US3] Controller and service JUnit/Mockito tests for POST /deposit, /withdraw, and /transfer flows covering all spec acceptance scenarios in `backend/src/test/unit/TransactionControllerTest.java`
- [ ] T080 [P] [US3] Add authorization JUnit/Mockito tests for Deposit, Withdraw, and Transfer covering missing required permission and admin override behavior in `backend/src/test/unit/TransactionControllerTest.java`
- [ ] T061 [P] [US3] Implement `useDeposit` React Query mutation hook in `frontend/src/hooks/useDeposit.js`
- [ ] T062 [P] [US3] Implement `useWithdraw` React Query mutation hook in `frontend/src/hooks/useWithdraw.js`
- [ ] T063 [P] [US3] Implement `useTransfer` React Query mutation hook in `frontend/src/hooks/useTransfer.js`
- [ ] T064 [P] [US3] Implement Deposit form page with amount field and inline error display in `frontend/src/pages/DepositPage.jsx`
- [ ] T065 [P] [US3] Implement Withdraw form page with amount field, insufficient-funds error handling in `frontend/src/pages/WithdrawPage.jsx`
- [ ] T066 [P] [US3] Implement Transfer form page with fromAccountId, toAccountId, amount fields and same-account/insufficient-funds error handling in `frontend/src/pages/TransferPage.jsx`

**Checkpoint**: US3 complete — all 9 spec operations implemented and testable

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Integration wiring, error handling, and final verification

- [ ] T067 [P] Wire React Router v6 routes connecting all pages (AccountList, AccountDetail, CreateAccount, Customer, Deposit, Withdraw, Transfer) in `frontend/src/App.jsx`
- [ ] T068 [P] Add global error boundary component and 401 redirect to login in `frontend/src/components/ErrorBoundary.jsx`
- [ ] T069 [P] Audit all frontend forms to ensure `ErrorResponse.field` highlights the correct input and `ErrorResponse.message` is shown as user-facing text across `frontend/src/pages/`
- [ ] T070 [P] Add JUnit/Mockito controller tests verifying non-numeric path parameters return 400 for all 9 endpoints in `backend/src/test/unit/`
- [ ] T071 [P] Add service-layer enforcement that all BigDecimal monetary amounts are set to scale=2 before persistence in `backend/src/main/java/com/bank/service/`
- [ ] T072 Create `docker-compose.yml` in project root defining MySQL, backend (with env vars), and frontend services for runtime-like local execution, while keeping H2 available for lightweight local/test use
- [ ] T073 Run `mvn test` in `backend/` and confirm all unit and integration tests pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — **blocks all user stories**
- **Phase 3 (US1)**, **Phase 4 (US2)**, **Phase 5 (US3)**: All depend on Phase 2 — can then run in parallel if team capacity allows, or sequentially P1 → P2 → P3
- **Phase 6 (Polish)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Start after Phase 2 — no dependency on US2 or US3
- **US2 (P2)**: Start after Phase 2 — no dependency on US1 or US3 (update/delete are self-contained)
- **US3 (P3)**: Start after Phase 2 — no dependency on US1 or US2 (TransactionService operates on accountId directly)

### Within Each User Story

- DTOs/request objects before services
- Services before controllers
- Backend service + controller before frontend hook
- Frontend hook before frontend page
- Tests written alongside implementation

---

## Parallel Opportunities

All Setup tasks (T003–T004), Foundational tasks marked [P] (T007–T012, T016–T017, T074–T075), and within each story all tasks marked [P] can be executed concurrently by different team members.

### Example: US3 Parallel Execution

```
After T050 (DTOs):
  ├── T051 deposit service  ─┐
  ├── T052 withdraw service  ├─ parallel
  └── T053 transfer service ─┘

After services:
  ├── T054 deposit controller ─┐
  ├── T055 withdraw controller  ├─ parallel
  └── T056 transfer controller ─┘

After controllers:
  ├── T057–T059 unit tests ──┐
  ├── T060 integration tests  ├─ parallel
  ├── T061–T063 FE hooks      │
  └── T064–T066 FE pages ────┘
```

---

## Summary

| Phase | Tasks | Scope |
|-------|-------|-------|
| Phase 1 — Setup | T001–T004 | 4 tasks |
| Phase 2 — Foundational | T005–T017, T074–T077 | 17 tasks |
| Phase 3 — US1 (P1) | T018–T033, T078 | 17 tasks |
| Phase 4 — US2 (P2) | T034–T049, T079 | 17 tasks |
| Phase 5 — US3 (P3) | T050–T066, T080 | 18 tasks |
| Phase 6 — Polish | T067–T073 | 7 tasks |
| **Total** | **T001–T080** | **80 tasks** |

**MVP scope**: Complete Phase 1 + Phase 2 + Phase 3 (US1) = 38 tasks for a working Create/Retrieve/List Account API with auth and RBAC-aware error handling.
