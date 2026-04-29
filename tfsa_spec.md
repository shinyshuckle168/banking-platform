# Feature Specification: Create TFSA Account

**Feature Branch**: `[spec/tfsa]`
**Created**: April 28, 2026
**Status**: Draft
**Input**: User description: "As a customer, I want to open a TFSA account so that I can grow savings tax-free."

## Feature Overview

This feature enables eligible customers to open a Tax-Free Savings Account (TFSA) through the Digital Banking Platform. It is designed for individuals who want to grow their savings tax-free, subject to annual contribution limits and regulatory requirements. The outcome is that customers can initiate and complete the opening of a TFSA account, supporting financial planning and compliance.

## Scope and Non-Goals

**In Scope:**
- Opening a new TFSA account for eligible customers
- Enforcing KYC and age verification before account creation
- Displaying and validating TFSA-specific fields during account opening
- Ensuring the new TFSA account appears in the customer’s account list
- Enforcing all business rules for TFSA creation (contribution limits, age, etc.)


## Actors and Preconditions

**Actors:**
- CUSTOMER — authenticated end-user (may open TFSA for self only; must own the customerId)
- ADMIN — privileged operator with unrestricted access (bypasses ownership checks)

**Preconditions:**
- Caller is authenticated (valid JWT required)
- CUSTOMER caller: must own the customerId
- ADMIN caller: may open TFSA for any customer
- Customer’s KYC (Know Your Customer) status is verified (mocked: true/false)
- Applicant is 18 years of age or older (date of birth or age confirmation required)
- Customer does not already have an active TFSA account
- Requested account type is TFSA

## Core Behavior and Flows

**Happy Path:**
1. Customer submits a request to open a TFSA account.
2. System verifies authentication and KYC status.
3. System checks applicant’s age (must be 18+).
4. System checks for an existing active TFSA account for the customer.
5. System validates that the requested account type is TFSA.
6. System presents and validates TFSA-specific fields (e.g., date of birth, contribution room).
7. Upon successful validation, the system creates the TFSA account with status ACTIVE.
8. The new TFSA account appears in the customer’s account list.

**Decision Points:**
- KYC verification (must be true)
- Age check (must be 18+)
- Duplicate TFSA account check
- Account type validation

**State Transitions:**
- Requested → ACTIVE (upon successful creation)

## Business Rules

- The system MUST track annual TFSA contribution limits for each customer (TFSA_ANNUAL_CONTRIBUTION_LIMIT, configurable per year).
- Withdrawals from a TFSA restore contribution room in the next calendar year.
- TFSA balance CANNOT be overdrawn.
- Customer may open a TFSA ONLY if age is 18 or older at the time of application.
- The new TFSA account MUST appear in the customer’s account list after creation.
- System MUST record and verify the applicant’s date of birth or age confirmation.

## Error Conditions and Edge Cases

| HTTP Status | Code / Field             | Condition                                                      |
|-------------|--------------------------|----------------------------------------------------------------|
| 422         | business state           | Customer’s KYC is not verified                                 |
| 409         | TFSA_ALREADY_EXISTS      | Customer already has an active TFSA account                    |
| 400         | accountType              | Requested account type is not supported                        |
| 404         | customerId               | Customer not found                                             |
| 401         | —                        | Ownership failure (CUSTOMER caller does not own customerId)    |
| 400         | —                        | Applicant is under 18 years of age                             |
| 400         | —                        | Missing required TFSA-specific fields during account opening   |
| 400         | —                        | Contribution would exceed available TFSA room                  |
| 503         | —                        | System unavailable or unable to process the request            |

## Non-Functional Requirements

- Account creation response time should be under 2 seconds for 95% of requests.
- Only authenticated users may open TFSA accounts; all actions must be logged.
- Personally Identifiable Information (PII) must be handled securely and not exposed in logs.
- All account opening actions must be auditable, recording who opened the account, when, the outcome, and age verification result.
- The system must be available 99.5% of the time (excluding planned maintenance).
- Role-based access control (RBAC) must be enforced, and privileges must not be hardcoded; roles can be changed without code changes.

## API Expectations (High-Level)
- The system will expose a create-account operation under the accounts resource (e.g., `POST /customers/{customerId}/accounts`).
- Expected inputs: customer ID, account type (MUST be TFSA), TFSA-specific fields (including date of birth or age confirmation).
- Expected outputs: account ID, status, TFSA-specific fields, and current contribution room.
- Only documented fields are accepted; undocumented fields are explicitly forbidden.
- No OpenAPI YAML or implementation details are included in this specification.
