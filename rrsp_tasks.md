
# Tasks: RRSP Account with GIC Investment Support (Backend & Frontend)

**Input**: rrsp_spec.md, rrsp_plan.md  
**Prerequisites**: Plan and spec files

---


## Phase 1: Setup & Foundation

- [ ] T001 Create/verify backend project structure in `backend/src/main/java/com/group1/banking/`
- [ ] T002 Ensure DB schema supports RRSP and GIC entities
- [ ] FT001 Scaffold frontend project (e.g., create-react-app, Vite, or preferred tool)
- [ ] FT002 Set up routing, state management, and API integration boilerplate

---


## Phase 2: RRSP Account Flows

### US1: Open RRSP Account

- [ ] T101 Implement endpoint to open RRSP account (validate KYC, one RRSP per customer)
- [ ] T102 Add logic to check for existing RRSP account before creation
- [ ] T103 Add tests for RRSP account creation (positive/negative scenarios)
- [ ] FT101 Create RRSP account opening page/form
- [ ] FT102 Integrate with backend endpoint for account creation
- [ ] FT103 Display validation errors (e.g., KYC not verified, RRSP already exists)

---


## Phase 3: GIC Investment Flows

### US2: Create GIC Investment

- [ ] T201 Implement endpoint to create GIC in RRSP (validate only one GIC per account)
- [ ] T202 Validate sufficient funds before GIC creation
- [ ] T203 Lock funds for GIC term and calculate maturity
- [ ] T204 Add tests for GIC creation (one per account, insufficient funds, etc.)
- [ ] FT201 Create GIC investment page/form (amount, term, etc.)
- [ ] FT202 Integrate with backend endpoint for GIC creation
- [ ] FT203 Validate sufficient funds and one GIC per account (show errors)
- [ ] FT204 Display GIC details and status

---


### US3: GIC Maturity & History

- [ ] T301 Implement GIC maturity processing (update status, credit principal + interest)
- [ ] T302 Add tests for GIC maturity flow
- [ ] FT301 Display GIC maturity status and transaction history

---


## Phase 4: Error Handling & Validation

- [ ] T401 Implement error responses for all business rule violations (e.g., RRSP_ALREADY_EXISTS, insufficientFunds)
- [ ] T402 Add tests for all error conditions
- [ ] FT401 Show all backend error messages to the user (e.g., insufficient funds, duplicate GIC)
- [ ] FT402 Add client-side validation for forms

---


## Phase 5: Testing, Documentation & Audit

- [ ] T501 Update API docs and quickstart for RRSP/GIC flows
- [ ] T502 Ensure all actions are logged and auditable
- [ ] FT501 Write tests for all user flows and edge cases
- [ ] FT502 Document frontend usage and provide a quickstart
