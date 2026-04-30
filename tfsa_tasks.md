# Tasks: TFSA Account Creation (Backend & Frontend)

**Input**: tfsa_spec.md, tfsa_plan.md  
**Prerequisites**: Plan and spec files

---

## Phase 1: Setup & Foundation

- [ ] T001 Create/verify backend project structure in `backend/src/main/java/com/group1/banking/`
- [ ] T002 Ensure DB schema supports TFSA entities
- [ ] FT001 Scaffold frontend project (e.g., create-react-app, Vite, or preferred tool)
- [ ] FT002 Set up routing, state management, and API integration boilerplate

---

## Phase 2: TFSA Account Flows

### US1: Open TFSA Account

- [ ] T101 Implement endpoint to open TFSA account (validate KYC, age, one TFSA per customer)
- [ ] T102 Add logic to check for existing TFSA account before creation
- [ ] T103 Add logic to validate age (18+) and KYC
- [ ] T104 Add logic to track and validate contribution room
- [ ] T105 Add tests for TFSA account creation (positive/negative scenarios)
- [ ] FT101 Create TFSA account opening page/form
- [ ] FT102 Integrate with backend endpoint for account creation
- [ ] FT103 Display validation errors (e.g., KYC not verified, TFSA already exists, underage, over contribution room)
- [ ] FT104 Display TFSA account details and contribution room

---

## Phase 3: Error Handling & Validation

- [ ] T201 Implement error responses for all business rule violations (e.g., TFSA_ALREADY_EXISTS, underage, KYC not verified, over contribution room)
- [ ] T202 Add tests for all error conditions
- [ ] FT201 Show all backend error messages to the user
- [ ] FT202 Add client-side validation for forms

---

## Phase 4: Testing, Documentation & Audit

- [ ] T301 Update API docs and quickstart for TFSA flows
- [ ] T302 Ensure all actions are logged and auditable
- [ ] FT301 Write tests for all user flows and edge cases
- [ ] FT302 Document frontend usage and provide a quickstart
