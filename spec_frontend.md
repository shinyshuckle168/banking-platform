# Frontend Specification

**Project:** Digital Banking Platform  
**Frontend Stack:** React 18 · JavaScript · React Router · Axios · React Query  
**Backend Dependencies:** Unified Banking Backend  
**Out of Scope for This Spec:** A later standalone transactions microservice, native mobile clients, and non-essential back-office dashboards

---

## Table of Contents

1. [Purpose](#1-purpose)
2. [Frontend Scope](#2-frontend-scope)
3. [Supported Backend Capabilities](#3-supported-backend-capabilities)
4. [User Flows](#4-user-flows)
5. [Application Routes](#5-application-routes)
6. [Page Specifications](#6-page-specifications)
7. [API Integration Contract](#7-api-integration-contract)
8. [State Management](#8-state-management)
9. [Validation and Error Handling](#9-validation-and-error-handling)
10. [Authentication and Authorization Behaviour](#10-authentication-and-authorization-behaviour)
11. [UI and UX Requirements](#11-ui-and-ux-requirements)
12. [Testing Expectations](#12-testing-expectations)
13. [Definition of Done](#13-definition-of-done)

---

## 1. Purpose

This document defines the frontend requirements for the React application. It is separate from the backend developer specifications and describes how the UI must expose and coordinate:

- authentication and customer profile endpoints
- account, money-movement, transaction history, standing order, statement, and insights endpoints

This frontend must provide a coherent authenticated banking experience now, while still leaving room for a future standalone transactions microservice later.

---

## 2. Frontend Scope

### In Scope

- Public authentication screens for registration and login
- Placeholder-capable screens for password reset request and token refresh support
- Authenticated customer profile screens for create, read, and update
- Account screens for create, read, list, update, and delete flows
- Money movement screens for deposit, withdraw, and transfer
- JWT-aware route protection and centralized API client behavior
- Role-aware UI differences for `CUSTOMER` and `ADMIN`
- Transaction history view with date filtering, category display, and PDF export
- Transaction recategorisation for DEBIT and TRANSFER transactions
- Standing order create, list, and cancel flows
- Monthly statement download (PDF)
- Spending insights view with category breakdown pie chart and six-month trend bar chart

### Out of Scope

- Native mobile clients
- Rich internal-admin analytics features unrelated to the documented endpoints
- Any backend capabilities not documented in the current unified backend API contract

---

## 3. Supported Backend Capabilities

The frontend must support these backend capabilities.

### Authentication and Customer

- User Registration
- User Login
- Password Reset Request
- Token Refresh
- Create Customer
- Update Customer Profile
- Get Customer Details

### Accounts and Money Movement

- Delete Customer
- Create Account
- Retrieve Account Details
- List Customer Accounts
- Update Account
- Delete Account
- Deposit
- Withdraw
- Transfer Funds

### Transactions, Standing Orders, Statements, and Insights

- Get Transaction History (with optional date range filter)
- Export Transaction History as PDF
- Recategorise Transaction (DEBIT/TRANSFER only)
- Create Standing Order
- List Standing Orders
- Cancel Standing Order
- Download Monthly Statement (PDF)
- Get Spending Insights (category breakdown + six-month trend)

---

## 4. User Flows

### Flow A — Register then Log In

1. User opens the registration page.
2. User enters email and password.
3. Frontend validates required fields before submitting.
4. Backend returns `201` on success.
5. On success, user is redirected to login.

### Flow B — Log In and Route to Banking Home

1. User opens the login page.
2. User enters email and password.
3. Frontend stores `accessToken`, `refreshToken`, `tokenType`, and `expiresIn`.
4. Frontend determines whether the user already has a customer profile.
5. If no customer profile exists, route to customer creation.
6. If a customer profile exists, route to customer overview and account list.

### Flow C — Create Customer Profile

1. Authenticated user without a customer profile opens the create customer page.
2. User submits name, address, and customer type.
3. Backend returns the created customer including `customerId`.
4. On success, frontend routes to the customer overview.

### Flow D — View and Update Customer Profile

1. Authenticated user opens the customer details page.
2. Frontend fetches customer data with Bearer authentication.
3. User may navigate to edit allowed fields.
4. On successful update, customer data is refreshed.

### Flow E — List Customer Accounts

1. Authenticated user opens the customer banking home.
2. Frontend calls list accounts for the current `customerId`.
3. UI renders zero-state, list-state, or error-state.

### Flow F — Create Account

1. Authenticated user opens create account.
2. User selects `CHECKING` or `SAVINGS`.
3. User provides initial `balance` plus the account-type-specific field required by the backend contract: `interestRate` for `SAVINGS` or `nextCheckNumber` for `CHECKING`.
4. On success, frontend routes to account details.

### Flow G — View Account Details

1. Authenticated user opens an account details page.
2. Frontend fetches account data and surfaces allowed actions based on role and account type.

### Flow H — Update Account

1. Authenticated user opens account edit.
2. For `SAVINGS`, frontend exposes `interestRate`.
3. For `CHECKING`, frontend exposes only fields that the current backend contract allows.
4. On success, frontend refreshes account details.

### Flow I — Delete Account

1. `ADMIN` user opens account details.
2. If balance is `0.00`, UI allows delete confirmation.
3. On success, frontend removes the account from active views and refreshes the account list.

### Flow J — Deposit Funds

1. Authenticated user opens deposit action for an eligible account.
2. User submits `amount` and optional `description`.
3. Frontend generates and sends an `Idempotency-Key`.
4. On success, balance and relevant account queries refresh.

### Flow K — Withdraw Funds

1. Authenticated user opens withdraw action.
2. User submits `amount` and optional `description`.
3. Frontend sends an `Idempotency-Key`.
4. Success or failure is shown without duplicate client re-submission side effects.

### Flow L — Transfer Funds

1. Authenticated user opens transfer action.
2. User enters `fromAccountId`, `toAccountId`, `amount`, and optional `description`.
3. Frontend sends an `Idempotency-Key`.
4. On success, source and destination balances are refreshed.

### Flow M — Delete Customer

1. `ADMIN` user opens customer details.
2. UI allows delete only as an admin action.
3. If the customer has active accounts, the backend returns `409` and the UI shows the business constraint.

### Flow N — Password Reset Request

1. User opens the password reset request page.
2. User enters email.
3. Frontend submits the request when the backend endpoint becomes available.
4. UI shows a generic success response regardless of whether the email exists.

### Flow O — Token Refresh

1. Frontend detects an expired access token.
2. Frontend attempts a refresh using the refresh token.
3. If refresh succeeds, the original request may be retried once.
4. If refresh fails, the user is logged out and redirected to login.

---

## 5. Application Routes

The React application should provide at least these routes.

| Route | Access | Purpose |
|---|---|---|
| `/` | Public | Landing page or auth redirect |
| `/register` | Public | User registration |
| `/login` | Public | User login |
| `/password-reset` | Public | Password reset request |
| `/customer/create` | Authenticated | Create customer profile |
| `/customer/:customerId` | Authenticated | Customer details |
| `/customer/:customerId/edit` | Authenticated | Update customer profile |
| `/customer/:customerId/accounts` | Authenticated | List customer accounts |
| `/customer/:customerId/accounts/create` | Authenticated | Create account |
| `/accounts/:accountId` | Authenticated | Account details |
| `/accounts/:accountId/edit` | Authenticated | Update account |
| `/accounts/:accountId/deposit` | Authenticated | Deposit funds |
| `/accounts/:accountId/withdraw` | Authenticated | Withdraw funds |
| `/accounts/transfer` | Authenticated | Transfer funds |
| `/accounts/:accountId/transactions` | Authenticated | Transaction history |
| `/accounts/:accountId/standing-orders` | Authenticated | Standing orders |
| `/accounts/:accountId/statements` | Authenticated | Monthly statement download |
| `/accounts/:accountId/insights` | Authenticated | Spending insights |

Notes:

- Customer users should land on their own customer overview or account list after login.
- Admin users may navigate across customer and account resources beyond their own ownership scope.
- Admin-only delete actions may be surfaced inside the relevant details pages rather than separate routes.

---

## 6. Page Specifications

## 6.1 Registration Page

### Purpose

Collect email and password and register a new user.

### Fields

- `username`
- `password`

### Success Behaviour

- Show confirmation and redirect to login.

### Error Behaviour

- Show duplicate-user error for `USER_ALREADY_EXISTS`.

---

## 6.2 Login Page

### Purpose

Authenticate the user and obtain JWT tokens.

### Fields

- `username`
- `password`

### Success Behaviour

- Persist tokens.
- Bootstrap authenticated application state.
- Route user into customer/account area.

### Error Behaviour

- Show generic invalid credentials messaging.

---

## 6.3 Password Reset Request Page

### Purpose

Support the future password reset endpoint without requiring route redesign later.

### Fields

- `username`

---

## 6.4 Customer Create Page

### Fields

- `name`
- `address`
- `type` with values `PERSON` or `COMPANY`

### Success Behaviour

- Redirect to customer details or customer banking home.

---

## 6.5 Customer Details Page

### Data Display

- `customerId`
- `name`
- `address`
- `type`
- `createdAt`
- `updatedAt`
- summary entry point to customer accounts

### Actions

- Edit customer
- View accounts
- Admin-only delete customer
- Log out

---

## 6.6 Customer Edit Page

### Editable Fields

- `name`
- `address`

### Non-Editable Fields

- `customerId`
- `type` if the backend does not permit change
- system timestamps

---

## 6.7 Account List Page

### Purpose

Show all active accounts for a customer.

### Data Display

- `accountId`
- `accountType`
- `status`
- `balance`
- `interestRate` when relevant
- `nextCheckNumber` when relevant
- `updatedAt`

### States

- Empty-state when customer has zero accounts
- Standard list-state
- Auth or ownership failure state

---

## 6.8 Account Create Page

### Fields

- `accountType`
- `balance`
- `interestRate` for `SAVINGS` only
- `nextCheckNumber` for `CHECKING` only

### Dynamic Behaviour

- If `accountType=CHECKING`, `interestRate` must be hidden or disabled and `nextCheckNumber` must be required.
- If `accountType=SAVINGS`, `interestRate` must be required and `nextCheckNumber` must be hidden or disabled.

---

## 6.9 Account Details Page

### Data Display

- `accountId`
- `accountType`
- `status`
- `balance`
- `interestRate` when present
- `nextCheckNumber` when present
- `createdAt`
- `updatedAt`

### Actions

- Edit account when allowed
- Deposit
- Withdraw
- Transfer
- Admin-only delete account

---

## 6.10 Account Edit Page

### Purpose

Expose only fields that are mutable for the existing account type.

### Editable Fields

- `interestRate` for `SAVINGS`
- `nextCheckNumber` for `CHECKING`

### Non-Editable Fields

- `accountId`
- `customerId`
- `accountType`
- `balance`
- timestamps

### Dynamic Behaviour

- For `CHECKING`, the page must expose `nextCheckNumber` only.
- For `SAVINGS`, the page must expose `interestRate` only.
- The frontend must not submit unsupported fields.

---

## 6.11 Deposit Page or Modal

### Fields

- `amount`
- `description`

### Behaviour

- Generate a fresh `Idempotency-Key` for each new logical submit.
- Disable duplicate submit while request is in flight.

---

## 6.12 Withdraw Page or Modal

### Fields

- `amount`
- `description`

### Behaviour

- Generate and send `Idempotency-Key`.
- Surface insufficient funds as a business conflict, not a generic crash.

---

## 6.13 Transfer Page

### Fields

- `fromAccountId`
- `toAccountId`
- `amount`
- `description`

### Behaviour

- Prevent obviously invalid same-account submissions on the client.
- Send `Idempotency-Key` with the request.

---

## 6.14 Admin Delete Actions

### Customer Delete

- Available only to `ADMIN`.
- Confirm destructive action before submit.
- If active accounts exist, show the backend `409` constraint message.

### Account Delete

- Available only to `ADMIN`.
- Warn that only zero-balance accounts can be deleted.

---

## 7. API Integration Contract

The frontend must integrate with the unified backend service.

| Method | Endpoint | Frontend Use |
|---|---|---|
| `POST` | `/api/auth/register` | Registration submit |
| `POST` | `/api/auth/login` | Login submit |
| `POST` | `/api/auth/password-reset` | Password reset request |
| `POST` | `/api/auth/refresh` | Token refresh |
| `POST` | `/api/customers` | Create customer profile |
| `PATCH` | `/api/customers/{customerId}` | Update customer profile |
| `GET` | `/api/customers/{customerId}` | Get customer details |
| `DELETE` | `/customers/{customerId}` | Admin delete customer |
| `POST` | `/customers/{customerId}/accounts` | Create account |
| `GET` | `/accounts/{accountId}` | Get account details |
| `GET` | `/customers/{customerId}/accounts` | List customer accounts |
| `PUT` | `/accounts/{accountId}` | Update account |
| `DELETE` | `/accounts/{accountId}` | Admin delete account |
| `POST` | `/accounts/{accountId}/deposit` | Deposit funds |
| `POST` | `/accounts/{accountId}/withdraw` | Withdraw funds |
| `POST` | `/accounts/transfer` | Transfer funds |
| `GET` | `/accounts/{accountId}/transactions` | Transaction history |
| `GET` | `/accounts/{accountId}/transactions/export` | Export transactions as PDF |
| `PUT` | `/accounts/{accountId}/transactions/{transactionId}/category` | Recategorise transaction |
| `POST` | `/accounts/{accountId}/standing-orders` | Create standing order |
| `GET` | `/accounts/{accountId}/standing-orders` | List standing orders |
| `DELETE` | `/standing-orders/{standingOrderId}` | Cancel standing order |
| `GET` | `/accounts/{accountId}/statements/{period}` | Download monthly statement PDF |
| `GET` | `/accounts/{accountId}/insights` | Get spending insights |

### Base Rules

- Protected requests must include `Authorization: Bearer <accessToken>`.
- Public auth requests must not require a Bearer token.
- The API client must be centralized.
- The frontend must support a configurable base URL per environment.
- Money-movement requests must include an `Idempotency-Key` header.

### Expected Response Models

#### Login

```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### Customer

```json
{
  "customerId": 42,
  "name": "Jane Doe",
  "address": "123 Main St, Toronto, ON",
  "type": "PERSON",
  "accounts": [],
  "createdAt": "2026-04-07T10:00:00Z",
  "updatedAt": "2026-04-07T10:00:00Z"
}
```

#### Account

```json
{
  "accountId": 1001,
  "accountType": "CHECKING",
  "status": "ACTIVE",
  "balance": "125.00",
  "interestRate": null,
  "nextCheckNumber": 1201,
  "createdAt": "2026-04-07T10:00:00Z",
  "updatedAt": "2026-04-07T10:00:00Z"
}
```

#### Error

```json
{
  "code": "string",
  "message": "string",
  "field": "string | null"
}
```

---

## 8. State Management

### Transaction and Insights Behaviour

- Transaction category display follows these rules:
  - CREDIT transactions display "Not applicable for credits" and are not editable.
  - DEBIT and TRANSFER transactions with no category display "No category" and may be recategorised via a dropdown.
  - Valid categories: Housing, Transport, Food & Drink, Entertainment, Shopping, Utilities, Health, Income, Other, No category.
- Spending insights pie chart derives slice widths from `totalAmount` values, not the backend `percentage` field. Each category has a stable named colour. Only categories with non-zero spend are shown.
- Percentages across all visible categories must sum to exactly 100; the last slice absorbs any rounding remainder.
- CREDIT transactions do not contribute to spending insights totals. Only DEBIT and TRANSFER transactions are eligible.
- The six-month trend bar chart always renders all six months; months with no spend show zero bars.

### Minimum Recommended Approach

- React Router for routes and guards
- Axios for HTTP transport
- React Query for request lifecycle, caching, and mutation states
- Local React state for forms and modal state

### Auth State

The frontend should track at minimum:

- `accessToken`
- `refreshToken`
- decoded token metadata such as `sub`, `roles`, and `exp`
- current `customerId` when known

### Domain State

The frontend should also manage:

- current customer profile
- account list by `customerId`
- account detail by `accountId`
- mutation states for deposit, withdraw, transfer, delete, and update operations

### Cache Rules

- Customer details should be cached by `customerId`.
- Customer account lists should be cached by `customerId`.
- Account details should be cached by `accountId`.
- Successful mutations must invalidate or refresh affected customer and account queries.

---

## 9. Validation and Error Handling

### Client-Side Validation

- Validate required fields before submit.
- Validate email format on registration and login.
- Validate password presence on login and agreed complexity on registration.
- Validate customer type values.
- Validate account type values.
- Validate monetary amounts as positive numbers with up to two decimal places.
- Validate `interestRate` as non-negative with up to four decimal places when applicable.
- Validate `nextCheckNumber` as a whole number greater than or equal to `0` when applicable.
- Prevent `fromAccountId` and `toAccountId` from being equal on transfer.

### Server Error Handling

The UI must map backend errors into user-readable states without exposing internal internals.

| Error Code or Condition | Frontend Behaviour |
|---|---|
| `USER_ALREADY_EXISTS` | Show duplicate registration error |
| `INVALID_CREDENTIALS` | Show generic login failure |
| `ACCOUNT_INACTIVE` | Show inactive account message |
| `UNAUTHORISED` or `401` | Redirect to login or show access denied depending on context |
| `SESSION_EXPIRED` | Attempt refresh once, then log out on failure |
| `FIELD_NOT_UPDATABLE` | Show immutable-field warning |
| `MISSING_REQUIRED_FIELD` | Show field or form validation message |
| `ACCOUNT_NOT_FOUND` or `404` | Show missing account state |
| `CUSTOMER_NOT_FOUND` or `404` | Show missing customer state |
| `409` insufficient funds | Show business conflict message and keep current visible balance until refetch |
| `409` active accounts on delete | Show delete-blocked state for customer deletion |
| `409` non-zero balance on delete | Show delete-blocked state for account deletion |
| `422` invalid amount | Show inline amount error |
| `422` invalid `interestRate` | Show inline rate error |
| `422` invalid `nextCheckNumber` | Show inline check number error |

### Idempotency UX Rules

- The client must generate a new idempotency key for each new logical deposit, withdraw, or transfer submission.
- The client must not regenerate the key while retrying the same logical request due to a transient network issue.
- The UI should disable repeat-click duplicates while a mutation is pending.

---

## 10. Authentication and Authorization Behaviour

### Login Success Handling

- Save tokens after successful login.
- Decode the access token for non-authoritative UI hints only.
- Treat the backend as the source of truth for permissions and ownership decisions.

### Route Protection

- Unauthenticated users must not access customer or account routes.
- `CUSTOMER` users must be routed only to resources they are allowed to manage.
- `ADMIN` users may access admin-only actions and cross-customer resources.

### UI Authorization Rules

- Customer delete and account delete controls must be rendered only for `ADMIN`.
- Customer users may see deposit, withdraw, transfer, create account, read account, and allowed update actions only for owned resources.
- The UI may hide actions the user is clearly not allowed to invoke, but must still handle backend `401` responses correctly.

### Refresh Strategy

- If implemented, retry only once after an expired-token response.
- If refresh fails, clear tokens and redirect to login.

### Logout

- Clear auth state and cached protected data.
- Redirect to login.

---

## 11. UI and UX Requirements

### General Requirements

- The application must be responsive across desktop and laptop viewports.
- Forms and actions must show loading, success, and error states.
- Protected routes must not flash protected data before auth checks complete.
- Submit buttons must be disabled during in-flight mutations.
- Destructive actions must require confirmation.

### Accessibility Requirements

- Every form control must have a visible label.
- Validation errors must be rendered accessibly.
- Keyboard navigation must be supported throughout the app.

### Design Direction

- Clean, modern banking UI
- Clear separation between public auth pages and authenticated banking pages
- Obvious visual handling for balances, account status, and destructive/admin-only actions

---

## 12. Testing Expectations

### Unit and Component Tests

- Registration validation and submit states
- Login token handling
- Route guard behavior
- Customer create and update forms
- Account create and update forms
- Deposit, withdraw, and transfer form validation
- Delete confirmation behavior for admin-only actions

### Integration Tests

- Register then login flow
- Login then create customer flow
- Login then list accounts flow
- Create account flow for `CHECKING` and `SAVINGS`
- Retrieve account details flow
- Update account flow for mutable fields by account type
- Deposit flow with idempotency behavior at client level
- Withdraw flow with insufficient funds error display
- Transfer flow with same-account validation and success path
- Admin delete account and delete customer flows

### QA Scenarios

- Missing token blocks all protected routes
- Ownership failures surface correctly for customer users
- Empty account list renders as a valid success state
- Account creation enforces savings/checking field compatibility
- Account deletion is blocked when balance is non-zero
- Customer deletion is blocked when active accounts exist
- Money movement mutations refresh affected balances after success

---

## 13. Definition of Done

The frontend work is complete when all of the following are true:

- A React application exists with the routes defined in this specification.
- Registration, login, customer create, customer get, and customer update flows work against the unified backend.
- Account create, retrieve, list, update, delete, deposit, withdraw, transfer, transaction history, recategorisation, standing orders, monthly statement, and spending insights flows work against the unified backend.
- CREDIT transactions show "Not applicable for credits" in the category column and cannot be recategorised.
- Authenticated API access and token handling are centralized.
- Money-movement requests include idempotency support.
- Role-aware UI behavior is implemented for `CUSTOMER` and `ADMIN`.
- Error states from both services are mapped into usable UI states.
- Tests cover the primary happy paths and critical failure paths.
- The architecture still leaves room to plug in a future standalone transactions microservice without reworking auth and core banking navigation.

---

## 14. UI Change Log

### [2026-04-23] Registration Page — Two-Step Wizard Refactor

**Scope:** `src/pages/RegisterPage.jsx`, `src/styles.css`

#### What Changed

**Step structure**
- The registration form was refactored from a single-page layout into a two-step wizard controlled by a `step` state (`'selectType'` | `'details'`).
- Step 1 presents only the Account Type dropdown (`PERSON` / `COMPANY`) and a "Continue" button.
- Step 2 presents the remaining fields and a "Back" button that returns to Step 1 while preserving all entered `formState`.

**Progress indicator**
- A stepper is displayed at the top of the card showing "1. Account Type → 2. Details".
- The active step is highlighted in the app's green (`--accent`).
- When Step 2 is active, Step 1 shows a filled green circle with a checkmark (✓) — no strikethrough text.
- Non-active future steps are displayed in a neutral muted colour.

**Step 2 field layout**
- All four fields (Email, Password, Name, Address) are rendered in a single-column `stack` layout, each taking the full card width on its own row.
- The name field label is dynamic: "Full Name" for `PERSON`, "Company Name" for `COMPANY`.

**Page and card styling (updated)**
- The custom `register-page` / `register-card` wrapper (white card on `#efe3d2` background) was removed.
- The register page now uses the same `<section class="panel stack auth-panel-page">` structure as `LoginPage`, ensuring consistent appearance across all auth screens.
- The bespoke `.register-page` and `.register-card` CSS classes are no longer used for the page shell.
- The stepper (`register-stepper`, `register-step`, `register-step-check`, `register-step-arrow`) styles are retained.

**Unchanged**
- All `handleSubmit`, `useMutation`, and field validation logic remain untouched.
- API payloads sent to the backend are identical to before.

---

### [2026-04-23] Global Layout — Sidebar Replaced with Horizontal Navbar + Conditional Sub-Navbar

**Scope:** `src/App.jsx`, `src/styles.css`

#### What Changed

**Layout structure**
- The two-column sidebar layout (`app-shell` as a CSS grid) was replaced with a full-width stacked layout (`app-shell` as `flex-direction: column`).
- The `<aside class="sidebar">` element was removed entirely.
- The main content area now spans the full page width below the navigation bars.

**Main Navbar (`<header class="navbar">`)**
- A persistent top navbar is rendered on all pages, authenticated or not.
- Left side: "FDM" brand name in bold.
- Right side is conditional on auth state:
  - If **not authenticated**: shows a `Login` link and a `Get Started` button.
  - If **authenticated**: shows a 32×32 circular profile avatar placeholder and a `Log Out` button.

**Sub-Navbar (`<div class="subnav">`)**
- The sub-navbar is only rendered when `isAuthenticated` is `true`; it is absent entirely for logged-out users.
- Contains three horizontally-aligned links:
  - **Overview** → `/`
  - **Customer Profile** → `/customer/${customerId}` (or `/customer` for `ADMIN` users)
  - **Customer Accounts** → `/customer/${customerId}/accounts` (only rendered once `customerId` is known)
- Active links are highlighted with the app's green (`--accent`) bottom border.
- The sub-navbar is `position: sticky` at `top: 64px` (directly below the main navbar).

**Styles added**
- `.navbar`, `.navbar-brand`, `.navbar-actions`, `.navbar-profile`, `.profile-avatar` — main navbar layout and identity.
- `.subnav`, `.subnav-list` — secondary nav bar with horizontal link layout and active-state underline indicator.
- Responsive breakpoint updated to adjust padding on smaller viewports; old sidebar rules removed.

**Unchanged**
- All route definitions, auth guards, and `ProtectedRoute` logic remain untouched.
- `useAuth` hook usage is identical; no changes to `AuthContext`.

---

### [2026-05-04] Profile Security & Navigation Refinement

**Scope:** `src/pages/Profile.jsx`, `src/components/Navbar.jsx`, `src/App.jsx`

#### What Changed

**Customer Profile Editing Restrictions**
- **Read-Only Fields:** The `Name` and `Email` input fields are now disabled/read-only to prevent unauthorized identity changes.
- **Support Hint:** Added a muted text notice: "Contact support to update this information" directly below the non-editable fields.
- **Editable Fields:** The `Address` field remains fully interactive, allowing users to update their primary residence info.

**Navigation & Feature Access**
- **Transfer Funds Tab:** Added a new "Transfer Funds" link to the sub-navbar pointing to `/accounts/transfer`.
- **Persistent Active State:** Updated the `NavLink` logic to ensure the green accent underline remains active when query parameters are present (e.g., `?fromAccountId=1003`).
- **Conditional Visibility:** Refined the sub-navbar to ensure account-specific features (Accounts, Transfer) only render once a `customerId` is successfully resolved in the auth context.

**Global Error Handling**
- **Custom 404 Page:** Implemented a themed `NotFoundPage` that catches all undefined routes using a wildcard `*` path.
- **Navigation State Fallback:** Updated the Account List page to check for `location.state.successMessage`, allowing for "Registration Successful" banners to persist across the redirect from the sign-up flow.

**Styles Updated**
- Added `.banner.success` styles using Voltio Green (#00684a) for account creation and registration feedback.
- Applied disabled-state styling to inputs to ensure visual clarity on non-editable profile data.

---

### [2026-05-04] Overview Page Features & Global Footer

**Scope:** `src/pages/Home.jsx`, `src/App.jsx`, `src/styles.css`

#### What Changed

**Featured Offers Slider (Home Page)**
- **Interactive Carousel:** Implemented a "Featured Offers" panel with a sliding track for monthly banking promotions.
- **Auto-Sliding Logic:** Added a state-managed slider using `activeOfferIndex` with smooth `translateX` transitions.
- **Pagination Controls:** Included interactive "dots" (tablist role) that allow users to manually jump to specific offer slides.
- **Offer Content:** Each slide supports a kicker/badge, title, description, and high-quality media/images.

**Global Footer Implementation**
- **Persistent Bottom Bar:** Added a comprehensive footer (`.overview-footer`) to the main layout.
- **Metadata & Support:** Displays dynamic copyright information with `currentYear` and localized support contact details (Email + Operating Hours).
- **Social Integration:** Included a social media link cluster (`.footer-social`) with accessible labels for Facebook, Instagram, X, LinkedIn, and YouTube.
- **Layout Alignment:** The footer uses a three-column bottom row layout for balanced spacing between meta info, support hours, and social icons.

**Contextual Navigation (Home Page)**
- **Auth-State Branching:**
  - **Public:** Displays a "Get Started" call-to-action for non-authenticated users.
  - **Admin:** Provides "Open Customer" and "Open Account" quick-lookup tools.
  - **Customer:** Offers direct deep-links to "My Profile" and "My Accounts" once a customer ID is linked or registered.

**Styles Updated**
- Added `.offers-slider-window` and `.offers-slider-track` for the carousel animation logic.
- Implemented `.footer-bottom-row` and `.footer-meta` for consistent branding across all application screens.

---

### [2026-05-04] Authentication Architecture — JWT Implementation

**Scope:** `src/auth/AuthContext.jsx`, `src/api/axiosClient.js`, `LocalStorage`

#### What Changed

**Token-Based Authentication**
- **JWT Storage:** Replaced local boolean flags with a secure JWT flow. The `accessToken` and `refreshToken` are now stored in `LocalStorage` to persist user sessions across browser refreshes.
- **Payload Parsing:** Implemented `jwt-decode` (or similar logic) to extract user roles (`ADMIN`, `USER`), `customerId`, and expiration timestamps directly from the token.
- **Dynamic Auth State:** The `isAuthenticated` state in `AuthContext` now depends on the presence and validity of a stored token rather than a hardcoded value.

**Axios Security Interceptors**
- **Authorization Header:** Added a global Axios Request Interceptor that automatically attaches the `Authorization: Bearer <token>` header to every outgoing API request.
- **401/403 Handling:** Added a Response Interceptor to catch "UNAUTHORISED" errors. If the backend rejects a token, the frontend automatically triggers a logout and redirects the user to the `/login` page to prevent raw JSON leaks.

**Customer Context Linking**
- **UUID Mapping:** Upon login, the app maps the JWT `sub` claim to the `banking-app-customer-contexts` object, ensuring that the "My Accounts" and "Profile" links always point to the correct `customerId` associated with that specific token.

**Styles & UX Updated**
- **Login Redirects:** The `ProtectedRoute` component now uses the `location` state to remember the user's intended destination, redirecting them back to their original page after a successful JWT-authenticated login.
- **Auth Guard:** Updated the top Navbar to conditionally show the `Profile` avatar only when a valid, non-expired token is present.

---

## Future Extension Note

If a separate transactions microservice is added later, the React application should integrate it through the same authenticated shell, shared route protection, and centralized API client conventions defined here. Current deposit, withdraw, and transfer UX should therefore be designed as modular features that can be re-pointed or extended without rewriting the app foundation.
