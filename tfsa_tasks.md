# Tasks: TFSA Account Creation (Backend & Frontend)

---

## TFSA Interest Rate Fetching Feature

- **Goal:** Fetch and display real-time interest rates for TFSA from a mock API

- **Approach:**
	- Mock API layer returns hardcoded TFSA rate (e.g. TFSA: 3.75%)
	- A service module handles the fetch and error handling
	- UI layer consumes the service and renders the TFSA rate

- **Dependencies:** Existing HTTP client or fetch utility in the project

- **Risks:** Mock data may not reflect real rate structures — flag for future real API swap

- **Milestone:** Feature complete when TFSA rate renders correctly with loading/error states
---

## Phase 3: GIC Investment Flows (TFSA)

### US2: Create GIC Investment

- [ ] T201 Implement endpoint to create GIC in TFSA (validate only one GIC per account)
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


---

## Phase 5: Interest Rate Feature (TFSA)

- [ ] T401 Create mock API endpoint returning TFSA interest rate
- [ ] T402 Create an interest rate service/fetch function
- [ ] T403 Add loading and error state handling
- [ ] FT401 Build UI component to display the TFSA rate
- [ ] FT402 Write unit tests for the fetch function and UI component


