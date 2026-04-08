# Tasks: Bank App Identity and Customer Management

## Format & Path Conventions

- Backend code lives under `backend/src/main/java/com/bankapp/`
- Backend tests live under `backend/src/test/java/com/bankapp/`
- Frontend code lives under `frontend/src/`
- Frontend tests live under `frontend/src/test/`
- Contracts live under `specs/001-bank-auth-customer/contracts/`
- All tasks follow the required checklist format with task ID, optional `[P]`, optional `[US#]`, and an exact file path

Story mapping used in this task list:
- `[US1]` = Register user
- `[US2]` = Login user
- `[US3]` = Create customer
- `[US4]` = Update customer
- `[US5]` = Get customer details

## Phase 1: Setup

- [X] T001 Add local secret exclusions for `.env` in `.gitignore`
- [X] T002 Create backend build baseline with Spring Boot, JPA, Security, WebFlux, Validation, JaCoCo, and reviewed JWT dependencies in `backend/pom.xml`
- [X] T003 Create frontend static-build baseline and test scripts in `frontend/package.json`
- [X] T004 [P] Add local configuration template for JWT and database settings in `.env.example`

## Phase 2: Foundational

- [X] T005 Create the feature OpenAPI skeleton in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T006 [P] Implement backend security property binding for JWT and environment-backed secrets in `backend/src/main/java/com/bankapp/config/SecurityProperties.java`
- [X] T007 [P] Implement JWT token utility for signed access and refresh tokens in `backend/src/main/java/com/bankapp/security/JwtTokenService.java`
- [X] T008 Implement Spring Security filter chain and BCrypt password encoder configuration in `backend/src/main/java/com/bankapp/security/SecurityConfig.java`
- [X] T009 [P] Implement the shared API error response model in `backend/src/main/java/com/bankapp/common/api/ErrorResponse.java`
- [X] T010 Implement global exception-to-error-code mapping in `backend/src/main/java/com/bankapp/common/api/GlobalExceptionHandler.java`
- [X] T011 [P] Implement the shared frontend API client for authenticated requests in `frontend/src/lib/api-client.ts`
- [X] T012 [P] Implement backend integration-test support and mock-server setup in `backend/src/test/java/com/bankapp/support/ApiIntegrationTestSupport.java`

## Phase 3: User Story 1 - Register User (Priority: P1)

Goal: Allow a new user to register with a unique email and hashed password.

Independent Test: Submit a valid registration request and receive `201`, reject duplicate or invalid input with the documented error format, and confirm `passwordHash` never appears in any API response.

- [X] T013 [P] [US1] Define the registration endpoint contract in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T014 [P] [US1] Add registration contract tests in `backend/src/test/java/com/bankapp/auth/contract/RegisterContractTest.java`
- [X] T015 [P] [US1] Add registration negative service tests for duplicate email, invalid email, weak password, and missing fields in `backend/src/test/java/com/bankapp/auth/service/RegistrationServiceTest.java`
- [X] T016 [P] [US1] Add registration page interaction tests in `frontend/src/test/auth/RegisterPage.test.tsx`
- [X] T017 [US1] Implement the user role enum in `backend/src/main/java/com/bankapp/auth/domain/UserRole.java`
- [X] T018 [US1] Implement the user entity with `passwordHash` excluded from serialization in `backend/src/main/java/com/bankapp/auth/domain/User.java`
- [X] T019 [US1] Implement user lookup and uniqueness queries in `backend/src/main/java/com/bankapp/auth/repository/UserRepository.java`
- [X] T020 [US1] Implement registration request validation DTOs in `backend/src/main/java/com/bankapp/auth/api/RegisterRequest.java`
- [X] T021 [US1] Implement the safe registration response DTO in `backend/src/main/java/com/bankapp/auth/api/UserResponse.java`
- [X] T022 [US1] Implement the registration service with BCrypt hashing and duplicate-user protection in `backend/src/main/java/com/bankapp/auth/service/RegistrationService.java`
- [X] T023 [US1] Expose `POST /api/auth/register` in `backend/src/main/java/com/bankapp/auth/api/AuthController.java`
- [X] T024 [US1] Implement the registration page route in `frontend/src/app/auth/register/page.tsx`
- [X] T025 [US1] Implement the registration form with client-side validation and safe error rendering in `frontend/src/components/auth/RegisterForm.tsx`

## Phase 4: User Story 2 - Login User (Priority: P1)

Goal: Allow an active registered user to log in and receive access and refresh tokens without leaking authentication details.

Independent Test: Log in with valid credentials to receive tokens, reject bad credentials with a generic `401`, reject inactive users with `403`, and confirm JWT claims contain only the documented fields.

- [X] T026 [P] [US2] Define the login endpoint contract in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T027 [P] [US2] Add login contract tests in `backend/src/test/java/com/bankapp/auth/contract/LoginContractTest.java`
- [X] T028 [P] [US2] Add login negative and JWT-claim tests in `backend/src/test/java/com/bankapp/auth/service/AuthenticationServiceTest.java`
- [X] T029 [P] [US2] Add login page and session-state tests in `frontend/src/test/auth/LoginPage.test.tsx`
- [X] T030 [US2] Implement the login request DTO in `backend/src/main/java/com/bankapp/auth/api/LoginRequest.java`
- [X] T031 [US2] Implement the session response DTO for access and refresh tokens in `backend/src/main/java/com/bankapp/auth/api/SessionResponse.java`
- [X] T032 [US2] Implement authentication rules for active users and generic credential failures in `backend/src/main/java/com/bankapp/auth/service/AuthenticationService.java`
- [X] T033 [US2] Update JWT claim generation for `sub`, `roles`, and `exp` in `backend/src/main/java/com/bankapp/security/JwtTokenService.java`
- [X] T034 [US2] Expose `POST /api/auth/login` in `backend/src/main/java/com/bankapp/auth/api/AuthController.java`
- [X] T035 [US2] Implement the login page route in `frontend/src/app/auth/login/page.tsx`
- [X] T036 [US2] Implement frontend auth-session storage and bearer-token injection in `frontend/src/lib/auth-session.ts`

## Phase 5: User Story 3 - Create Customer (Priority: P1)

Goal: Allow an authenticated authorized user to create a customer record with the required fields and linked user ownership.

Independent Test: Create a PERSON or COMPANY customer with a valid bearer token and receive `201`, reject missing token or invalid fields with the documented error format, and ignore any client-supplied immutable identifier.

- [X] T037 [P] [US3] Define the create-customer endpoint contract in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T038 [P] [US3] Add create-customer contract tests in `backend/src/test/java/com/bankapp/customer/contract/CreateCustomerContractTest.java`
- [X] T039 [P] [US3] Add create-customer negative tests for unauthorized, missing fields, and invalid type in `backend/src/test/java/com/bankapp/customer/service/CreateCustomerServiceTest.java`
- [X] T040 [P] [US3] Add customer-creation page tests in `frontend/src/test/customer/CreateCustomerPage.test.tsx`
- [X] T041 [US3] Implement the customer type enum in `backend/src/main/java/com/bankapp/customer/domain/CustomerType.java`
- [X] T042 [US3] Implement the account reference value object in `backend/src/main/java/com/bankapp/customer/domain/AccountReference.java`
- [X] T043 [US3] Implement the customer entity with audit timestamps in `backend/src/main/java/com/bankapp/customer/domain/Customer.java`
- [X] T044 [US3] Implement customer persistence queries in `backend/src/main/java/com/bankapp/customer/repository/CustomerRepository.java`
- [X] T045 [US3] Implement the create-customer request DTO in `backend/src/main/java/com/bankapp/customer/api/CreateCustomerRequest.java`
- [X] T046 [US3] Implement the customer response DTO in `backend/src/main/java/com/bankapp/customer/api/CustomerResponse.java`
- [X] T047 [US3] Implement customer creation and user-customer linking rules in `backend/src/main/java/com/bankapp/customer/service/CustomerService.java`
- [X] T048 [US3] Expose `POST /api/customers` in `backend/src/main/java/com/bankapp/customer/api/CustomerController.java`
- [X] T049 [US3] Implement the customer creation page route in `frontend/src/app/customers/new/page.tsx`

## Phase 6: User Story 4 - Update Customer (Priority: P2)

Goal: Allow an authorized user to update mutable customer profile fields while rejecting immutable-field changes and stale updates.

Independent Test: Update a permitted customer field and receive `200`, reject immutable field changes with `400 FIELD_NOT_UPDATABLE`, reject stale updates with `409`, and reject missing customers with `404`.

- [X] T050 [P] [US4] Define the update-customer endpoint contract in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T051 [P] [US4] Add update-customer contract and immutable-field tests in `backend/src/test/java/com/bankapp/customer/contract/UpdateCustomerContractTest.java`
- [X] T052 [P] [US4] Add update-customer service tests for stale timestamps and immutable fields in `backend/src/test/java/com/bankapp/customer/service/UpdateCustomerServiceTest.java`
- [X] T053 [P] [US4] Add customer-edit page tests in `frontend/src/test/customer/EditCustomerPage.test.tsx`
- [X] T054 [US4] Implement the update-customer request DTO with immutable-field validation in `backend/src/main/java/com/bankapp/customer/api/UpdateCustomerRequest.java`
- [X] T055 [US4] Implement optimistic-concurrency update rules in `backend/src/main/java/com/bankapp/customer/service/CustomerService.java`
- [X] T056 [US4] Expose `PATCH /api/customers/{customerId}` in `backend/src/main/java/com/bankapp/customer/api/CustomerController.java`
- [X] T057 [US4] Implement the customer edit page with conflict-state handling in `frontend/src/app/customers/[customerId]/edit/page.tsx`

## Phase 7: User Story 5 - Get Customer Details (Priority: P2)

Goal: Allow an authorized user to retrieve customer details without mutating data.

Independent Test: Fetch an existing customer and receive `200` with all documented fields, reject missing token or invalid ID format, and confirm repeated GET calls do not change `updatedAt`.

- [X] T058 [P] [US5] Define the get-customer endpoint contract in `specs/001-bank-auth-customer/contracts/openapi.yaml`
- [X] T059 [P] [US5] Add get-customer contract and authorization tests in `backend/src/test/java/com/bankapp/customer/contract/GetCustomerContractTest.java`
- [X] T060 [P] [US5] Add customer-detail page rendering tests in `frontend/src/test/customer/CustomerDetailPage.test.tsx`
- [X] T061 [US5] Implement read-only customer detail queries in `backend/src/main/java/com/bankapp/customer/service/CustomerQueryService.java`
- [X] T062 [US5] Expose `GET /api/customers/{customerId}` in `backend/src/main/java/com/bankapp/customer/api/CustomerController.java`
- [X] T063 [US5] Implement the customer detail page route in `frontend/src/app/customers/[customerId]/page.tsx`

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T064 [P] Add OpenAPI compliance regression tests for all implemented endpoints in `backend/src/test/java/com/bankapp/contract/OpenApiComplianceTest.java`
- [X] T065 [P] Add security regression tests for plaintext password logging and `passwordHash` leakage in `backend/src/test/java/com/bankapp/security/SecurityRegressionTest.java`
- [X] T066 [P] Add frontend accessibility and responsive verification tests for auth and customer flows in `frontend/src/test/accessibility/AuthCustomerAccessibilityTest.tsx`
- [X] T067 [P] Add public-environment safety checks to prevent secret exposure in static assets in `frontend/src/test/config/PublicEnvSafetyTest.ts`
- [X] T068 Verify JaCoCo coverage gates and fail the build below the required threshold in `backend/pom.xml`

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 Setup must complete before Phase 2 Foundational.
- Phase 2 Foundational must complete before any user story phases.
- Phase 3 `[US1]` should complete before Phase 4 `[US2]`.
- Phase 4 `[US2]` should complete before Phase 5 `[US3]`.
- Phase 5 `[US3]` should complete before Phase 6 `[US4]` and Phase 7 `[US5]`.
- Phase 8 Polish runs after all user story phases.

### User Story Dependencies

- `[US1]` Register is the first deliverable user story.
- `[US2]` Login depends on `[US1]` user persistence and hashing rules.
- `[US3]` Create Customer depends on `[US2]` authentication and bearer-token support.
- `[US4]` Update Customer depends on `[US3]` customer creation and persistence.
- `[US5]` Get Customer depends on `[US3]` customer creation and retrieval support.

## Parallel Opportunities

### Foundational

- T006, T007, T009, T011, and T012 can run in parallel after T005.

### User Story 1

- T013, T014, T015, and T016 can run in parallel before backend implementation begins.
- T024 and T025 can proceed once T013 defines the registration contract.

### User Story 2

- T026, T027, T028, and T029 can run in parallel before backend implementation begins.
- T035 and T036 can proceed once T026 defines the login contract.

### User Story 3

- T037, T038, T039, and T040 can run in parallel before backend implementation begins.
- T041, T042, and T043 can be developed in parallel because they touch separate domain files.

### User Story 4

- T050, T051, T052, and T053 can run in parallel before backend implementation begins.

### User Story 5

- T058, T059, and T060 can run in parallel before backend implementation begins.

### Polish

- T064, T065, T066, and T067 can run in parallel after all story work is complete.

## Implementation Strategy

### MVP First

- Deliver Phases 1 through 5 first to ship the MVP: registration, login, and customer creation.
- This yields a usable authenticated flow with the minimum protected business capability.

### Incremental Delivery

- Add Phase 6 to support safe customer updates with immutable-field rejection and stale-update protection.
- Add Phase 7 to complete read-only customer retrieval behavior.
- Finish with Phase 8 to enforce quality, accessibility, contract, and coverage gates.

### Team Strategy

- One backend engineer can focus on foundational security and auth services.
- One frontend engineer can work from the OpenAPI contract on auth and customer pages.
- One QA or test-focused engineer can build contract, negative, integration, and accessibility coverage in parallel.

## Notes

- All password handling must remain server-side and hashed with BCrypt.
- No endpoint may return `passwordHash`.
- All secrets must come from `.env` or managed environment variables, never hardcoded.
- `WebClient` is the only allowed Spring HTTP client for outbound service calls.
- JPA is the required persistence path; do not bypass it with raw SQL.
- API responses must not add fields not defined in the OpenAPI contract.
- Any new dependency must pass an explicit security review before adoption.