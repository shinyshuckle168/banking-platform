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
