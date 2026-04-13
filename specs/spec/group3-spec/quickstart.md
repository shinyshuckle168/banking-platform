# Quickstart: Group 3 Implementation

**Feature**: Group 3 Stories (US-01 through US-05)  
**Stack**: Java 21 + Spring Boot 3.x | React 18 + Vite  
**Date**: 2026-04-08

---

## Prerequisites

- Java 21 (JDK)
- Node.js 20+ and npm
- MySQL 8 (production / shared dev)
- Git

---

## Backend Local Setup

```bash
# 1. Clone and switch to branch
git checkout spec/group3-spec

# 2. Navigate to backend
cd backend

# 3. Start with H2 (local development profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# H2 console available at: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:bankingdb
# Username: sa  Password: (empty)
```

**Active profiles**:
- `local` — H2 in-memory, debug logging, no mTLS required for `/notifications/evaluate`
- `test` — H2 in-memory, used by JUnit/integration tests
- `prod` — MySQL, full security enforcement

---

## Backend Configuration (application-local.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bankingdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

banking:
  categories:
    Housing: ["rent", "mortgage", "letting"]
    Transport: ["fuel", "bus", "train", "uber", "taxi"]
    Food & Drink: ["restaurant", "cafe", "grocery", "supermarket"]
    Entertainment: ["cinema", "netflix", "spotify", "gaming"]
    Shopping: ["amazon", "ebay", "retail"]
    Utilities: ["electricity", "gas", "water", "internet"]
    Health: ["pharmacy", "dentist", "gym", "doctor"]
    Income: ["salary", "payroll", "dividend", "interest"]

  notifications:
    allowed-service-ids:
      payments-service: "hashed-api-key-here"
      fraud-service:    "hashed-api-key-here"

  idempotency:
    purge-after-hours: 72
```

---

## Backend: Running Tests

```bash
# All tests
./mvnw test

# Single story
./mvnw test -Dtest=TransactionHistoryServiceTest
./mvnw test -Dtest=StandingOrderServiceTest
./mvnw test -Dtest=NotificationEvaluationServiceTest
./mvnw test -Dtest=MonthlyStatementServiceTest
./mvnw test -Dtest=SpendingInsightServiceTest

# Integration tests only
./mvnw test -Dgroups=Integration
```

---

## Frontend Local Setup

```bash
cd frontend
npm install
npm run dev
# App available at: http://localhost:5173
```

**Environment file** (`frontend/.env.local`):

```
VITE_API_BASE_URL=http://localhost:8080
```

---

## Frontend: Running Tests

```bash
cd frontend
npm test              # Jest + React Testing Library (watch mode)
npm test -- --run     # Single pass
npm run coverage      # Coverage report
```

---

## Key Endpoints — Quick Reference

| Story | Method | Path |
|---|---|---|
| US-01 | GET | `/accounts/{accountId}/transactions` |
| US-01 | GET | `/accounts/{accountId}/transactions/export` |
| US-02 | POST | `/accounts/{accountId}/standing-orders` |
| US-02 | GET | `/accounts/{accountId}/standing-orders` |
| US-02 | DELETE | `/standing-orders/{standingOrderId}` |
| US-03 | POST | `/notifications/evaluate` |
| US-04 | GET | `/accounts/{accountId}/statements/{period}` |
| US-05 | GET | `/accounts/{accountId}/insights` |
| US-05 | PUT | `/accounts/{accountId}/transactions/{transactionId}/category` |

---

## Group 2 Dependency Note

Group 3 reads, but does not write, the following Group 2 tables:
- `users` — for JWT subject resolution and permission lookup
- `roles` / `role_permissions` — for RBAC enforcement
- `customers` — for ownership resolution
- `accounts` — for account status and balance checks
- `transactions` — for history, statements, and insights (with one additive column: `category`)

Do **not** redefine any Group 2 entity or repository class. Use Group 2's published service interfaces or repository beans directly.
