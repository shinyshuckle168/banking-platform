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
