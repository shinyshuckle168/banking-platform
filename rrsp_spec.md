## Feature Specification: RRSP Account with GIC Investment Support

- **Feature Branch:** [spec/rrsp-gic]  
- **Created:** April 29, 2026  
- **Status:** Draft  
- **Input:** "As a customer, I want to open an RRSP account and invest in GICs so that I can grow my retirement savings securely."

---

## 1.1 Feature Overview

This feature enables customers to open a Registered Retirement Savings Plan (RRSP) account and invest funds into Guaranteed Investment Certificates (GICs) within that account.

- **Who is this for?**  
  - Individual customers who want a secure and predictable way to save for retirement  

- **What outcome does it enable?**  
  - Customers can create an RRSP account, deposit funds, and allocate those funds into fixed-term GIC investments that generate guaranteed returns  

- **Why does the system need this behavior?**  
  - To support retirement savings products with low-risk investment options  
  - Improve customer retention  
  - Provide structured investment capabilities within the banking platform  

---

## 1.2 Scope and Non-Goals

### In Scope:
- Opening a new RRSP account for eligible customers  
- Maintaining RRSP account with available balance  
- Creating GIC investments within an RRSP account  
- Locking funds when invested into a GIC  
- Calculating and tracking GIC maturity details  
- Displaying RRSP account with associated GIC investments  

### Explicitly Excluded:
- Contribution limits and tax-related validations  
- Withdrawal flows and penalties  
- Tax slip generation  
- RRIF conversion  
- External investment products (stocks, mutual funds)  

---

## 1.3 Actors and Preconditions

### Actors:
- **CUSTOMER** — authenticated end-user (can operate only on owned accounts)  
- **ADMIN** — privileged operator with unrestricted access  

### Preconditions:
- Caller is authenticated (valid JWT required)  
- CUSTOMER must own the customerId  
- ADMIN can act on any customerId  
- Customer’s KYC status is verified (mocked: true/false)  
- Customer does not already have an active RRSP account  

---

## 1.4 Core Behavior and Flows

### Happy Path — Account Creation:
- Customer requests to open an RRSP account  
- System validates authentication and ownership  
- System verifies KYC status  
- System checks for existing RRSP account  
- System creates RRSP account with zero or initial balance  
- Account status is set to ACTIVE  
- Account appears in customer account list  

---

### Happy Path — GIC Investment:
- Customer selects an existing RRSP account  
- Customer chooses to create a GIC investment  
- Customer provides:
  - Investment amount (e.g., $10,000)  
  - Term (e.g., 3 years)  
  - Interest rate (e.g., 4%)  
- System validates sufficient available balance  
- System deducts amount from available balance  
- System creates GIC investment linked to RRSP account  
- System locks the invested funds for the term  
- System calculates maturity date and expected maturity amount  

---

### Maturity Flow:
- System identifies GICs reaching maturity  
- System updates GIC status from ACTIVE → MATURED  
- System credits principal + interest back to RRSP available balance  

---

### Decision Points:
- KYC verification must be true  
- Duplicate RRSP account check  
- Sufficient balance check for GIC creation  

---

### State Transitions:

- **RRSP Account:**  
  - Requested → ACTIVE  

- **GIC Investment:**  
  - ACTIVE → MATURED  

---

## 1.5 Business Rules

- An RRSP account must have:
  - Unique ID  
  - Available balance  
  - List of associated GIC investments  

- A customer can have only one active RRSP account  

- GIC investments:
  - Must be linked to a single RRSP account  
  - Only one GIC investment is allowed per RRSP account at any time  
  - Require sufficient available balance (validated before GIC creation; if insufficient, transaction fails)  
  - Lock funds for the selected term  
  - Are non-redeemable before maturity (default behavior)  
  - Must store principal, rate, term, and maturity details

- Interest:
  - Is fixed for the duration of the GIC  
  - The interest rate depends on the selected GIC term (e.g., longer terms may have higher rates)  
  - Is calculated at the time of creation and/or maturity

- On maturity:
  - Principal + interest must be credited back to RRSP account  

---

## 1.6 Data Model (Logical View)

### RRSP Account:
- Account ID  
- Customer ID  
- Available Balance  
- Status (ACTIVE)  
- List of GIC Investments  

### GIC Investment:
- GIC ID  
- RRSP Account ID  
- Principal Amount  
- Interest Rate  
- Term (in years)  
- Start Date  
- Maturity Date  
- Maturity Amount  
- Status (ACTIVE, MATURED)  
- Redeemable Flag (default: false)  

---

## 1.7 Account Type Extension

- Existing Account Types:  
  - CHECKING  
  - SAVINGS  

- New Account Type:  
  - RRSP  

- The system must recognize RRSP as a valid account type for creation and retrieval operations  

---

## 1.8 Error Conditions and Edge Cases

- **422** — business state → KYC not verified  
- **409** — RRSP_ALREADY_EXISTS → Active RRSP already exists  
- **400** — accountType → Unsupported account type  
- **404** — customerId → Customer not found  
- **401** — Ownership failure  
- **400** — insufficientFunds → Not enough balance for GIC  
- **400** — invalidTerm → Unsupported GIC term  
- **400** — Missing required GIC fields  
- **503** — System unavailable  

---

## 1.9 Non-Functional Requirements

- Account and GIC creation must complete within 2 seconds for 95% of requests  
- All operations must be authenticated and logged  
- Financial data must be handled using high-precision formats  
- All actions must be auditable (account creation, GIC creation, maturity events)  
- System availability must be at least 99.5%  
- Role-based access control (RBAC) must be enforced  

---

## 1.10 Acceptance Criteria

### Positive Scenario 1 — RRSP Account Creation
- **Given** an authenticated CUSTOMER or ADMIN with verified KYC and no existing RRSP  
- **When** a request to open an RRSP account is submitted  
- **Then** the system creates an ACTIVE RRSP account and displays it in the account list  

---

### Positive Scenario 2 — GIC Creation
- **Given** an ACTIVE RRSP account with sufficient available balance  
- **When** the customer creates a GIC with valid amount, term, and rate  
- **Then** the system:
  - Deducts the amount from available balance  
  - Creates a GIC with ACTIVE status  
  - Locks funds for the specified term  

---

### Positive Scenario 3 — GIC Maturity
- **Given** a GIC that has reached its maturity date  
- **When** the system processes maturity  
- **Then** the system:
  - Updates status to MATURED  
  - Credits principal + interest to RRSP available balance  

---

### Negative Scenario 1 — KYC Not Verified
- **Given** a caller with unverified KYC  
- **When** attempting to create an RRSP account  
- **Then** the system returns HTTP 422  

---

### Negative Scenario 2 — RRSP Already Exists
- **Given** a customer with an active RRSP account  
- **When** attempting to create another  
- **Then** the system returns HTTP 409 with RRSP_ALREADY_EXISTS  

---

### Negative Scenario 3 — Insufficient Balance for GIC
- **Given** an RRSP account with insufficient balance  
- **When** attempting to create a GIC  
- **Then** the system rejects the request with HTTP 400 (insufficientFunds)  
