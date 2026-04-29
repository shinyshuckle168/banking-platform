# Feature Specification: Create RRSP Account

**Feature Branch**: `[spec/rrsp]`
**Created**: April 28, 2026  
**Status**: Draft  
**Input**: User description: "As a customer, I want to open an RRSP account so that I can contribute to my retirement savings."

## Feature Overview

This feature enables customers to open a Registered Retirement Savings Plan (RRSP) account within the banking platform. It is designed for individual customers who wish to save for retirement in a tax-advantaged manner. The outcome is that eligible customers can initiate and complete the opening of an RRSP account, ensuring compliance with regulatory and business rules. The system needs this behavior to support retirement planning products, regulatory compliance, and customer retention.

## Scope and Non-Goals

**In Scope:**
- Opening a new RRSP account for eligible customers
- Enforcing KYC verification before account creation
- Displaying RRSP-specific fields during account opening
- Ensuring the new RRSP account appears in the customer’s account list
- Applying business rules for RRSP creation (contribution limits, over-contribution buffer, etc.)

**Explicitly Excluded:**
- Contribution processing (adding funds to RRSP)
- Withdrawal flow (handling withdrawals and related tax slips)
- Tax slip generation
- Retirement conversion to RRIF (Registered Retirement Income Fund)
- Interest calculation and posting

## Actors and Preconditions

**Actors:**
- CUSTOMER — authenticated end-user (may open RRSP for self only; must own the customerId)
- ADMIN — privileged operator with unrestricted access (bypasses ownership checks)

**Preconditions:**
- Caller is authenticated (valid JWT required)
- CUSTOMER caller: must own the customerId
- ADMIN caller: may open RRSP for any customer
- Customer’s KYC (Know Your Customer) status is verified (mocked: true/false)
- Customer does not already have an active RRSP account
- Requested account type is RRSP

## Core Behavior and Flows

**Happy Path:**
1. Customer requests to open an RRSP account.
2. System checks if the customer is authenticated.
3. System verifies KYC status (mocked: returns true or false).
4. System checks if the customer already has an active RRSP account.
5. System validates that the requested account type is RRSP and supported.
6. System presents RRSP-specific fields for completion.
7. Upon successful validation, the system creates the RRSP account.
8. The new RRSP account appears in the customer’s account list.

**Decision Points:**
- KYC verification (must be true)
- Duplicate RRSP account check
- Account type validation

**State Transitions:**
- Requested → Active (upon successful creation)

## Business Rules

 - The system MUST track annual RRSP contribution limits for each customer.
- Over-contribution is NOT ALLOWED beyond a $2,000 buffer.
- RRSP balance CANNOT be overdrawn.
- The interest rate for RRSP accounts is variable and may change each year.
- Customer may open an RRSP ONLY if KYC is verified.
- The new RRSP account MUST appear in the customer’s account list after creation.

### Constants
- ANNUAL_CONTRIBUTION_LIMIT — current year's CRA-published RRSP limit, loaded from configuration
- OVER_CONTRIBUTION_BUFFER — 2,000.00, CRA-defined lifetime buffer

Both values MUST be configurable and MUST NOT be hardcoded.

### Account Type Extension
- The platform's standard AccountType enum is: CHECKING | SAVINGS.
- RRSP is introduced as a third supported account type for this feature. All validation and API contracts must recognize RRSP as a valid account type for account creation and retrieval.

## Error Conditions and Edge Cases

| HTTP Status | Code / Field         | Condition                                                      |
|-------------|----------------------|----------------------------------------------------------------|
| 422         | business state       | Customer’s KYC is not verified (mocked false)                  |
| 409         | RRSP_ALREADY_EXISTS  | Customer already has an active RRSP account                    |
| 400         | accountType          | Requested account type is not supported                        |
| 404         | customerId           | Customer not found                                             |
| 401         | —                    | Ownership failure (CUSTOMER caller does not own customerId)    |
| 400         | —                    | Customer under eligible age for RRSP (if applicable)           |
| 400         | —                    | Missing mandatory RRSP fields during account opening           |
| 503         | —                    | System unavailable or unable to process the request            |

## Non-Functional Requirements

- Account creation response time should be under 2 seconds for 95% of requests.
- Only authenticated users may open RRSP accounts; all actions must be logged.
- Personally Identifiable Information (PII) must be handled securely and not exposed in logs.
- All account opening actions must be auditable, recording who opened the account, when, and the outcome.
- The system must be available 99.5% of the time (excluding planned maintenance).
- Role-based access control (RBAC) must be enforced, and privileges must not be hardcoded; roles can be changed without code changes.

## Acceptance Criteria

### Positive Scenario 1 — Successful RRSP Account Creation
**Given** an authenticated CUSTOMER or ADMIN caller, KYC is verified, no active RRSP exists for the customer, and account type RRSP is supported
**When** a request to open an RRSP account is submitted with all required fields
**Then** the system creates the RRSP account with status ACTIVE and the new account appears in the customer’s account list

### Negative Scenario 1 — KYC Not Verified
**Given** an authenticated caller whose KYC is not verified (mocked false)
**When** a request to open an RRSP account is submitted
**Then** the system rejects the request with HTTP 422 and a business state error

### Negative Scenario 2 — RRSP Already Exists
**Given** an authenticated caller for a customer who already has an active RRSP account
**When** a request to open another RRSP account is submitted
**Then** the system rejects the request with HTTP 409 and code RRSP_ALREADY_EXISTS