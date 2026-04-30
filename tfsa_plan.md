# Interest Rate Fetching Feature: TFSA

**Goal:** Fetch and display real-time interest rates for TFSA from a mock API

**Approach:**

- Mock API layer returns hardcoded TFSA rate (e.g. TFSA: 3.75%)
- A service module handles the fetch and error handling
- UI layer consumes the service and renders the TFSA rate

**Dependencies:** Existing HTTP client or fetch utility in the project

**Risks:** Mock data may not reflect real rate structures — flag for future real API swap

**Milestone:** Feature complete when TFSA rate renders correctly with loading/error states
# Implementation Plan: TFSA Account Creation

**Branch**: `spec/tfsa` | **Date**: 30 April 2026 | **Spec**: tfsa_spec.md
**Input**: Feature specification from `tfsa_spec.md`

## Summary

Enable eligible customers to open a TFSA account. Enforce KYC, age (18+), and one TFSA per customer. Track and validate annual contribution room. All validation rules and constraints are clearly defined in the spec.

## Technical Context

**Language/Version**: Java 17 (Spring Boot)
**Primary Dependencies**: Spring Boot, JPA/Hibernate, JWT, Maven
**Storage**: Relational DB (e.g., PostgreSQL or H2 for dev)
**Testing**: JUnit, Mockito
**Target Platform**: Linux server, Docker/Kubernetes
**Project Type**: Web service (REST API)
**Constraints**: One TFSA per customer, age 18+, KYC required, contribution room enforced
**Scale/Scope**: Multi-user, production banking platform

## Constitution Check

- All business rules and validation logic must be enforced in both backend and API layers.
- All error conditions must return correct HTTP status and error codes.
- All actions must be logged and auditable.

## Project Structure

### Documentation (this feature)

```
./
├── tfsa_spec.md    # Feature specification
├── tfsa_plan.md    # Implementation plan (this file)
├── tfsa_tasks.md   # Task breakdown
```

### Source Code (repository root)

```
backend/src/main/java/com/group1/banking/
backend/src/main/resources/
backend/src/test/java/com/group1/banking/
```

## Next Steps

1. Research any unclear technical context (e.g., DB schema, integration points).
2. Define data model and API contracts for TFSA flows.
3. Implement validation logic for TFSA creation (age, KYC, one per customer, contribution room).
4. Write tests for all business rules and error conditions.
5. Document quickstart and update contracts as needed.

---

## Frontend Implementation Plan

### Overview
The frontend will provide a user interface for customers to:
- Open a TFSA account (if eligible)
- View TFSA account details and contribution room
- Receive error messages for business rule violations (e.g., already has TFSA, underage, KYC not verified, over contribution room)

### Technical Context
- **Language/Framework**: React (or your chosen SPA framework)
- **State Management**: Redux, Context API, or similar
- **API Integration**: RESTful calls to backend endpoints
- **Testing**: Jest, React Testing Library
- **Target Platform**: Web (desktop/mobile responsive)

### Project Structure (example)
```
frontend/
├── src/
│   ├── components/
│   ├── pages/
│   ├── api/
│   ├── store/
│   └── App.js
├── public/
└── package.json
```

### Next Steps
1. Design wireframes for TFSA flows
2. Scaffold frontend project and core pages/components
3. Implement API integration for TFSA endpoints
4. Add validation and error handling for all business rules
5. Write tests for all user flows and edge cases
6. Document usage and provide a quickstart for running the frontend
---
## Backend Implementation Plan: TFSA Account Creation

**Branch**: `spec/tfsa`  
**Created**: 28 April 2026  
**Input**: TFSA Account Feature Specification  

---

## Overview

- Provide REST API to create TFSA (Tax-Free Savings Account)
- Ensure strict eligibility validation (KYC, age, ownership)
- Prevent duplicate TFSA accounts per customer
- Enforce TFSA contribution rules and limits
- Maintain audit logs for all account creation actions
- Ensure RBAC (CUSTOMER vs ADMIN) is enforced
- Return consistent, structured error responses
- Guarantee compliance with financial regulations and constraints

---

## Technical Context

- Language: Java 17
- Framework: Spring Boot
- Architecture: Controller → Service → Repository
- Database: Relational DB (MySQL / H2 for dev)
- ORM: JPA / Hibernate
- Security: JWT-based authentication + RBAC
- Validation: Bean Validation (`@Valid`, `@NotNull`, `@Min`)
- Exception Handling: Global handler (`@ControllerAdvice`)
- Transactions: `@Transactional`
- Logging: SLF4J / Logback (audit + trace logs)
- Testing: JUnit + Mockito
- Build Tool: Maven
- Deployment: Docker / Kubernetes

---

## Entity Structure

### Customer Entity (simplified view)

```java
class Customer {
    String customerId;
    String name;
    LocalDate dateOfBirth;
    boolean kycVerified;
    List<Account> accounts;
}
TFSA Account Entity
class TFSAAccount {
    String tfsaId;
    String customerId;
    BigDecimal balance;
    BigDecimal contributionRoomUsed;
    BigDecimal annualContributionLimit;
    TFSAStatus status;
    LocalDateTime createdAt;
}
TFSA Transaction (optional but recommended)
class TFSAContribution {
    String transactionId;
    String tfsaId;
    BigDecimal amount;
    TFSAOperationType type; // CONTRIBUTION | WITHDRAWAL
    LocalDateTime timestamp;
}
Enums
enum TFSAStatus {
    ACTIVE,
    CLOSED
}

enum TFSAOperationType {
    CONTRIBUTION,
    WITHDRAWAL
}
```
## Business Rules
- Customer must be 18 years or older at time of account creation
- Customer must have verified KYC status
- Only one active TFSA account per customer
- TFSA account must be linked to exactly one customer
- Contribution must not exceed available contribution room
- Withdrawals restore contribution room in next calendar year
- TFSA balance cannot go negative
- ADMIN can bypass ownership validation
- CUSTOMER can only operate on their own customerId
- All actions must be auditable and logged
- No undocumented fields allowed in request payload

## API Specification
### Create TFSA Account
#### Endpoint
- POST /api/v1/customers/{customerId}/accounts
- Content-Type: application/json
Request
{
  "accountType": "TFSA",
  "dateOfBirth": "2000-05-12",
  "initialDeposit": 1000
}
Response — 201 Created
{
  "tfsaId": "TFSA12345",
  "customerId": "CUST001",
  "status": "ACTIVE",
  "balance": 1000,
  "annualContributionLimit": 7000,
  "contributionRoomUsed": 1000,
  "createdAt": "2026-04-28T10:00:00Z"
}
#### Rules
- Customer must be 18+
- KYC must be verified
- Only one active TFSA per customer
- Initial deposit processed via Transfer API (internal transfer or funding source)
- Account must be ACTIVE after creation
#### Errors
- 409 TFSA_ALREADY_EXISTS
- 404 CUSTOMER_NOT_FOUND
- 422 KYC_NOT_VERIFIED
- 400 UNDERAGE_CUSTOMER
- 400 INVALID_ACCOUNT_TYPE
### Get TFSA Account Details
#### Endpoint
- GET /api/v1/accounts/{tfsaId}
Response — 200 OK
{
  "tfsaId": "TFSA12345",
  "customerId": "CUST001",
  "balance": 2500,
  "contributionRoomUsed": 2500,
  "annualContributionLimit": 7000,
  "status": "ACTIVE",
  "createdAt": "2026-04-28T10:00:00Z"
}
#### Rules
- Must return latest balance and contribution status
- Only owner or ADMIN can access
#### Errors
- 404 TFSA_NOT_FOUND
- 403 FORBIDDEN
### Transfer INTO TFSA (Deposit / Contribution)
#### Endpoint (USES GLOBAL TRANSFER API)
- POST /accounts/transfer
Request
{
  "fromAccountId": "BANK_CHEQUING_001",
  "toAccountId": "TFSA12345",
  "amount": 1000,
  "currency": "CAD",
  "reference": "TFSA_CONTRIBUTION"
}
Response — 200 OK
{
  "transactionId": "TRX789",
  "status": "SUCCESS",
  "message": "Transfer completed successfully"
}
#### TFSA Post-Processing Rules
##### After successful transfer:
- Increase TFSA balance
- Increase contribution usage
- Validate against annual limit
##### TFSA Errors (Post Transfer Validation)
- 400 CONTRIBUTION_LIMIT_EXCEEDED
- 400 ACCOUNT_CLOSED
- 400 INVALID_TRANSFER_DESTINATION

### Withdraw from TFSA
#### Endpoint (USES GLOBAL TRANSFER API)
- POST /accounts/transfer
Request
{
  "fromAccountId": "TFSA12345",
  "toAccountId": "BANK_CHEQUING_001",
  "amount": 500,
  "currency": "CAD",
  "reference": "TFSA_WITHDRAWAL"
}
Response — 200 OK
{
  "transactionId": "TRX790",
  "status": "SUCCESS",
  "message": "Withdrawal completed successfully"
}
#### Rules
- Must not overdraw TFSA
- Must reduce balance
- Contribution room restored based on TFSA rules (next-year logic optional)
- Only ACTIVE accounts allowed
#### Errors
- 400 INSUFFICIENT_FUNDS
- 400 ACCOUNT_CLOSED
- 400 INVALID_WITHDRAWAL

### Get Contribution Room
#### Endpoint
- GET /api/v1/accounts/{tfsaId}/contribution-room
Response — 200 OK
{
  "tfsaId": "TFSA12345",
  "annualContributionLimit": 7000,
  "usedContribution": 2500,
  "remainingContributionRoom": 4500
}
###  Get Transaction History
#### Endpoint
- GET /api/v1/accounts/{tfsaId}/transactions
```
Response — 200 OK
{
  "tfsaId": "TFSA12345",
  "transactions": [
    {
      "transactionId": "TRX789",
      "type": "CONTRIBUTION",
      "amount": 1000,
      "timestamp": "2026-04-28T10:10:00Z"
    },
    {
      "transactionId": "TRX790",
      "type": "WITHDRAWAL",
      "amount": 500,
      "timestamp": "2026-04-28T11:00:00Z"
    }
  ]
}

### Close TFSA Account
#### Endpoint
- DELETE /api/v1/accounts/{tfsaId}
Response — 200 OK
{
  "message": "TFSA account closed successfully",
  "tfsaId": "TFSA12345",
  "closedAt": "2026-04-28T12:00:00Z"
}
#### Rules
- Balance must be 0 before closing
- No active pending transfers allowed
- Account marked CLOSED (soft delete)
- Must be audited
#### Errors
- 400 NON_ZERO_BALANCE
- 400 ACTIVE_TRANSACTIONS_EXIST
- 404 TFSA_NOT_FOUND
```
Global Error Response Format
{
  "timestamp": "2026-04-28T10:15:30Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "CONTRIBUTION_LIMIT_EXCEEDED",
  "message": "TFSA contribution limit exceeded",
  "path": "/api/v1/accounts/TFSA12345"
} ```
## Key Design Notes
- All money movement goes through /accounts/transfer
- TFSA only reacts to transfer results (post-processing)
- No direct balance mutation APIs allowed
- Ensures strong consistency and audit compliance
##### Business Flow
- Validate authentication (JWT required)
- Validate role (CUSTOMER or ADMIN)
- Fetch customer record
- Verify KYC status is true
- Validate age >= 18
- Check if TFSA already exists
- Validate contribution limit rules
- Create TFSA account with ACTIVE status
- Persist account and audit log entry
### Error Mapping
#### HTTP	Code	Condition
- 422	KYC_NOT_VERIFIED	Customer KYC is false
- 409	TFSA_ALREADY_EXISTS	Active TFSA already exists
- 400	UNDERAGE_APPLICANT	Age < 18
- 404	CUSTOMER_NOT_FOUND	Invalid customerId
- 401	UNAUTHORIZED_ACCESS	CUSTOMER accessing another customer’s data
- 400	INVALID_ACCOUNT_TYPE	accountType != TFSA
- 400	CONTRIBUTION_LIMIT_EXCEEDED	Initial deposit exceeds room
- 400	MISSING_REQUIRED_FIELDS	Missing DOB or required fields
- 503	SERVICE_UNAVAILABLE	System failure
## Non-Functional Requirements
- API response time < 2 seconds (95th percentile)
- 99.5% system availability (excluding maintenance)
- All requests must be logged for audit
- PII must not appear in logs
- RBAC must be dynamic (no hardcoded roles)
- All account creation must be traceable (who, when, outcome)
- System must support horizontal scaling
## Audit Logging Requirements
- Log customerId
- Log actor role (CUSTOMER / ADMIN)
- Log timestamp
- Log outcome (SUCCESS / FAILURE)
- Log age verification result
- Log KYC validation result
- Never log sensitive PII (full DOB, etc.)
## Notes
- This API is designed as a single unified account creation endpoint
- TFSA is enforced via accountType = TFSA
- Future extensibility allows adding RRSP, CHEQUING, SAVINGS under same endpoint
