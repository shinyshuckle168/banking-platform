# Implementation Plan: Banking API (Spring Boot + React)

**Branch**: `main` | **Date**: 2026-04-02 | **Spec**: [spec.md](spec.md)

## Summary

Implement a Banking API based on the completed and clarified spec. The system has a **Spring Boot REST backend** (Java 21) and a **React frontend** (JavaScript). Three domain entities — Customer, Account, Transaction — back nine REST operations. Critical cross-cutting concerns: authentication and role-based authorization on every endpoint, endpoint-level permission enforcement plus ownership checks, idempotency for all three money-movement operations, atomicity for Transfer, first-commit-wins concurrency, deletion semantics with retained underlying records for delete operations, and BigDecimal scale=2 monetary precision.

---

## Technical Context

| Dimension | Decision |
|---|---|
| Language/Version | Java 21 (backend), JavaScript ES2022 (frontend) |
| Backend Framework | Spring Boot 3.x (Web, Data JPA, Validation, Security) |
| Frontend Framework | React 18 + React Query v5 + Axios + Vite |
| Storage | Runtime: MySQL; local development and test execution: H2 |
| Testing (backend) | JUnit 5, Mockito |
| Testing (frontend) | Jest, React Testing Library |
| Build (backend) | Maven (pom.xml) |
| Build (frontend) | Vite + npm / package.json |
| Target Platform | Linux server (backend), modern browser (frontend) |
| Monetary Precision | BigDecimal scale=2, single system currency |
| Security | JWT Bearer token on all endpoints, RBAC permission enforcement, ownership authorization per resource |
| Project Type | Web service (backend) + Web application (frontend) |

---

## Project Structure

### Documentation (this feature)

```
specs/
├── plan.md          ← this file
├── spec.md          ← feature specification
└── tasks.md         ← task breakdown
```

### Source Code

```
account-service/
├── spec.md
├── backend/                         ← Spring Boot project
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/bank/
│       │   │   ├── model/           Customer, Account, Transaction, Role, Permission, UserRole + enums
│       │   │   ├── repository/      CustomerRepository, AccountRepository, TransactionRepository, RBAC repositories
│       │   │   ├── service/         CustomerService, AccountService, TransactionService, AuthorizationService
│       │   │   ├── controller/      CustomerController, AccountController
│       │   │   ├── dto/             request/response DTOs
│       │   │   ├── exception/       BankingException, GlobalExceptionHandler
│       │   │   ├── security/        JWT principal mapping, permission evaluators, ownership guards
│       │   │   └── config/          SecurityConfig
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/    Flyway scripts (V1__init.sql, etc.)
│       └── test/
│           └── unit/
└── frontend/                        ← React project
    ├── package.json
    └── src/
        ├── api/                     Axios client stubs per operation
        ├── components/              shared UI components
        ├── pages/                   per-operation pages/forms
        ├── hooks/                   React Query hooks per operation
        └── types/                   JS object shape definitions for domain objects + enums
```

---

## Phases

### Phase 0 — Research & Decisions *(no blockers; can begin immediately)*

1. Confirm Spring Boot 3.x + Java 21 Maven dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `flyway-core`, `mysql-connector-j`, `h2`, `jjwt`.
2. Confirm Vite React JavaScript scaffold dependencies: `react-query` v5, `axios`, `react-router-dom` v6.
3. Document idempotency mechanism: `Idempotency-Key` HTTP request header; value stored as unique column `idempotency_key` on `transaction` table; duplicate insert violation = duplicate request; return cached `Transaction` response.
4. Document concurrency mechanism: `@Version` column on `account`; optimistic locking; Spring Data raises `ObjectOptimisticLockingFailureException` on conflict; service retries once; first commit wins from client perspective.
5. Document auth mechanism: JWT Bearer token validated by Spring Security `OncePerRequestFilter`; 401 returned for missing/invalid token on all routes; caller roles/permissions resolved from JWT claims and/or persisted role assignments; endpoint permission checks applied before resource ownership checks.

**Output:** `specs/research.md`

---

### Phase 1 — Data Model & Contracts *(depends on Phase 0; steps 1–3 parallel)*

1. **Data model** → `specs/data-model.md`:
   - `customer`: PK `customer_id` (bigint auto_increment), `name` (varchar), `address` (text), `type` (enum: PERSON/COMPANY), `created_at`, `updated_at`.
   - `customer`: include deletion status fields (`deleted_at` timestamp nullable or equivalent deletion indicator) so deleted records can be excluded from normal operational queries while remaining stored.
   - `account`: PK `account_id` (bigint auto_increment), FK `customer_id`, `account_type` (enum: CHECKING/SAVINGS), `balance` (decimal(19,2) NOT NULL DEFAULT 0), `interest_rate` (decimal nullable — SAVINGS only), `next_check_number` (bigint nullable — CHECKING only), `version` (bigint for OL), deletion status fields (`deleted_at` timestamp nullable or equivalent deletion indicator), `created_at`, `updated_at`.
   - `transaction`: PK `transaction_id` (bigint auto_increment), FK `account_id`, `amount` (decimal(19,2)), `type` (enum: DEPOSIT/WITHDRAW/TRANSFER), `timestamp`, `description` (text), `status` (enum: SUCCESS/FAILED), `idempotency_key` (varchar UNIQUE).
   - RBAC model: persisted role and permission catalog plus user-role assignment keyed to the authenticated principal identifier from the external Identity Service.
   - Relationships: Customer 1→N Account; Account 1→N Transaction.
   - Relationships: Role M→N Permission; User principal 1→N Role assignments.

2. **OpenAPI 3.0 contract** → `specs/contracts/openapi.yaml`:
   - All 9 endpoints with method, path, request body schema, response schemas (200/201/400/401/404/409/422), `ErrorResponse` component, Bearer security scheme, and per-operation required-permission notes aligned to the spec's endpoint permission matrix.

3. **Quickstart** → `specs/quickstart.md`:
   - `docker-compose.yml` running MySQL + Spring Boot backend + React frontend for runtime-like local execution.
   - Document H2 as the lightweight local/test database option for non-containerized development and automated tests.
   - Environment variables, test credentials, first-run instructions.

**Output:** `specs/data-model.md`, `specs/contracts/openapi.yaml`, `specs/quickstart.md`

---

### Phase 2 — Backend Implementation *(depends on Phase 1)*

**Group A — Foundation (sequential, each depends on prior):**

1. Bootstrap Maven project; `application.yml`; Flyway `V1__init.sql` creating all three tables with correct types, constraints, and indexes.
2. JPA entities (`Customer`, `Account`, `Transaction`), enum types, and Spring Data repositories.
3. `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping exception subtypes to `ErrorResponse` with correct HTTP status codes (400/404/409/422).
4. RBAC foundation: seed default roles and permissions, persist user-role assignments, and provide lookup components that resolve effective permissions for the authenticated principal.
5. `SecurityConfig`: JWT `OncePerRequestFilter`; all routes require authentication; 401 on missing/invalid token; enforce endpoint required permissions; ownership check returns 401 when authenticated caller does not own the requested customer or account.

**Group B — Services + Controllers (parallel after Group A; each endpoint independent):**

6. `DELETE /customers/{customerId}` — `CustomerService.deleteCustomer()`: guard no active accounts, perform deletion, preserve retained record, require `CUSTOMER:DELETE` → 200/404/409.
7. `POST /customers/{customerId}/accounts` — `AccountService.createAccount()`: field-type compatibility check, require `ACCOUNT:CREATE` → 201/400/404/422.
8. `GET /accounts/{accountId}` — `AccountService.getAccount()`: require `ACCOUNT:READ` → 200/400/404.
9. `GET /customers/{customerId}/accounts` — `AccountService.listCustomerAccounts()`: require `CUSTOMER:READ` and `ACCOUNT:READ` → 200/400/404.
10. `PUT /accounts/{accountId}` — `AccountService.updateAccount()`: type-constrained field update, require `ACCOUNT:UPDATE` → 200/400/401/404/422.
11. `DELETE /accounts/{accountId}` — `AccountService.deleteAccount()`: zero-balance guard, perform deletion, preserve retained record, require `ACCOUNT:DELETE` → 200/400/404/409.
12. `POST /accounts/{accountId}/deposit` — `TransactionService.deposit()`: idempotency key, persist SUCCESS/FAILED transaction, require `TRANSACTION:DEPOSIT` → 200/400/404/422.
13. `POST /accounts/{accountId}/withdraw` — `TransactionService.withdraw()`: balance guard, idempotency, optimistic locking, require `TRANSACTION:WITHDRAW` → 200/400/404/409/422.
14. `POST /accounts/transfer` — `TransactionService.transfer()`: `@Transactional`, atomicity, same-account guard, idempotency, require `TRANSACTION:TRANSFER` → 200/400/404/409/422.

**Group C — Tests (parallel with Group B):**

15. Unit tests per service method: positive path + each negative path from spec acceptance criteria.
16. Controller- and service-level JUnit/Mockito tests per endpoint flow: success + primary failure mode at minimum.
17. Authorization-focused JUnit/Mockito tests: missing/invalid token handling, missing required permission, ownership violation, and admin override across representative endpoints.

---

### Phase 3 — Frontend Implementation *(depends on Phase 1 contracts; parallel with Phase 2)*

1. Scaffold Vite + React 18 + JavaScript; configure Axios base URL and Bearer token auth interceptor.
2. Define JS shape constants for `Customer`, `Account`, `Transaction`, `ErrorResponse`, and enum values from the OpenAPI contract.
3. Implement React Query hooks — one per API operation:
   `useDeleteCustomer`, `useCreateAccount`, `useGetAccount`, `useListCustomerAccounts`, `useUpdateAccount`, `useDeleteAccount`, `useDeposit`, `useWithdraw`, `useTransfer`.
4. Implement pages/forms (parallel):
   - Customer page: delete customer action.
   - Account list page: list accounts for a customer.
   - Account detail page: retrieve, update, delete account.
   - Create account form.
   - Deposit / Withdraw / Transfer forms.
5. Wire `ErrorResponse.message` to user-facing error messages; highlight `field` validation errors inline on corresponding form fields.

---

### Phase 4 — Integration & Verification *(depends on Phase 2 + Phase 3)*

1. Docker Compose: MySQL + backend + frontend running together, with H2 available for lightweight local/test execution outside Compose.
2. Verify all spec acceptance criteria (positive + negative scenarios per endpoint).
3. Idempotency spot-check: submit duplicate Deposit/Withdraw/Transfer with same `Idempotency-Key`; confirm single balance change, second call returns original response.
4. Transfer atomicity spot-check: confirm rollback on failure; both account balances unchanged.
5. OpenAPI linter pass on `contracts/openapi.yaml`.

---

## Verification Checklist

1. All 9 endpoints return documented status codes for every spec acceptance scenario.
2. Duplicate `Idempotency-Key` on Deposit/Withdraw/Transfer → original response, balance unchanged.
3. Transfer insufficient funds → 409, both balances unchanged.
4. Delete customer with active accounts → 409, customer remains active and record persists.
5. Delete customer with no active accounts → 200, customer is deleted and underlying record persists.
6. Delete account with non-zero balance → 409, account remains active and record persists.
7. Delete account with zero balance → 200, account is deleted and underlying record persists.
8. Non-numeric path ID on any endpoint → 400.
9. `amount=0` on Deposit or Withdraw → 422, `field="amount"`.
10. All endpoints without valid Bearer token → 401.
11. Authenticated caller without the endpoint's required permission is rejected according to the final security contract.
12. Authenticated non-admin caller accessing another customer's account → 401.
13. Authenticated admin caller can access resources regardless of ownership.
14. OpenAPI linter passes on `contracts/openapi.yaml`.
15. `mvn test` all pass.

---

## Decisions

- **MySQL for runtime, H2 for local/test**: runtime environments use MySQL, while H2 supports lightweight development and automated test execution.
- **Optimistic locking** (`@Version` on `Account`): first-commit-wins without serialization overhead.
- **Idempotency-Key** stored as unique column on `transaction`; duplicate DB insert = duplicate request; cached response returned.
- `interestRate` / `nextCheckNumber` are nullable columns; account-type incompatibility enforced at service layer, not DB check constraints.
- **JWT Bearer token** is the standard Spring Boot default for "all endpoints require authenticated caller".
- RBAC is implemented as role-based permission evaluation aligned to the endpoint permission matrix; direct per-user permission grants are out of scope unless the spec is expanded.
- Multi-currency is out of scope for v1.
- Frontend covers the 9 spec-defined operations only.
