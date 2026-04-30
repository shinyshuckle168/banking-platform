# Implementation Plan: RRSP Account with GIC Investment Support

**Branch**: `spec/rrsp` | **Date**: 29 April 2026 | **Spec**: rrsp_spec.md
**Input**: Feature specification from `rrsp_spec.md`

## Summary

Enable customers to open an RRSP account and invest in GICs. Each customer can have only one RRSP account, and each RRSP account can have only one GIC at a time. Sufficient funds are required before GIC creation. The interest rate for a GIC depends on the selected term. All validation rules and constraints are clearly defined in the spec.

## Technical Context

**Language/Version**: Java 17 (Spring Boot)
**Primary Dependencies**: Spring Boot, JPA/Hibernate, JWT, Maven
**Storage**: Relational DB (e.g., MySQL or H2 for dev)
**Testing**: JUnit, Mockito
**Target Platform**: Linux server, Docker/Kubernetes
**Project Type**: Web service (REST API)
**Constraints**: One RRSP per customer, one GIC per RRSP, sufficient funds required, interest rate by term
**Scale/Scope**: Multi-user, production banking platform

## Constitution Check

- All business rules and validation logic must be enforced in both backend and API layers.
- All error conditions must return correct HTTP status and error codes.
- All actions must be logged and auditable.

## Project Structure


### Documentation (this feature)

```
./
├── rrsp_spec.md    # Feature specification
├── rrsp_plan.md    # Implementation plan (this file)
├── rrsp_tasks.md   # Task breakdown
```

### Source Code (repository root)

```
backend/src/main/java/com/group1/banking/
backend/src/main/resources/
backend/src/test/java/com/group1/banking/
```

## Next Steps

1. Research any unclear technical context (e.g., DB schema, integration points).
2. Define data model and API contracts for RRSP and GIC flows.
3. Implement validation logic for GIC creation (one per account, sufficient funds, interest by term).
4. Write tests for all business rules and error conditions.
5. Document quickstart and update contracts as needed.

---

## Frontend Implementation Plan

### Overview
The frontend will provide a user interface for customers to:
- Open an RRSP account (if eligible)
- View RRSP account details and GIC investments
- Create a GIC investment (with validation for sufficient funds and one GIC per account)
- View GIC maturity status and history
- Receive error messages for business rule violations (e.g., already has RRSP, insufficient funds)

### Technical Context
- **Language/Framework**: React
- **API Integration**: RESTful calls to backend endpoints
- **Testing**: Jest, React Testing Library
- **Target Platform**: Web (desktop/mobile responsive)

### Project Structure
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
1. Design wireframes for RRSP and GIC flows
2. Scaffold frontend project and core pages/components
3. Implement API integration for RRSP and GIC endpoints
4. Add validation and error handling for all business rules
5. Write tests for all user flows and edge cases
6. Document usage and provide a quickstart for running the frontend

---
## Backend Implementation Plan
### Overview
Provide REST APIs to manage RRSP accounts and GIC investments
Allow customers to create exactly one RRSP account
Support creation of a single active GIC per RRSP account
Enable retrieval of RRSP and GIC details
Allow redemption of GIC investments with payout calculation
Support closing of RRSP accounts using status-based lifecycle
Enforce all business rules at the service layer
Ensure all operations are auditable and logged
Return standardized error responses across all APIs

### Technical Context
Language: Java 17
Framework: Spring Boot
Architecture: Controller → Service → Repository
Database: Relational database H2
ORM: JPA/Hibernate
Security: JWT-based authentication
Validation: Bean Validation (@Valid, @NotNull, @Min)
Exception Handling: Global handler using @ControllerAdvice
Transactions: Managed using @Transactional
Logging: SLF4J / Logback
Testing: JUnit and Mockito
Build Tool: Maven
Deployment: Docker / Kubernetes
### Entity Structure
#### RRSP Account
 RRSPAccount {
    - String rrspId;
    - String customerId;
    - BigDecimal balance;
    - RRSPStatus status;
    - LocalDateTime createdAt;
    - LocalDateTime closedAt;

#### GIC
    - String gicId;
    - String rrspId;
    - BigDecimal amount;
    - String term;
    - Double interestRate;
    - LocalDate startDate;
    - LocalDate maturityDate;
    - GICStatus status;

### Enums
#### enum RRSPStatus 
    - ACTIVE
    - CLOSED


#### enum GICStatus 
    - ACTIVE
    - REDEEMED

### Business Rules
- One RRSP account per customer
- One active GIC per RRSP account
- GIC creation requires sufficient RRSP balance
- Interest rate is derived from term and cannot be user-defined
- GIC amount is deducted from RRSP balance on creation
- GIC payout includes principal and interest on redemption
- RRSP cannot be closed if an active GIC exists
- RRSP cannot be closed if balance is not zero
- No hard deletes allowed for RRSP or GIC
- Use status-based lifecycle management for all entities

### API Specifications

#### US-201 — Create RRSP Account
#### Endpoint
POST /api/v1/rrsp
Content-Type: application/json
Request
{
  "customerId": "12345"
}
Response — 201 Created
{
  "rrspId": "RRSP001",
  "customerId": "12345",
  "balance": 0,
  "createdAt": "2026-04-29T10:00:00Z"
}
 #### Rules
- Customer must not already have an RRSP
- Customer must exist
 Errors
- 409 RRSP_ALREADY_EXISTS
- 404 CUSTOMER_NOT_FOUND
- 422 MISSING_REQUIRED_FIELD

#### US-202 — Get RRSP Details
#### Endpoint
GET /api/v1/rrsp/{customerId}
Response — 200 OK
{
  "rrspId": "RRSP001",
  "balance": 10000,
  "gic": {
    "gicId": "GIC001",
    "amount": 5000,
    "term": "1_YEAR",
    "interestRate": 5.0,
    "maturityDate": "2027-04-29"
  }
}
Rules
- Must return RRSP with active GIC if present
- Customer must own the account
Errors
- 404 RRSP_NOT_FOUND
- 403 FORBIDDEN

#### US-203 — Create GIC
Endpoint
POST /api/v1/rrsp/{rrspId}/gic
Content-Type: application/json
Request
{
  "amount": 5000,
  "term": "1_YEAR"
}
Response — 201 Created
{
  "gicId": "GIC001",
  "amount": 5000,
  "interestRate": 5.0,
  "term": "1_YEAR",
  "maturityDate": "2027-04-29"
}
Rules
- Only one active GIC per RRSP
- Balance must be sufficient
- Interest rate derived from term
- Amount deducted from RRSP
Errors
- 409 GIC_ALREADY_EXISTS
- 400 INSUFFICIENT_FUNDS
- 400 INVALID_TERM
- 404 RRSP_NOT_FOUND

#### US-204 — Get GIC Details
Endpoint
GET /api/v1/rrsp/{rrspId}/gic
Response — 200 OK
{
  "gicId": "GIC001",
  "amount": 5000,
  "interestRate": 5.0,
  "term": "1_YEAR",
  "maturityDate": "2027-04-29"
}
Rules
- Return only active GIC
Errors
- 404 GIC_NOT_FOUND
- 404 RRSP_NOT_FOUND

#### US-205 — Redeem GIC
Endpoint
POST /api/v1/rrsp/{rrspId}/gic/redeem
Response — 200 OK
{
  "message": "GIC redeemed successfully",
  "payoutAmount": 5250
}
Rules
- Only active GIC can be redeemed
- Payout includes interest
- Funds credited back to RRSP
- Status updated to REDEEMED
Errors
- 404 GIC_NOT_FOUND
- 400 INVALID_OPERATION

#### US-206 — Close RRSP Account
Endpoint
POST /api/v1/rrsp/{rrspId}/close
Response — 200 OK
{
  "message": "RRSP account closed successfully",
  "rrspId": "RRSP001",
  "closedAt": "2026-04-29T12:00:00Z"
}
Rules
- No active GIC must exist
- Balance must be zero
- Status updated to CLOSED
Errors
- 400 ACTIVE_GIC_EXISTS
- 400 NON_ZERO_BALANCE
- 400 RRSP_ALREADY_CLOSED
- 404 RRSP_NOT_FOUND

### Global Error Response
{
  "timestamp": "2026-04-29T10:15:30Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "INSUFFICIENT_FUNDS",
  "message": "Insufficient balance to create GIC",
  "path": "/api/v1/rrsp/RRSP001/gic"
}
