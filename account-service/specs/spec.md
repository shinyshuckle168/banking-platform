# Banking API Feature Specification

## Business Context
The Banking API is the core service for managing customer accounts and monetary operations within a retail banking platform. It exposes a REST interface consumed by a web frontend and, where authorised, by internal back-office tooling. Two categories of caller interact with the API: customers who manage their own data, and administrators who have elevated, unrestricted access across all resources.

## Purpose and Scope
This specification defines the Banking API as the source of truth for contract generation and implementation alignment. It covers the following operations only:

1. Delete Customer
2. Create Account
3. Retrieve Account Details
4. List Customer Accounts
5. Update Account
6. Delete Account
7. Deposit
8. Withdraw
9. Transfer Funds

This document is specification-first. Business rules and API behaviour remain normative, and the Development Perspective sections may include agreed implementation context where that helps delivery.

### Development Perspective
- Target implementation stack:
  - Backend: Java 21 with Spring Boot 3.x (`Web`, `Data JPA`, `Validation`, `Security`)
  - Frontend: React 18 with JavaScript, React Query v5, Axios, and Vite
  - Runtime persistence: MySQL
  - Local development and test persistence: H2
  - Backend testing: JUnit 5 and Mockito
  - Frontend testing: Jest and React Testing Library
- Treat all statements using MUST, MUST NOT, REQUIRED, and NOT ALLOWED as normative.
- For implementation purposes, the externally observable contract is defined by the combination of shared sections and each operation's contract, rules, error mapping, edge cases, and acceptance criteria.
- Where multiple rules apply, operation-specific statements take precedence over general cross-cutting statements when they are more specific.
- In this document, implementation guidance is intended to help backend and frontend developers map the specification onto the agreed project stack; detailed class design, file layout, and coding tasks remain the responsibility of the implementation plan.

### QA Perspective
- Use this document as the basis for traceable test coverage, not just as a high-level requirements summary.
- For every operation, verify request/response contract, security behaviour, state change or non-change, and any required persisted side effects.
- Acceptance criteria are the minimum regression set and should be extended with boundary, negative, and authorization scenarios derived from the surrounding rules.

## Clarifications

### Session 2026-04-02

- Q: Should failed monetary operations be persisted as transactions? → A: Yes, persist transactions for both SUCCESS and FAILED for Deposit, Withdraw, and Transfer.
- Q: How should concurrent money-movement conflicts be handled? → A: Allow concurrent operations; whichever commits first wins silently.
- Q: How should duplicate retries be handled for monetary operations? → A: Require idempotency for Deposit, Withdraw, and Transfer so retried identical requests return the original outcome.
- Q: What is the API security baseline? → A: All endpoints require authenticated caller; account/customer operations allowed only on resources the caller is authorized to access.
- Q: What are the monetary currency and precision rules? → A: All monetary amounts are in one system currency with scale=2 (two decimal places).

### Session 2026-04-06

- Q: Should this API persist internal user data in addition to external identity? → A: Yes. The API persists a local user record linked to the external identity subject.
- Q: Are admin role grant/revoke actions in scope for this API? → A: Yes, admin role-management capability is in scope.
- Q: What validation applies to `interestRate` on savings accounts? → A: `interestRate` must be a non-negative decimal value with at most 4 decimal places.
- Q: What validation applies to `nextCheckNumber` on checking accounts? → A: `nextCheckNumber` must be a whole number greater than or equal to `0` and greater than the current stored value.

## Shared Definitions

### Domain Objects

#### Customer
- `customerId` (long)
- `name`
- `address`
- `type` (PERSON or COMPANY)
- `accounts` (List of Account)
- `createdAt`
- `updatedAt`

#### Account
- `accountId` (long)
- `accountType` (CHECKING or SAVINGS)
- `status` (ACTIVE or CLOSED)
- `balance` (BigDecimal)
- `interestRate` (Savings only)
- `nextCheckNumber` (Checking only)
- `createdAt`
- `updatedAt`

#### Transaction
- A record of a monetary operation on an account. Both successful and failed monetary outcomes are persisted as transaction records in line with the shared platform rule.
- `transactionId` (UUID string, exactly 36 characters)
- `accountId` (foreign key linking this transaction to its account, using the same identifier type as `Account.accountId`)
- `amount` (BigDecimal, exactly scale=2, greater than `0`)
- `direction` (`CREDIT` or `DEBIT`)
- `status` (`PENDING`, `SUCCESS`, or `FAILED`)
- `timestamp` (ISO-8601 UTC string)
- `description` (string, maximum 255 characters)
- `senderInfo` (string, maximum 100 characters, present on `CREDIT` transactions)
- `receiverInfo` (string, maximum 100 characters, present on `DEBIT` transactions)
- `idempotencyKey` (string, optional) — present only on transactions originating from Deposit, Withdraw, or Transfer requests that supplied an `Idempotency-Key` header
- Relationships: many transactions belong to one account.

#### User
- Represents an authenticated actor with a local application record linked to an external identity subject.
- A `User` MAY be linked to a single `Customer` profile through `customerId` when the actor is an end-customer. Administrative or internal users are not required to have a linked customer profile.
- `id`
- `externalSubjectId`
- `customerId` (optional)
- `username`
- `roles` (list of assigned roles)

#### Role
- A collection of permissions.
- `id`
- `name` (e.g., CUSTOMER, ADMIN, ACCOUNT_READ_ONLY)
- `permissions` (list of permissions)

### Enums

#### AccountType
- `CHECKING`
- `SAVINGS`

#### TransactionStatus
- `PENDING`
- `SUCCESS`
- `FAILED`

#### TransactionDirection
- `CREDIT`
- `DEBIT`

#### AccountStatus
- `ACTIVE`
- `CLOSED`

### ErrorResponse Schema
```json
{
  "code": "string",
  "message": "string",
  "field": "string"
}
```
- `ErrorResponse` is returned for all error responses defined by this specification.
- In the current version of this API, that includes `400`, `401`, `404`, `409`, and `422` responses.
- If additional error status codes are introduced in later revisions, they SHOULD use the same `ErrorResponse` schema unless this document explicitly states otherwise.
- `code` is a machine-readable error code identifying the type of failure, for example `ACCOUNT_NOT_FOUND` or `INVALID_ACCOUNT_ID`.
- `message` is a human-readable explanation of what went wrong.
- `field` is optional and is used when a validation error is tied to a specific request field.

### Actors and Roles
- `CUSTOMER` — an authenticated end-user with access only to self-service banking features on resources they own.
  - Allowed features:
    - Create Account for their own customer profile
    - Retrieve Account Details for their own accounts
    - List Customer Accounts for their own customer profile
    - Update Account for their own accounts, subject to account-type rules
    - Deposit into their own accounts
    - Withdraw from their own accounts
    - Transfer funds from their own accounts
  - Not allowed:
    - Create customers
    - Delete customers
    - Delete accounts
    - Access another customer's resources
- `ADMIN` — a privileged operator with unrestricted access to all features defined in this specification.
  - Allowed features:
    - Delete Customer for any customer, subject to business rules
    - Create Account for any customer
    - Retrieve Account Details for any account
    - List Customer Accounts for any customer
    - Update Account for any account, subject to account-type rules
    - Delete Account for any account, subject to business rules
    - Deposit into any account
    - Withdraw from any account
    - Transfer funds between any accounts
  - Not allowed:
    - Create customers, because customer creation is outside the scope of this API

### Glossary
- **Active account** — an account record with `status=ACTIVE`. An account with a zero balance is still considered active unless it has been closed.
- **Closed account** — an account record with `status=CLOSED`. Closed accounts remain retained in storage but are not available for normal operational use.

### Credential Policy
Credential rules are enforced by the external Identity Service. This API never receives or stores raw credentials; it only receives the resulting JWT. The following patterns are agreed between this system and the Identity Service and must be enforced at registration time.

**Username**
- Length: 4–30 characters.
- Allowed characters: letters (`a–z`, `A–Z`), digits (`0–9`), underscores (`_`), hyphens (`-`), and periods (`.`).
- Must begin with a letter.
- Case-insensitive; stored in lower case.
- Pattern: `^[a-zA-Z][a-zA-Z0-9._-]{3,29}$`

**Password**
- Length: 8–128 characters.
- Must contain at least one uppercase letter (`A–Z`).
- Must contain at least one lowercase letter (`a–z`).
- Must contain at least one digit (`0–9`).
- Must contain at least one special character from: `! @ # $ % ^ & * ( ) - _ = + [ ] { } | ; : , . < > ?`
- Spaces are not permitted.
- Pattern (minimum requirements): `^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()\-_=+\[\]{}|;:,.<>?])[^\s]{8,128}$`

### Development Perspective
- Shared definitions establish the canonical meaning of domain fields, roles, and enums used throughout the document.
- If an operation references a field or role, its semantics come from this section unless the operation narrows them explicitly.
- Developers should treat these definitions as the contract vocabulary that request models, response models, validation, and authorization behaviour must consistently reflect.
- In the agreed stack, these definitions are expected to map cleanly to Spring Boot DTOs, JPA-backed domain models, frontend API types, and form state used by the React client.
- Account status is part of the domain contract. This API uses only the documented `ACTIVE` and `CLOSED` states.

### QA Perspective
- Validate that response payloads, enum values, and field presence align with these shared definitions.
- Use the role descriptions here to derive expected access outcomes for `CUSTOMER` and `ADMIN` callers.
- Use the glossary and credential-policy statements to avoid testing assumptions that contradict the agreed domain language.

## Role-Based Access Control (RBAC)

### Overview
- Users are assigned one or more roles.
- Roles are composed of one or more permissions.
- Each API endpoint MUST define one or more required permissions.
- Access is granted if `required_permissions ⊆ user.permissions`.

### Permissions
Permissions represent fine-grained access control to resources.

Examples:
- `CUSTOMER:CREATE`
- `CUSTOMER:READ`
- `CUSTOMER:UPDATE`
- `CUSTOMER:DELETE`
- `ACCOUNT:CREATE`
- `ACCOUNT:READ`
- `ACCOUNT:UPDATE`
- `ACCOUNT:DELETE`
- `TRANSACTION:DEPOSIT`
- `TRANSACTION:WITHDRAW`
- `TRANSACTION:TRANSFER`

### Default Roles

#### CUSTOMER
- Permissions:
  - `CUSTOMER:READ` (own data only)
  - `CUSTOMER:UPDATE` (own data only)
  - `ACCOUNT:CREATE`
  - `ACCOUNT:READ` (own accounts only)
  - `ACCOUNT:UPDATE` (own accounts only)
  - `TRANSACTION:DEPOSIT`
  - `TRANSACTION:WITHDRAW`
  - `TRANSACTION:TRANSFER`

#### ADMIN
- Permissions:
  - All permissions
- Notes:
  - No ownership restrictions apply.

### Custom Roles (Optional)

#### ACCOUNT_READ_ONLY
- Permissions:
  - `ACCOUNT:READ`

#### ACCOUNT_MANAGER
- Permissions:
  - `ACCOUNT:CREATE`
  - `ACCOUNT:READ`
  - `ACCOUNT:UPDATE`

### Role Assignment
- A user MAY have multiple roles.
- Roles can be assigned at user creation or via admin-managed updates.
- Only `ADMIN` may grant roles to users or revoke roles from users.
- Administrative role grant and revoke capability is in scope for this API.
- Detailed endpoint contracts for role-management operations are not yet specified in this document and MUST be added before implementation of that capability begins.
- Direct per-user permission grants or revocations are out of scope; effective permissions are derived from assigned roles only.
- Role assignments MUST be persisted.

### Authorization Rules

#### Permission-Based Access
- Each API endpoint MUST define one or more required permissions.
- Access is granted if `required_permissions ⊆ user.permissions`.
- If an authenticated caller lacks one or more required permissions for an endpoint, the API MUST return `401 Unauthorized`.

#### Ownership Constraint
- Having the required permissions does NOT bypass ownership rules.
- For non-admin users, access MUST be restricted to resources owned by the user.
- If ownership validation fails, the API MUST return `401 Unauthorized`.

#### Admin Override
- A user with role `ADMIN` has all permissions.
- A user with role `ADMIN` can access all endpoints.
- A user with role `ADMIN` bypasses ownership checks.

### Endpoint Permission Matrix
- `DELETE /customers/{customerId}` → requires `CUSTOMER:DELETE`
- `POST /customers/{customerId}/accounts` → requires `ACCOUNT:CREATE`
- `GET /accounts/{accountId}` → requires `ACCOUNT:READ`
- `GET /customers/{customerId}/accounts` → requires `CUSTOMER:READ` and `ACCOUNT:READ`
- `PUT /accounts/{accountId}` → requires `ACCOUNT:UPDATE`
- `DELETE /accounts/{accountId}` → requires `ACCOUNT:DELETE`
- `POST /accounts/{accountId}/deposit` → requires `TRANSACTION:DEPOSIT`
- `POST /accounts/{accountId}/withdraw` → requires `TRANSACTION:WITHDRAW`
- `POST /accounts/transfer` → requires `TRANSACTION:TRANSFER`

### Development Perspective
- The permission matrix is normative for endpoint access control and must stay aligned with the operation-specific Security Constraints.
- Permission checks and ownership checks are separate requirements; satisfying one does not waive the other unless the caller is `ADMIN`.
- Any future endpoint added to this API must define required permissions before implementation begins.
- In the agreed implementation stack, these rules are expected to be enforced in Spring Security and supporting authorization components rather than in frontend-only logic.

### QA Perspective
- For each endpoint, test at least: unauthenticated caller, authenticated caller with required permission and valid ownership, authenticated caller without required permission, ownership failure for `CUSTOMER`, and admin override for `ADMIN`.
- Validate that documented permission combinations are enforced exactly as written, especially endpoints requiring more than one permission.

## Assumptions
- Customer registration and authentication are managed by a separate Identity Service outside the scope of this API.
- A valid JWT Bearer token is issued by the external Identity Service and must be present on every request. The Banking API validates the token on each call but does not issue tokens.
- The Banking API persists a local user record linked to the external identity subject and uses that record for application-level role assignment.
- A single system currency is pre-configured at deployment time. No currency conversion is performed by this API.
- Account type is immutable after creation; it cannot be changed via any operation in this API.
- All timestamps stored and returned by this API are in UTC.
- The caller identity is available from the JWT token claims and can be linked to the persisted local user record.

## Out of Scope
The following are explicitly excluded from this specification and must not be implemented as part of this feature:
- Customer registration and login flows
- Interest calculation and accrual processing
- Account type conversion (e.g., CHECKING to SAVINGS)
- Transaction history pagination and filtering
- Event notifications or webhooks triggered by balance changes
- Multi-currency support or foreign exchange operations

## Non-Functional Requirements
- All monetary amounts MUST be stored and returned at exactly scale=2 (two decimal places). Values with more than two decimal places must be rejected.
- Monetary operations (Deposit, Withdraw, Transfer) MUST be idempotent when the caller supplies the same `Idempotency-Key` header value on a repeated request. The original response MUST be returned and no balance change applied again.
- Transfer MUST be atomic: either both the debit from the source account and the credit to the destination account are committed, or neither is applied.
- Concurrent balance modifications on the same account are resolved via optimistic locking. The first request to commit wins; conflicting concurrent requests are retried internally or return an appropriate error.
- All API responses MUST use `application/json` as the content type.

### Development Perspective
- These requirements are contract-level obligations, even when the implementation mechanism is left unspecified.
- Any implementation approach is acceptable only if it preserves the externally visible behaviour defined here for precision, idempotency, atomicity, concurrency outcomes, and content type.
- For the agreed stack, developers should assume Spring Boot REST endpoints returning JSON, Java `BigDecimal` for monetary values, and a React client consuming the API through Axios-based HTTP calls.

### QA Perspective
- Verify scale handling, idempotent replay behaviour, atomic rollback behaviour, concurrency-sensitive outcomes, and JSON response content type as cross-cutting quality gates.
- Treat these as mandatory regression areas across multiple endpoints, not as one-off tests.

## Data Storage Principles
- Core business records MUST be stored in persistent server-side data stores and MUST NOT rely on in-memory-only storage.
- Customer, account, user-role assignment, and transaction records MUST be stored as structured operational data so they can be queried, validated, and updated consistently.
- Audit logs MUST be stored separately from normal operational records in append-only durable storage.
- Idempotency records MUST be stored in durable storage for their configured retention window so repeated requests can be detected reliably across restarts and deployments.
- Deleted resources MUST be removed from normal operational access while their retained records remain stored in accordance with the Data Retention and Audit Policy.
- The system MUST be able to retrieve retained customer, account, transaction, role-assignment, and audit records for audit, legal, regulatory, and support purposes throughout the required retention period.
- The specific database engine, file format, hosting model, and infrastructure topology are implementation-defined and out of scope for this specification.

### Development Perspective
- Storage-related rules here define what data must exist and remain retrievable, not how a particular persistence technology must be selected.
- Delete semantics in the operation sections must be interpreted consistently with these storage principles and the retention policy.
- For this feature, runtime operational data is expected to use MySQL, while H2 may be used for local development and test execution where a lightweight environment is sufficient.
- Audit and retention obligations apply regardless of whether the environment is backed by MySQL or H2.

### QA Perspective
- Testing should confirm that deletion removes resources from normal operational access without violating retained-record expectations.
- Where direct storage inspection is not available through the API, QA should identify evidence paths that demonstrate retention and audit requirements are being met.

## Data Retention and Audit Policy
- Financial transaction records and customer/account data MUST be retained for 7 years.
- This 7-year retention period is derived from the Canada Revenue Agency (CRA) requirement to retain records for 6 years, plus an additional 1-year buffer.
- This retention period also satisfies the minimum 5-year recordkeeping expectation referenced by FINTRAC: https://fintrac-canafe.canada.ca/guidance-directives/recordkeeping-document/record/fin-eng
- Customer data, account data, transaction data, role-assignment data, and audit data MUST be stored in persistent storage for the full 7-year retention period.
- The system MUST maintain an append-only audit log capturing all operations, including both successful and failed actions.
- Audit logs MUST include actor identity, role, action, resource type, resource ID, timestamp, and outcome.
- Audit logs MUST be retained for at least the same 7-year duration as financial records and MUST NOT be modified or deleted during the retention period.
- All transaction records, including records for both successful and failed monetary outcomes, MUST be retained and MUST NOT be deleted during the 7-year retention period.
- Transaction records SHOULD be treated as immutable once written.
- "Delete" operations defined in this API MUST remove the resource from normal operational use.
- Underlying records MUST remain stored for audit and regulatory purposes.
- Idempotency keys MAY be retained for a limited duration, such as 24 to 72 hours, and MAY be purged thereafter.
- Purging idempotency data MUST NOT affect retention of transaction records, customer/account records, or audit logs.
- Personal data MUST be retained only as long as necessary to fulfill regulatory, legal, and business requirements.
- In accordance with Principle 5 of the Personal Information Protection and Electronic Documents Act (PIPEDA), personal information that is no longer required to fulfil the identified purposes should be destroyed, erased, or made anonymous, and organisations SHOULD maintain guidelines and procedures governing such disposal.
- Where continued retention of financial records is required but direct personal identification is no longer necessary, personal data SHOULD be irreversibly anonymized.

### Development Perspective
- Retention and audit requirements are normative business constraints that apply to delete operations, transaction persistence, and role-assignment history.
- Any behaviour that removes a resource from active use must still preserve the retention obligations stated here.
- Developers should interpret these requirements as applying across the Spring Boot service layer, persistence layer, and any supporting audit or background retention processes used by the agreed stack.

### QA Perspective
- QA should verify not only successful business outcomes but also that required auditability and record-retention behaviour is preserved for successful and failed actions.
- Failed monetary operations are especially important because the specification requires them to remain recorded and retained.

## API Operations

---

## 1. Delete Customer
 
### Description
Deletes an existing customer when the customer has no active accounts.
 
### HTTP Contract
- Method: `DELETE`
- Path: `/customers/{customerId}`
- Request Body: None
- Expected Response Codes:
  - `200` Customer deleted successfully
  - `400` Invalid `customerId` format
  - `401` Unauthenticated or caller not authorized to access this customer
  - `404` Customer not found
  - `409` Customer has active accounts and cannot be deleted
 
### Business Rules
- A customer can be deleted only if the customer exists.
- A customer with one or more active accounts cannot be deleted.
- Deleting a customer removes the customer from normal operational use.
- No implied account closure is performed by this operation.
 
### Security Constraints
- The caller must be authenticated.
- Required permission: `CUSTOMER:DELETE`.
- This operation is restricted to `ADMIN` callers only.
- `CUSTOMER` callers are not permitted to delete customer resources, including their own, and receive `401`.
 
### Validation Rules
- `customerId` must be a valid numeric value.
- `customerId` must be greater than 0.
 
### Error Mapping
- Invalid `customerId` format → `400`, `field="customerId"`
- Customer not found → `404`
- Customer has active accounts → `409`
 
### Edge Cases
- If the customer was already deleted or does not exist → return `404`
- If multiple delete requests are made concurrently → only one succeeds, others return `404`

### Development Perspective
- This operation must distinguish between business-state conflict (`409`) and missing or already-removed resource (`404`).
- Deletion must remove the customer from normal operational use while preserving consistency with the retention policy.
- In the agreed backend stack, this maps to an authenticated Spring Boot delete endpoint with authorization enforcement and persistent state change rather than a transient in-memory action.

### QA Perspective
- Verify the full delete decision tree: existing customer with no active accounts, existing customer with active accounts, missing customer, already-deleted customer, and unauthorized caller.
- Include a repeat-delete scenario to confirm that only the first successful deletion returns `200`.
 
### Acceptance Criteria
 
#### Success (200)
```json
{
  "message": "Customer deleted successfully"
}
```
#### Positive Scenario
- Given an `ADMIN` caller and an existing customer with no active accounts  
- When a delete customer request is submitted with that `customerId`  
- Then the API returns `200` and the customer is deleted and no longer available for normal operational use  
 
#### Negative Scenario 1
- Given no customer exists for the supplied `customerId`  
- When a delete customer request is submitted  
- Then the API returns `404` with an `ErrorResponse`  
 
#### Negative Scenario 2
- Given an existing customer with one or more active accounts  
- When a delete customer request is submitted  
- Then the API returns `409` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller
- When a delete customer request is submitted
- Then the API returns `401` with an `ErrorResponse`

---
 
## 2. Create Account
 
### Description
Creates a new account for an existing customer as either a checking or savings account.
 
### HTTP Contract
- Method: `POST`
- Path: `/customers/{customerId}/accounts`
- Request Body:
```json
{
  "accountType": "CHECKING | SAVINGS",
  "balance": "BigDecimal",
  "interestRate": "BigDecimal (required for SAVINGS, forbidden for CHECKING)",
  "nextCheckNumber": "long (required for CHECKING, forbidden for SAVINGS)"
}
```
- Expected Response Codes:
  - `201` Account created successfully
  - `400` Invalid path or malformed request
  - `401` Unauthenticated or caller not authorized to access this customer
  - `404` Customer not found
  - `409` Account creation conflicts with business state
  - `422` Semantic validation failure (e.g., incompatible fields)
 
### Business Rules
- Account creation requires an existing customer.
- `accountType` is required and must be `CHECKING` or `SAVINGS`.
- Newly created accounts MUST have `status=ACTIVE`.
- `balance` is required and must be greater than or equal to `0`.
- For `SAVINGS`, `interestRate` is required and `nextCheckNumber` must not be provided.
- For `CHECKING`, `nextCheckNumber` is required and `interestRate` must not be provided.

### Security Constraints
- The caller must be authenticated.
- Required permission: `ACCOUNT:CREATE`.
- `CUSTOMER` callers must own the specified customer.
- `ADMIN` callers may access any customer resource regardless of ownership.

### Validation Rules
- `customerId` must be a valid numeric value greater than `0`.
- `accountType` is required and must be one of `CHECKING`, `SAVINGS`.
- `balance` is required and must be a non-negative decimal value with at most two decimal places.
- For `SAVINGS`: `interestRate` is required; `nextCheckNumber` must not be present.
- For `CHECKING`: `nextCheckNumber` is required; `interestRate` must not be present.

### Error Mapping
- Invalid `customerId` format → `400`, `field="customerId"`
- Missing or invalid `accountType` → `400`, `field="accountType"`
- Missing or negative `balance` → `400`, `field="balance"`
- Customer not found → `404`
- `interestRate` provided for `CHECKING` account → `422`, `field="interestRate"`
- `nextCheckNumber` provided for `SAVINGS` account → `422`, `field="nextCheckNumber"`
- `interestRate` missing for `SAVINGS` account → `422`, `field="interestRate"`
- `nextCheckNumber` missing for `CHECKING` account → `422`, `field="nextCheckNumber"`

### Edge Cases
- A customer with no prior accounts may create their first account using this operation.
- `balance` of exactly `0.00` is valid at creation time.

### Development Perspective
- This operation must enforce account-type compatibility rules exactly as specified and return `422` for well-formed but semantically incompatible field combinations.
- A successful response must reflect the created account state using the shared Account definition.
- In the agreed stack, developers should expect a Spring Boot POST endpoint, validated request DTOs, persisted account state in the configured environment database, and a React form client that submits the defined contract.

### QA Perspective
- Test both account types, including required-field and forbidden-field combinations for each.
- Cover first-account creation, zero-balance creation, ownership checks, and field-specific validation outcomes.

### Acceptance Criteria
 
#### Positive Scenario
- Given an existing customer
- When a create account request is submitted with `accountType=SAVINGS`, non-negative `balance`, and valid `interestRate`
- Then the API returns `201` and the created account details

#### Negative Scenario 1
- Given no customer exists for the supplied `customerId`
- When a create account request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 2
- Given an existing customer
- When a create account request is submitted with `accountType=CHECKING` and `interestRate` included in the body
- Then the API returns `422` with an `ErrorResponse` indicating `field="interestRate"`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified customer
- When a create account request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 3. Retrieve Account Details

### Description
Returns the details of a specific account.

### HTTP Contract
- Method: `GET`
- Path: `/accounts/{accountId}`
- Request Body: None
- Expected Response Codes:
  - `200` Account retrieved successfully
  - `400` Invalid `accountId` format
  - `401` Unauthenticated or caller not authorized to access this account
  - `404` Account not found

### Business Rules
- The account must exist to be retrieved.
- Only accounts with `status=ACTIVE` are available through normal operational retrieval.
- Returned data must include account fields defined in the `Account` domain object.

### Security Constraints
- The caller must be authenticated.
- Required permission: `ACCOUNT:READ`.
- `CUSTOMER` callers must own the specified account.
- `ADMIN` callers may access any account regardless of ownership.

### Validation Rules
- `accountId` must be a valid numeric value greater than `0`.

### Error Mapping
- Invalid `accountId` format → `400`, `field="accountId"`
- Account not found → `404`

### Edge Cases
- Requesting a previously deleted account by its former `accountId` returns `404`.

### Development Perspective
- Retrieval is read-only but still subject to authentication, permission, ownership, and deleted-resource visibility rules.
- The returned representation must include the account fields defined in Shared Definitions and must not surface deleted accounts through normal operational access.
- In the agreed stack, this is expected to be delivered through a secured Spring Boot GET endpoint consumed by a React account-detail view.

### QA Perspective
- Verify successful retrieval, missing account, deleted account, invalid path input, and ownership failure.
- Confirm the payload shape matches the shared Account object contract.

### Acceptance Criteria

#### Positive Scenario
- Given an existing account
- When an account details request is submitted with that `accountId`
- Then the API returns `200` with the account details

#### Negative Scenario 1
- Given no account exists for the supplied `accountId`
- When an account details request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 2
- Given an invalid non-numeric `accountId` in the path
- When an account details request is submitted
- Then the API returns `400` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified account
- When an account details request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 4. List Customer Accounts

### Description
Returns all accounts belonging to a specific customer.

### HTTP Contract
- Method: `GET`
- Path: `/customers/{customerId}/accounts`
- Request Body: None
- Expected Response Codes:
  - `200` Accounts listed successfully
  - `400` Invalid `customerId` format
  - `401` Unauthenticated or caller not authorized to access this customer
  - `404` Customer not found

### Business Rules
- The customer must exist to list accounts.
- Response includes zero or more active accounts.
- If the customer exists and has no accounts, an empty list is returned with `200`.

### Security Constraints
- The caller must be authenticated.
- Required permissions: `CUSTOMER:READ` and `ACCOUNT:READ`.
- `CUSTOMER` callers must own the specified customer.
- `ADMIN` callers may access any customer resource regardless of ownership.

### Validation Rules
- `customerId` must be a valid numeric value greater than `0`.

### Error Mapping
- Invalid `customerId` format → `400`, `field="customerId"`
- Customer not found → `404`

### Edge Cases
- A customer that exists but has no accounts returns `200` with an empty list; this is not an error.

### Development Perspective
- This operation must distinguish between a missing customer (`404`) and an existing customer with zero accounts (`200` with empty list).
- Authorization must be evaluated against the customer resource being queried, not inferred solely from returned account data.
- In the agreed stack, this should surface as a secured collection-style GET endpoint and a frontend list view that handles both populated and empty successful states.

### QA Perspective
- Include coverage for empty-list success, non-empty success, missing customer, invalid path input, and ownership failure.
- Verify that an empty result is represented as a valid successful response rather than an error condition.

### Acceptance Criteria

#### Positive Scenario
- Given an existing customer with two accounts
- When a list customer accounts request is submitted
- Then the API returns `200` with both accounts in the response

#### Negative Scenario 1
- Given no customer exists for the supplied `customerId`
- When a list customer accounts request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 2
- Given an invalid non-numeric `customerId` in the path
- When a list customer accounts request is submitted
- Then the API returns `400` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified customer
- When a list customer accounts request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 5. Update Account

### Description
Updates mutable account attributes while preserving account type constraints.

This operation supports updating only the account fields that are explicitly marked as mutable for the account's existing `accountType`.

### HTTP Contract
- Method: `PUT`
- Path: `/accounts/{accountId}`
- Request Body:
```json
{
  "interestRate": "BigDecimal (SAVINGS only)",
  "nextCheckNumber": "long (CHECKING only)"
}
```
- Expected Response Codes:
  - `200` Account updated successfully
  - `400` Invalid path or malformed request
  - `401` Unauthenticated or caller not authorized to access this account
  - `404` Account not found
  - `422` Semantic validation failure

### Business Rules
- The account must exist to be updated.
- Only accounts with `status=ACTIVE` may be updated.
- This is a partial update of mutable account attributes only; fields omitted from the request remain unchanged.
- Mutable fields by account type:
  - `SAVINGS` account: `interestRate` only.
  - `CHECKING` account: `nextCheckNumber` only.
- Immutable fields for all accounts through this operation include `accountId`, `customerId`, `accountType`, `balance`, `createdAt`, and any transaction history.
- Requests that violate account-type field compatibility are rejected.
- If a request includes fields outside the defined request contract for this operation, the request is rejected.
- A successful update MUST refresh the account's `updatedAt` value.

### Field Update Matrix

| Field | CHECKING | SAVINGS | Notes |
|---|---|---|---|
| `interestRate` | Not allowed | Updatable | Rejected for `CHECKING` accounts |
| `nextCheckNumber` | Updatable | Not allowed | Rejected for `SAVINGS` accounts |
| `accountType` | Not allowed | Not allowed | Immutable after account creation |
| `balance` | Not allowed | Not allowed | Changed only by Deposit, Withdraw, or Transfer |
| `customerId` | Not allowed | Not allowed | Account ownership cannot be reassigned by this API |
| `createdAt` | Not allowed | Not allowed | System-managed |
| `updatedAt` | Not allowed in request | Not allowed in request | System-managed and updated by the API on success |

### Security Constraints
- The caller must be authenticated.
- Required permission: `ACCOUNT:UPDATE`.
- `CUSTOMER` callers must own the specified account.
- `ADMIN` callers may access any account regardless of ownership.

### Validation Rules
- `accountId` must be a valid numeric value greater than `0`.
- The request body must contain at least one of `interestRate` or `nextCheckNumber`; an empty body is rejected.
- The request body MUST NOT contain fields other than `interestRate` and `nextCheckNumber`.
- `interestRate` is only valid for `SAVINGS` accounts.
- `interestRate`, when provided, must be a non-negative decimal value with at most 4 decimal places.
- `nextCheckNumber` is only valid for `CHECKING` accounts.
- `nextCheckNumber`, when provided, must be a whole number greater than or equal to `0` and greater than the current stored `nextCheckNumber`.

### Error Mapping
- Invalid `accountId` format → `400`, `field="accountId"`
- Empty request body (no updatable fields provided) → `400`
- Unsupported or immutable field submitted in request body → `400`, `field` set to the offending field when identifiable
- Account not found → `404`
- Negative `interestRate` or `interestRate` with more than 4 decimal places → `422`, `field="interestRate"`
- `interestRate` submitted for a `CHECKING` account → `422`, `field="interestRate"`
- `nextCheckNumber` less than `0` or not greater than the current stored value → `422`, `field="nextCheckNumber"`
- `nextCheckNumber` submitted for a `SAVINGS` account → `422`, `field="nextCheckNumber"`

### Edge Cases
- A request body containing only fields not applicable to the account type (e.g., `interestRate` on a `CHECKING` account) is treated as a `422`, not a `400`.
- If both `interestRate` and `nextCheckNumber` are submitted, the API evaluates each field against the existing account type and rejects the request if any submitted field is not permitted for that account.
- If an allowed mutable field is omitted, its current stored value remains unchanged.
- A checking-account update that supplies the same `nextCheckNumber` as the current stored value is rejected.

### Development Perspective
- This operation must preserve the distinction between malformed requests (`400`) and well-formed but semantically invalid updates (`422`).
- Only the documented mutable fields may change, and successful updates must leave all other account fields unchanged except system-managed metadata such as `updatedAt`.
- In the agreed stack, this maps to a secured Spring Boot update endpoint with request-body validation and a frontend edit flow that exposes only the permitted mutable fields.

### QA Perspective
- Verify partial-update behaviour, immutable-field rejection, wrong-account-type field rejection, boundary validation for `interestRate`, and monotonic validation for `nextCheckNumber`.
- Confirm that successful updates change only the permitted field and preserve unrelated account values.

### Acceptance Criteria

#### Positive Scenario
- Given an existing savings account
- When an update account request is submitted with a valid `interestRate`
- Then the API returns `200` and the updated account details

#### Positive Scenario 2
- Given an existing checking account with current `nextCheckNumber=1200`
- When an update account request is submitted with `nextCheckNumber=1201`
- Then the API returns `200`, `nextCheckNumber` is updated to `1201`, and no other account fields are changed

#### Negative Scenario 1
- Given an existing checking account
- When an update account request is submitted with `interestRate`
- Then the API returns `422` with an `ErrorResponse` indicating `field="interestRate"`

#### Negative Scenario 2
- Given no account exists for the supplied `accountId`
- When an update account request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified account
- When an update account request is submitted
- Then the API returns `401` with an `ErrorResponse`

#### Negative Scenario 4
- Given an existing account
- When an update account request is submitted with an immutable or unsupported field such as `balance`
- Then the API returns `400` with an `ErrorResponse`

#### Negative Scenario 5
- Given an existing checking account with current `nextCheckNumber=1200`
- When an update account request is submitted with `nextCheckNumber=1200`
- Then the API returns `422` with an `ErrorResponse` indicating `field="nextCheckNumber"`

---

## 6. Delete Account

### Description
Deletes an account when account closure rules are satisfied.

### HTTP Contract
- Method: `DELETE`
- Path: `/accounts/{accountId}`
- Request Body: None
- Expected Response Codes:
  - `200` Account deleted successfully
  - `400` Invalid `accountId` format
  - `401` Unauthenticated or caller not authorized to access this account
  - `404` Account not found
  - `409` Account cannot be deleted due to business constraints

### Business Rules
- The account must exist to be deleted.
- Accounts with non-zero balance cannot be deleted.
- Account deletion sets `status=CLOSED` and removes the account from normal operational use.

### Security Constraints
- The caller must be authenticated.
- Required permission: `ACCOUNT:DELETE`.
- This operation is restricted to `ADMIN` callers only.
- `CUSTOMER` callers are not permitted to delete accounts, including their own, and receive `401`.

### Validation Rules
- `accountId` must be a valid numeric value greater than `0`.

### Error Mapping
- Invalid `accountId` format → `400`, `field="accountId"`
- Account not found → `404`
- Account has a non-zero balance → `409`

### Edge Cases
- An account with a balance of exactly `0.00` satisfies the deletion constraint and is allowed.
- Requesting deletion of an already deleted account returns `404`.
- Concurrent delete requests: only the first succeeds; subsequent requests return `404`.

### Development Perspective
- This operation must enforce zero-balance closure rules before removing the account from normal operational use.
- The delete outcome must remain consistent with retention and deleted-resource visibility requirements.
- In the agreed backend stack, this should be implemented as a persistent delete-state transition behind a secured delete endpoint.

### QA Perspective
- Test zero-balance success, non-zero conflict, missing account, already-deleted account, repeat delete, and unauthorized caller.
- Confirm the account is no longer retrievable through normal operational endpoints after a successful delete.

### Acceptance Criteria

#### Positive Scenario
- Given an `ADMIN` caller and an existing account with `balance=0`
- When a delete account request is submitted
- Then the API returns `200` and the account is deleted and no longer available for normal operational use

#### Negative Scenario 1
- Given an existing account with non-zero balance
- When a delete account request is submitted
- Then the API returns `409` with an `ErrorResponse`

#### Negative Scenario 2
- Given no account exists for the supplied `accountId`
- When a delete account request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller
- When a delete account request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 7. Deposit

### Description
Credits funds to an account and records a successful deposit transaction.

### HTTP Contract
- Method: `POST`
- Path: `/accounts/{accountId}/deposit`
- Headers: `Idempotency-Key: <uuid>` (required; uniquely identifies this logical request)
- Request Body:
```json
{
  "amount": "BigDecimal",
  "description": "string"
}
```
- Expected Response Codes:
  - `200` Deposit completed successfully
  - `400` Invalid request format
  - `401` Unauthenticated or caller not authorized to access this account
  - `404` Account not found
  - `422` Invalid business input (e.g., non-positive amount)

### Business Rules
- The target account must exist.
- Only accounts with `status=ACTIVE` may accept deposits.
- `amount` is required and must be greater than `0`.
- A successful deposit increases the account balance by `amount`.
- A successful deposit MUST create a `Transaction` record with `direction=CREDIT` and `status=SUCCESS`.
- Failed deposit attempts MUST create a `Transaction` record with `status=FAILED`.
- Duplicate deposit retries with the same idempotency key MUST return the original outcome without applying the balance change again.

### Security Constraints
- The caller must be authenticated.
- Required permission: `TRANSACTION:DEPOSIT`.
- `CUSTOMER` callers must own the specified account.
- `ADMIN` callers may access any account regardless of ownership.

### Validation Rules
- `accountId` must be a valid numeric value greater than `0`.
- `amount` is required and must be greater than `0` with at most two decimal places.
- `Idempotency-Key` header is required and must be a non-empty string (UUID recommended).

### Error Mapping
- Invalid `accountId` format → `400`, `field="accountId"`
- Missing or non-positive `amount` → `422`, `field="amount"`
- Account not found → `404`

### Edge Cases
- A second request with the same `Idempotency-Key` returns the original response without re-applying the balance change.
- A failed deposit (e.g., due to a system error) still persists a `Transaction` record with `status=FAILED`.

### Development Perspective
- Deposit must preserve three observable outcomes together: balance change on success, idempotent replay behaviour, and transaction persistence for both success and failure.
- Semantic amount validation belongs to the documented `422` path rather than generic malformed-request handling.
- In the agreed stack, this is expected to be implemented as a secured Spring Boot POST endpoint backed by persistent account and transaction updates, with the React client supplying the `Idempotency-Key` header.

### QA Perspective
- Verify successful balance increase, amount validation, missing account, ownership failure, duplicate idempotency-key replay, and failed-operation transaction recording.
- Confirm that replays return the original outcome without applying the deposit a second time.

### Acceptance Criteria

#### Positive Scenario
- Given an existing account with balance `100.00`
- When a deposit request is submitted with `amount=25.00`
- Then the API returns `200`, account balance becomes `125.00`, and a deposit transaction is recorded with `direction=CREDIT` and `status=SUCCESS`

#### Negative Scenario 1
- Given an existing account
- When a deposit request is submitted with `amount=0`
- Then the API returns `422` with an `ErrorResponse` indicating `field="amount"`

#### Negative Scenario 2
- Given no account exists for the supplied `accountId`
- When a deposit request is submitted
- Then the API returns `404` with an `ErrorResponse`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified account
- When a deposit request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 8. Withdraw

### Description
Debits funds from an account and records the withdrawal transaction outcome.

### HTTP Contract
- Method: `POST`
- Path: `/accounts/{accountId}/withdraw`
- Headers: `Idempotency-Key: <uuid>` (required; uniquely identifies this logical request)
- Request Body:
```json
{
  "amount": "BigDecimal",
  "description": "string"
}
```
- Expected Response Codes:
  - `200` Withdrawal completed successfully
  - `400` Invalid request format
  - `401` Unauthenticated or caller not authorized to access this account
  - `404` Account not found
  - `409` Insufficient funds conflict
  - `422` Invalid business input (e.g., non-positive amount)

### Business Rules
- The target account must exist.
- Only accounts with `status=ACTIVE` may allow withdrawals.
- `amount` is required and must be greater than `0`.
- Balance cannot become negative.
- A successful withdrawal decreases the account balance by `amount`.
- A successful withdrawal MUST create a `Transaction` record with `direction=DEBIT` and `status=SUCCESS`.
- A failed withdrawal attempt MUST create a `Transaction` record with `status=FAILED`.
- Concurrent withdrawal/transfer requests against the same account are allowed and are evaluated against the latest committed balance at execution time.
- Duplicate withdraw retries with the same idempotency key MUST return the original outcome without applying the balance change again.

### Security Constraints
- The caller must be authenticated.
- Required permission: `TRANSACTION:WITHDRAW`.
- `CUSTOMER` callers must own the specified account.
- `ADMIN` callers may access any account regardless of ownership.

### Validation Rules
- `accountId` must be a valid numeric value greater than `0`.
- `amount` is required and must be greater than `0` with at most two decimal places.
- `Idempotency-Key` header is required and must be a non-empty string (UUID recommended).

### Error Mapping
- Invalid `accountId` format → `400`, `field="accountId"`
- Missing or non-positive `amount` → `422`, `field="amount"`
- Account not found → `404`
- Withdrawal would make balance negative → `409`

### Edge Cases
- A second request with the same `Idempotency-Key` returns the original response without re-applying the balance change, regardless of whether the original succeeded or failed.
- Concurrent withdrawals from the same account are evaluated independently against the latest committed balance; a later request may succeed or fail depending on the remaining balance after the first commits.
- A failed withdrawal still persists a `Transaction` record with `status=FAILED`.

### Development Perspective
- Withdraw must preserve validation, idempotency, balance protection, and failed-transaction persistence as part of one coherent contract.
- Concurrency handling is implementation-defined, but the externally visible result must match the latest committed balance rules in this specification.
- In the agreed stack, developers should expect secured Spring Boot business logic using persistent balance state and frontend submission through a dedicated withdraw form or action.

### QA Perspective
- Verify success, insufficient funds, invalid amount, ownership failure, duplicate replay after success, duplicate replay after failure, and concurrency-sensitive outcomes.
- Confirm the balance remains unchanged on failed withdrawal attempts.

### Acceptance Criteria

#### Positive Scenario
- Given an existing account with balance `100.00`
- When a withdraw request is submitted with `amount=20.00`
- Then the API returns `200`, account balance becomes `80.00`, and a withdrawal transaction is recorded with `direction=DEBIT` and `status=SUCCESS`

#### Negative Scenario 1
- Given an existing account with balance `50.00`
- When a withdraw request is submitted with `amount=75.00`
- Then the API returns `409` with an `ErrorResponse` and the account balance remains unchanged

#### Negative Scenario 2
- Given an existing account
- When a withdraw request is submitted with `amount=-5.00`
- Then the API returns `422` with an `ErrorResponse` indicating `field="amount"`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the specified account
- When a withdraw request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## 9. Transfer Funds

### Description
Transfers funds from one account to another as a single atomic operation and records transfer transactions.

### HTTP Contract
- Method: `POST`
- Path: `/accounts/transfer`
- Headers: `Idempotency-Key: <uuid>` (required; uniquely identifies this logical request)
- Request Body:
```json
{
  "fromAccountId": "long",
  "toAccountId": "long",
  "amount": "BigDecimal",
  "description": "string"
}
```
- Expected Response Codes:
  - `200` Transfer completed successfully
  - `400` Invalid request format
  - `401` Unauthenticated or caller not authorized to access source or destination account
  - `404` Source or destination account not found
  - `409` Transfer conflict (e.g., insufficient funds)
  - `422` Invalid business input (e.g., same source/destination or non-positive amount)

### Business Rules
- Source and destination accounts must both exist.
- Source and destination accounts must both have `status=ACTIVE`.
- `fromAccountId` and `toAccountId` must be different.
- `amount` is required and must be greater than `0`.
- Source account balance cannot become negative.
- Transfer is atomic: either both debit and credit succeed or neither is applied.
- Successful transfers MUST create `Transaction` entries for both accounts: a `DEBIT` transaction on the source account and a `CREDIT` transaction on the destination account. Successful transfer transaction records MUST have `status=SUCCESS`.
- Failed transfer attempts MUST create transaction records for auditability with `status=FAILED`.
- Concurrent transfer/withdrawal requests against the same source account are allowed and are evaluated against the latest committed balance at execution time.
- Duplicate transfer retries with the same idempotency key MUST return the original outcome without applying the debit/credit again.

### Security Constraints
- The caller must be authenticated.
- Required permission: `TRANSACTION:TRANSFER`.
- `CUSTOMER` callers must own the source account (`fromAccountId`). Ownership of the destination account is not required.
- `ADMIN` callers may initiate transfers between any accounts regardless of ownership.

### Validation Rules
- `fromAccountId` and `toAccountId` must each be valid numeric values greater than `0`.
- `fromAccountId` and `toAccountId` must not be equal.
- `amount` is required and must be greater than `0` with at most two decimal places.
- `Idempotency-Key` header is required and must be a non-empty string (UUID recommended).

### Error Mapping
- Invalid `fromAccountId` or `toAccountId` format → `400`, `field` set to the offending field
- Missing or non-positive `amount` → `422`, `field="amount"`
- `fromAccountId` equals `toAccountId` → `422`, `field="toAccountId"`
- Source account not found → `404`
- Destination account not found → `404`
- Insufficient funds on source account → `409`

### Edge Cases
- A transfer between two accounts owned by the same customer is valid and permitted.
- If atomicity fails after the debit is recorded, neither balance is modified and transaction records are still persisted as required for failed outcomes.
- A second request with the same `Idempotency-Key` returns the original response without re-applying the debit/credit.
- If the destination account does not exist, the source account balance is not modified.

### Development Perspective
- Transfer must preserve atomicity, idempotency, source-account ownership rules for `CUSTOMER`, and unchanged balances on failure as observable outcomes.
- A transfer request is not valid unless both account references and the amount satisfy the stated validation rules.
- In the agreed stack, this maps to a secured Spring Boot transfer endpoint coordinating persistent updates to both accounts and transactions, with the React client acting as the request initiator.

### QA Perspective
- Verify successful transfer, insufficient-funds failure, same-account rejection, missing source, missing destination, unauthorized source ownership, duplicate replay, and rollback behaviour.
- Include scenarios where the destination account is owned by another customer, since destination ownership is explicitly not required for `CUSTOMER` callers.

### Acceptance Criteria

#### Positive Scenario
- Given source account balance `200.00` and destination account balance `50.00`
- When a transfer request is submitted with `amount=75.00`
- Then the API returns `200`, source balance becomes `125.00`, destination balance becomes `125.00`, and transfer transactions are recorded as `SUCCESS` with `direction=DEBIT` for the source account and `direction=CREDIT` for the destination account

#### Negative Scenario 1
- Given source account balance `30.00` and destination account exists
- When a transfer request is submitted with `amount=45.00`
- Then the API returns `409` with an `ErrorResponse` and both balances remain unchanged

#### Negative Scenario 2
- Given an existing account id `1001`
- When a transfer request is submitted with `fromAccountId=1001` and `toAccountId=1001`
- Then the API returns `422` with an `ErrorResponse` indicating `field="toAccountId"`

#### Negative Scenario 3
- Given a `CUSTOMER` caller who does not own the source account
- When a transfer request is submitted
- Then the API returns `401` with an `ErrorResponse`

---

## Cross-Cutting Validation and Error Semantics

- All endpoints require an authenticated caller. Operations on customer or account resources are permitted only when the caller is authorised to access them.
- `CUSTOMER` callers may use only the explicitly allowed self-service features listed in Actors and Roles, and only on resources they own. `CUSTOMER` callers are never permitted to delete customers or delete accounts, even when they own the resource.
- `ADMIN` callers may use all operations in scope and bypass ownership checks. A `401` for ownership or privilege reasons is never returned to an `ADMIN` caller.
- All monetary amounts are expressed in a single system currency and MUST use a scale of exactly 2 decimal places.
- Error responses for `400`, `401`, `404`, `409`, and `422` use the shared `ErrorResponse` schema.
- `400` is used for malformed syntax, type mismatches, and invalid path parameter format.
- `401` is used when the caller is unauthenticated (no valid token), when a `CUSTOMER` caller attempts to access a resource they do not own, or when a `CUSTOMER` caller attempts to invoke an admin-only operation.
- `401` is also used when an authenticated caller lacks one or more required permissions for the target endpoint.
- `404` is used when requested resources do not exist.
- `404` is also used when an account exists as a retained record but is no longer available for normal operational access because it has `status=CLOSED`.
- `409` is used for business-state conflicts (e.g., insufficient funds, delete prevented by active accounts).
- `422` is used for semantically invalid but well-formed inputs (e.g., negative amount, incompatible field combinations).
- `200` and `201` success responses return the updated or created resource representation relevant to the operation.

### Development Perspective
- These semantics must be applied consistently across all operations so equivalent failure categories produce the same class of response.
- When an operation-specific rule defines a more precise status or field mapping, that specific rule governs the final behaviour.
- For the agreed implementation stack, this consistency should be visible across controllers, exception handling, and frontend error rendering rather than being left to individual endpoints to interpret differently.

### QA Perspective
- Use this section to build cross-endpoint consistency checks for status-code usage, `ErrorResponse` shape, field attribution, and ownership-versus-authentication outcomes.
- A defect in one endpoint's error semantics should be evaluated as a potential regression risk across the rest of the API.
