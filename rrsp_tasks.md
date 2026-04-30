
# Tasks: RRSP Account with GIC Investment Support

**Input**: rrsp_spec.md, rrsp_plan.md

---

## Phase 1: Setup

- [ ] T001 Verify backend project structure exists in backend/src/main/java/com/group1/banking/
- [ ] T002 Verify frontend project structure exists in frontend/
- [ ] T003 Add/verify Maven, Spring Boot, and dependencies in backend/pom.xml
- [ ] T004 Add/verify React, dependencies, and scripts in frontend/package.json

---

## Phase 2: Foundational

- [ ] T005 Implement base DB schema for RRSP and GIC entities in backend/src/main/resources/
- [ ] T006 [P] Implement enums RRSPStatus and GICStatus in backend/src/main/java/com/group1/banking/model/
- [ ] T007 [P] Implement base entity classes for RRSPAccount and GIC in backend/src/main/java/com/group1/banking/model/
- [ ] T008 [P] Implement JWT authentication and RBAC in backend/src/main/java/com/group1/banking/security/
- [ ] T009 [P] Set up global exception handler in backend/src/main/java/com/group1/banking/exception/
- [ ] T010 [P] Set up logging and audit infrastructure in backend/src/main/java/com/group1/banking/logging/

---

## Phase 3: User Stories

### [US1] Create RRSP Account (US-201)

- [ ] T101 [US1] Implement POST /api/v1/rrsp endpoint in backend/src/main/java/com/group1/banking/controller/RRSPController.java
- [ ] T102 [P] [US1] Implement RRSP account creation logic and validation in backend/src/main/java/com/group1/banking/service/RRSPService.java
- [ ] T103 [P] [US1] Implement RRSPAccount JPA repository in backend/src/main/java/com/group1/banking/repository/RRSPAccountRepository.java
- [ ] T104 [P] [US1] Add tests for RRSP account creation in backend/src/test/java/com/group1/banking/service/RRSPServiceTest.java
- [ ] T105 [P] [US1] Implement error handling for RRSP creation in backend/src/main/java/com/group1/banking/exception/
- [ ] T106 [P] [US1] Create RRSP account opening page in frontend/src/pages/RRSPCreatePage.js
- [ ] T107 [P] [US1] Integrate frontend with backend RRSP creation API in frontend/src/api/rrsp.js
- [ ] T108 [P] [US1] Display validation errors in frontend/src/components/ErrorDisplay.js

### [US2] Get RRSP Details (US-202)

- [ ] T201 [US2] Implement GET /api/v1/rrsp/{customerId} endpoint in backend/src/main/java/com/group1/banking/controller/RRSPController.java
- [ ] T202 [P] [US2] Implement RRSP details retrieval logic in backend/src/main/java/com/group1/banking/service/RRSPService.java
- [ ] T203 [P] [US2] Add tests for RRSP details retrieval in backend/src/test/java/com/group1/banking/service/RRSPServiceTest.java
- [ ] T204 [P] [US2] Create RRSP details page in frontend/src/pages/RRSPDetailsPage.js
- [ ] T205 [P] [US2] Integrate frontend with backend RRSP details API in frontend/src/api/rrsp.js

### [US3] Create GIC (US-203)

- [ ] T301 [US3] Implement POST /api/v1/rrsp/{rrspId}/gic endpoint in backend/src/main/java/com/group1/banking/controller/GICController.java
- [ ] T302 [P] [US3] Implement GIC creation logic and validation in backend/src/main/java/com/group1/banking/service/GICService.java
- [ ] T303 [P] [US3] Implement GIC JPA repository in backend/src/main/java/com/group1/banking/repository/GICRepository.java
- [ ] T304 [P] [US3] Add tests for GIC creation (one per account, insufficient funds, etc.) in backend/src/test/java/com/group1/banking/service/GICServiceTest.java
- [ ] T305 [P] [US3] Implement error handling for GIC creation in backend/src/main/java/com/group1/banking/exception/
- [ ] T306 [P] [US3] Create GIC investment page in frontend/src/pages/GICCreatePage.js
- [ ] T307 [P] [US3] Integrate frontend with backend GIC creation API in frontend/src/api/gic.js
- [ ] T308 [P] [US3] Display GIC creation errors in frontend/src/components/ErrorDisplay.js

### [US4] Get GIC Details (US-204)

- [ ] T401 [US4] Implement GET /api/v1/rrsp/{rrspId}/gic endpoint in backend/src/main/java/com/group1/banking/controller/GICController.java
- [ ] T402 [P] [US4] Implement GIC details retrieval logic in backend/src/main/java/com/group1/banking/service/GICService.java
- [ ] T403 [P] [US4] Add tests for GIC details retrieval in backend/src/test/java/com/group1/banking/service/GICServiceTest.java
- [ ] T404 [P] [US4] Create GIC details page in frontend/src/pages/GICDetailsPage.js
- [ ] T405 [P] [US4] Integrate frontend with backend GIC details API in frontend/src/api/gic.js

### [US5] Redeem GIC (US-205)

- [ ] T501 [US5] Implement POST /api/v1/rrsp/{rrspId}/gic/redeem endpoint in backend/src/main/java/com/group1/banking/controller/GICController.java
- [ ] T502 [P] [US5] Implement GIC redemption logic and payout calculation in backend/src/main/java/com/group1/banking/service/GICService.java
- [ ] T503 [P] [US5] Add tests for GIC redemption in backend/src/test/java/com/group1/banking/service/GICServiceTest.java
- [ ] T504 [P] [US5] Implement error handling for GIC redemption in backend/src/main/java/com/group1/banking/exception/
- [ ] T505 [P] [US5] Create GIC redemption page in frontend/src/pages/GICRedeemPage.js
- [ ] T506 [P] [US5] Integrate frontend with backend GIC redemption API in frontend/src/api/gic.js
- [ ] T507 [P] [US5] Display GIC redemption errors in frontend/src/components/ErrorDisplay.js

### [US6] Close RRSP Account (US-206)

- [ ] T601 [US6] Implement POST /api/v1/rrsp/{rrspId}/close endpoint in backend/src/main/java/com/group1/banking/controller/RRSPController.java
- [ ] T602 [P] [US6] Implement RRSP account closure logic and validation in backend/src/main/java/com/group1/banking/service/RRSPService.java
- [ ] T603 [P] [US6] Add tests for RRSP account closure in backend/src/test/java/com/group1/banking/service/RRSPServiceTest.java
- [ ] T604 [P] [US6] Implement error handling for RRSP closure in backend/src/main/java/com/group1/banking/exception/
- [ ] T605 [P] [US6] Create RRSP account closure page in frontend/src/pages/RRSPClosePage.js
- [ ] T606 [P] [US6] Integrate frontend with backend RRSP closure API in frontend/src/api/rrsp.js
- [ ] T607 [P] [US6] Display RRSP closure errors in frontend/src/components/ErrorDisplay.js

---

## Phase 4: Polish & Cross-Cutting

- [ ] T701 [P] Add global error response structure in backend/src/main/java/com/group1/banking/exception/GlobalErrorHandler.java
- [ ] T702 [P] Add API documentation and quickstart in backend/README.md and frontend/README.md
- [ ] T703 [P] Ensure all actions are logged and auditable in backend/src/main/java/com/group1/banking/logging/
- [ ] T704 [P] Write end-to-end tests for all user flows in backend/src/test/java/com/group1/banking/ and frontend/src/__tests__/
- [ ] T705 [P] Document frontend usage and provide a quickstart in frontend/README.md

---

## Dependencies

- [US1] → [US2], [US3], [US6] (RRSP must exist before details, GIC, or closure)
- [US3] → [US4], [US5] (GIC must exist before details or redemption)
- [US5] → [US6] (GIC must be redeemed before RRSP closure)

## Parallel Execution Examples

- T006, T007, T008, T009, T010 can be done in parallel after T005
- All [P] tasks within a user story phase can be executed in parallel after the main endpoint/controller task

## Implementation Strategy

- MVP: Complete [US1] (RRSP account creation and validation, backend and frontend)
- Incremental: Deliver each user story phase independently, with full test and UI coverage

