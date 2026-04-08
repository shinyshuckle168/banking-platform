```md
# Implementation Plan: Bank App Identity and Customer Management

## Summary

Build BankApp as a static single-page web application backed by a separate Java API for authentication and customer management. The frontend remains deployable as static assets only; all secrets, password handling, session rules, access control, and data persistence stay on the backend. The implementation will be OpenAPI-first, enforce optimistic concurrency for customer updates, require passing contract, negative, and integration tests, and meet the explicit coverage gate defined by the specification.

## Technical Context

**Language/Version**: Frontend TypeScript 5.x with React 18 on Vite; Backend Java 21 with Spring Boot 3.3.x  
**Primary Dependencies**: React, React Router, Vite, Spring Boot Starter Web, Spring Boot Starter Security, Spring Boot Starter Validation, Spring Boot Starter Data JPA, Spring WebFlux for WebClient, springdoc-openapi, PostgreSQL driver, JUnit 5, Spring Boot Test, WireMock or equivalent reviewed mock-server library, JaCoCo  
**Storage**: PostgreSQL for persistent backend storage  
**Testing**: Frontend component and form tests; backend unit tests, API contract tests, negative tests, integration tests against mock servers, JaCoCo coverage gate matching the specification  
**Target Platform**: Static hosting for frontend plus independently deployed backend API  
**Project Type**: Web application with static frontend and API backend  
**Performance Goals**: Authentication and customer read/update flows complete within 2 seconds at p95 under normal load; keep frontend bundle and third-party scripts minimal  
**Constraints**:
- Never store or log passwords in plain text
- Never return `passwordHash` in any API response
- Never hardcode secrets such as JWT keys or database credentials
- Use `.env` for local secret configuration and ensure `.env` is gitignored
- Use `WebClient` for service-to-service calls, not `RestTemplate`
- Use JPA for persistence, not raw SQL bypasses
- Do not add response fields not defined in the OpenAPI contract
- Do not add AI-suggested dependencies without explicit security review
- Frontend must remain statically hostable with no SSR requirement
- Client-side configuration must remain safe to publish
**Scale/Scope**: Initial release supports registration, login, password reset request and confirm, token refresh, create customer, update customer profile, get customer detail, USER and ADMIN authorization, optimistic concurrency, and audited API contracts

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Static hosting fit: PASS  
  Frontend remains a static web application. All stateful auth and customer operations are delegated to the backend API. No server-side rendering or runtime-only frontend secrets are introduced.
- Accessibility and responsiveness: PASS  
  The feature includes accessible forms, keyboard-usable flows, semantic structure, readable contrast, and responsive layouts for auth and customer screens.
- Performance and asset impact: PASS  
  The frontend stack is intentionally limited. Third-party scripts are avoided unless justified and reviewed.
- Testing: PASS  
  The plan includes contract tests, negative tests, integration tests against mock servers, and a coverage gate enforced through JaCoCo.
- Client-side secret safety: PASS  
  Only publishable runtime values such as API base URL and feature flags are allowed on the client. JWT signing material, DB credentials, and reset-delivery secrets remain server-side only.

## Project Structure

```text
specs/001-bank-auth-customer/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md

frontend/
├── src/
│   ├── app/
│   ├── routes/
│   ├── pages/
│   │   ├── auth/
│   │   └── customer/
│   ├── components/
│   ├── api/
│   ├── forms/
│   ├── hooks/
│   ├── types/
│   └── test/
└── public/

backend/
├── src/main/java/com/bankapp/
│   ├── auth/
│   ├── customer/
│   ├── common/
│   ├── config/
│   ├── security/
│   └── integration/
├── src/main/resources/
└── src/test/java/com/bankapp/
```

## Phase 0: Research Plan

1. Confirm the static frontend plus separate backend boundary as the only constitution-compliant architecture for this feature.
2. Finalize sliding-session behavior and revocation triggers for token refresh.
3. Define API-first testing approach so response bodies never drift from OpenAPI.
4. Confirm persistence approach through Spring Data JPA only.
5. Define dependency review gate before introducing any library outside the Spring and frontend baseline.
6. Define local configuration strategy using `.env` plus `.env.example` without checking secrets into source control.

## Phase 1: Design Plan

1. Produce the domain data model for User, Customer, AccountReference, Session, PasswordResetToken, and ErrorResponse.
2. Define the OpenAPI contract for all auth and customer endpoints, including conflict and validation responses.
3. Produce a quickstart covering local frontend/backend startup, `.env`, test commands, and quality gates.
4. Re-check constitution compliance against the design outputs.

## Post-Design Constitution Check

- Static hosting fit: expected to remain compliant because the frontend contract consumes only external HTTP APIs.
- Accessibility and responsiveness: design includes explicit user-facing responsive and accessible auth and customer screens.
- Performance and asset impact: design keeps frontend and dependency surface constrained.
- Testing: design requires contract, negative, integration, and coverage verification.
- Client-side secret safety: design keeps all secrets and token-signing material server-side.

## Complexity Tracking

No constitution violations or justified exceptions are currently required.
```

Two caveats from the later analysis still apply:
- This plan exists only as drafted content unless you already pasted it into specs/001-bank-auth-customer/plan.md.
- The later spec/tasks analysis found drift around the role model, coverage threshold wording, and immutable customer fields, so if you use this plan, it should be reconciled with the current spec.md and tasks.md.