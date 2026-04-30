# Tasks: TFSA Account Creation (Backend & Frontend)


## TFSA Interest Rate Fetching Feature


	- Mock API layer returns hardcoded TFSA rate (e.g. TFSA: 3.75%)
	- A service module handles the fetch and error handling
	- UI layer consumes the service and renders the TFSA rate




## Phase 3: GIC Investment Flows (TFSA)

### US2: Create GIC Investment



### US3: GIC Maturity & History


**Input**: tfsa_spec.md, tfsa_plan.md  
**Prerequisites**: Plan and spec files


## Phase 1: Setup & Foundation



## Phase 2: TFSA Account Flows

### US1: Open TFSA Account



## Phase 3: Error Handling & Validation



## Phase 4: Testing, Documentation & Audit



## Phase 5: Interest Rate Feature (TFSA)


# Tasks: TFSA Account Creation & Interest Rate Feature

**Input**: tfsa_spec.md, tfsa_plan.md

---

## Phase 1: Setup

- [ ] T001 Verify backend project structure exists in backend/src/main/java/com/group1/banking/
- [ ] T002 Verify frontend project structure exists in frontend/
- [ ] T003 Add/verify Maven, Spring Boot, and dependencies in backend/pom.xml
- [ ] T004 Add/verify React, dependencies, and scripts in frontend/package.json

---

## Phase 2: Foundational

- [ ] T005 Implement base DB schema for TFSA entities in backend/src/main/resources/
- [ ] T006 [P] Implement enums TFSAStatus and TFSAOperationType in backend/src/main/java/com/group1/banking/model/
- [ ] T007 [P] Implement base entity classes for TFSAAccount, TFSAContribution in backend/src/main/java/com/group1/banking/model/
- [ ] T008 [P] Implement JWT authentication and RBAC in backend/src/main/java/com/group1/banking/security/
- [ ] T009 [P] Set up global exception handler in backend/src/main/java/com/group1/banking/exception/
- [ ] T010 [P] Set up logging and audit infrastructure in backend/src/main/java/com/group1/banking/logging/

---

## Phase 3: User Stories

### [US1] Create TFSA Account

- [ ] T101 [US1] Implement POST /api/v1/customers/{customerId}/accounts endpoint in backend/src/main/java/com/group1/banking/controller/AccountController.java
- [ ] T102 [P] [US1] Implement TFSA account creation logic and validation (KYC, age, one per customer) in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T103 [P] [US1] Implement TFSAAccount JPA repository in backend/src/main/java/com/group1/banking/repository/TFSAAccountRepository.java
- [ ] T104 [P] [US1] Add tests for TFSA account creation in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T105 [P] [US1] Implement error handling for TFSA creation in backend/src/main/java/com/group1/banking/exception/
- [ ] T106 [P] [US1] Create TFSA account opening page in frontend/src/pages/TFSAAccountCreatePage.js
- [ ] T107 [P] [US1] Integrate frontend with backend TFSA creation API in frontend/src/api/tfsa.js
- [ ] T108 [P] [US1] Display validation errors in frontend/src/components/ErrorDisplay.js

### [US2] Get TFSA Account Details

- [ ] T201 [US2] Implement GET /api/v1/accounts/{tfsaId} endpoint in backend/src/main/java/com/group1/banking/controller/AccountController.java
- [ ] T202 [P] [US2] Implement TFSA details retrieval logic in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T203 [P] [US2] Add tests for TFSA details retrieval in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T204 [P] [US2] Create TFSA details page in frontend/src/pages/TFSAAccountDetailsPage.js
- [ ] T205 [P] [US2] Integrate frontend with backend TFSA details API in frontend/src/api/tfsa.js

### [US3] TFSA Contribution (Deposit)

- [ ] T301 [US3] Implement POST /accounts/transfer endpoint for TFSA deposit in backend/src/main/java/com/group1/banking/controller/TransferController.java
- [ ] T302 [P] [US3] Implement TFSA deposit logic and contribution room validation in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T303 [P] [US3] Add tests for TFSA deposit and contribution room in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T304 [P] [US3] Create TFSA deposit page in frontend/src/pages/TFSAContributionPage.js
- [ ] T305 [P] [US3] Integrate frontend with backend TFSA deposit API in frontend/src/api/tfsa.js
- [ ] T306 [P] [US3] Display deposit errors in frontend/src/components/ErrorDisplay.js

### [US4] TFSA Withdrawal

- [ ] T401 [US4] Implement POST /accounts/transfer endpoint for TFSA withdrawal in backend/src/main/java/com/group1/banking/controller/TransferController.java
- [ ] T402 [P] [US4] Implement TFSA withdrawal logic and balance validation in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T403 [P] [US4] Add tests for TFSA withdrawal and balance in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T404 [P] [US4] Create TFSA withdrawal page in frontend/src/pages/TFSAWithdrawalPage.js
- [ ] T405 [P] [US4] Integrate frontend with backend TFSA withdrawal API in frontend/src/api/tfsa.js
- [ ] T406 [P] [US4] Display withdrawal errors in frontend/src/components/ErrorDisplay.js

### [US5] Get Contribution Room

- [ ] T501 [US5] Implement GET /api/v1/accounts/{tfsaId}/contribution-room endpoint in backend/src/main/java/com/group1/banking/controller/AccountController.java
- [ ] T502 [P] [US5] Implement contribution room logic in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T503 [P] [US5] Add tests for contribution room in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T504 [P] [US5] Create contribution room display in frontend/src/pages/TFSAContributionRoomPage.js
- [ ] T505 [P] [US5] Integrate frontend with backend contribution room API in frontend/src/api/tfsa.js

### [US6] Get Transaction History

- [ ] T601 [US6] Implement GET /api/v1/accounts/{tfsaId}/transactions endpoint in backend/src/main/java/com/group1/banking/controller/AccountController.java
- [ ] T602 [P] [US6] Implement transaction history logic in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T603 [P] [US6] Add tests for transaction history in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T604 [P] [US6] Create transaction history page in frontend/src/pages/TFSAAccountTransactionsPage.js
- [ ] T605 [P] [US6] Integrate frontend with backend transaction history API in frontend/src/api/tfsa.js

### [US7] Close TFSA Account

- [ ] T701 [US7] Implement DELETE /api/v1/accounts/{tfsaId} endpoint in backend/src/main/java/com/group1/banking/controller/AccountController.java
- [ ] T702 [P] [US7] Implement TFSA account closure logic and validation in backend/src/main/java/com/group1/banking/service/TFSAService.java
- [ ] T703 [P] [US7] Add tests for TFSA account closure in backend/src/test/java/com/group1/banking/service/TFSAServiceTest.java
- [ ] T704 [P] [US7] Implement error handling for TFSA closure in backend/src/main/java/com/group1/banking/exception/
- [ ] T705 [P] [US7] Create TFSA account closure page in frontend/src/pages/TFSAAccountClosePage.js
- [ ] T706 [P] [US7] Integrate frontend with backend TFSA closure API in frontend/src/api/tfsa.js
- [ ] T707 [P] [US7] Display closure errors in frontend/src/components/ErrorDisplay.js

### [US8] TFSA Interest Rate Fetching Feature

- [ ] T801 [US8] Create mock API endpoint returning TFSA interest rate in backend/src/main/java/com/group1/banking/controller/InterestRateController.java
- [ ] T802 [P] [US8] Create interest rate service/fetch function in frontend/src/api/interestRate.js
- [ ] T803 [P] [US8] Add loading and error state handling in frontend/src/components/InterestRateDisplay.js
- [ ] T804 [P] [US8] Build UI component to display the TFSA rate in frontend/src/components/InterestRateDisplay.js
- [ ] T805 [P] [US8] Write unit tests for the fetch function and UI component in frontend/src/__tests__/

---

## Phase 4: Polish & Cross-Cutting

- [ ] T901 [P] Add global error response structure in backend/src/main/java/com/group1/banking/exception/GlobalErrorHandler.java
- [ ] T902 [P] Add API documentation and quickstart in backend/README.md and frontend/README.md
- [ ] T903 [P] Ensure all actions are logged and auditable in backend/src/main/java/com/group1/banking/logging/
- [ ] T904 [P] Write end-to-end tests for all user flows in backend/src/test/java/com/group1/banking/ and frontend/src/__tests__/
- [ ] T905 [P] Document frontend usage and provide a quickstart in frontend/README.md

---

## Dependencies

- [US1] → [US2], [US3], [US4], [US5], [US6], [US7] (TFSA must exist before details, deposit, withdrawal, contribution room, transactions, or closure)
- [US3] → [US5] (Deposit must update contribution room)
- [US4] → [US5] (Withdrawal must update contribution room)
- [US7] → [US1] (Closure disables account, no further actions allowed)

## Parallel Execution Examples

- T006, T007, T008, T009, T010 can be done in parallel after T005
- All [P] tasks within a user story phase can be executed in parallel after the main endpoint/controller task

## Implementation Strategy

- MVP: Complete [US1] (TFSA account creation and validation, backend and frontend)
- Incremental: Deliver each user story phase independently, with full test and UI coverage


