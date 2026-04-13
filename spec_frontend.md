# Frontend Specification

**Project:** Digital Banking Platform  
**Frontend Stack:** React 18 · JavaScript · React Router · Axios · React Query  
**Backend Dependency:** merged banking backend  
**Current Backend Location:** `/home/kap/fdm_skills_lab/Sprint6/banking-platform/merged-backend`

---

## 1. Purpose

This document defines the frontend requirements for the standalone React app in `frontend-app`.

It reflects two realities that the frontend must support:

- the merged backend that is available now for auth, customer, account, deposit, and withdraw journeys
- the future Group 3 backend contract in `group3-spec.md` on branch `spec/group3-spec`, which the frontend should scaffold against now so those pages can be wired quickly after the backend merge

The currently available merged backend is a single Spring Boot service exposing:

- authentication endpoints under `/api/auth`
- customer endpoints under `/api/customers`
- account endpoints under `/customers/*` and `/accounts/*`

This specification replaces the earlier assumption that the frontend would call separate `login-api` and `account-service` applications, and adds frontend scaffolding requirements for these future Group 3 journeys:

- transaction history
- standing orders
- monthly statements
- spending insights

Notifications are explicitly out of scope for this frontend iteration.

---

## 2. Current Scope

### In Scope

- Public registration and login screens
- Placeholder password-reset screen
- Authenticated customer create, read, update, and delete flows
- Registration-driven customer bootstrap flow that creates the initial customer profile immediately after user registration
- Account create, read, list, update, delete, deposit, and withdraw flows
- JWT-based route protection and persisted session state
- Local customer-context memory in the browser because the backend does not expose a "get my current customer" endpoint
- Admin-only customer switching on customer profile and customer accounts pages
- Combined account listing and account creation page per customer
- Quick account-ID navigation from the overview page for account detail, deposit, and withdraw pages
- Frontend scaffolding for transaction history with date-range filters, chronological display, status display, inline PDF availability, and export action
- Frontend scaffolding for standing order list and create flow on an account
- Frontend scaffolding for monthly statements with year-month input and statement-style transaction rendering
- Frontend scaffolding for spending insights with a period filter, bar-chart view, and one-month pie-chart view

### Out of Scope

- Separate microservice targeting for auth and account calls
- Token refresh API integration because the backend does not expose a refresh endpoint
- Notifications UI and notification-event evaluation flow for now
- Production charting/reporting polish beyond the initial scaffolded experience
- Live Group 3 backend integration before the backend merge exists

---

## 3. Backend Reality

### Base URL

- Development backend URL: `http://localhost:8080`
- Frontend development URL: `http://localhost:5173`

### Service Layout

- Single backend application
- No separate auth service port
- No separate account service port

### Public Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Log in and receive JWT tokens |

### Protected Customer Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/customers` | Create customer profile |
| `GET` | `/api/customers/{customerId}` | Get customer |
| `PATCH` | `/api/customers/{customerId}` | Update customer |
| `GET` | `/api/customers` | Admin-only customer list |
| `DELETE` | `/api/customers/{customerId}` | Admin-only customer delete |

### Protected Account Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/customers/{customerId}/accounts` | Create account |
| `GET` | `/customers/{customerId}/accounts` | List customer accounts |
| `GET` | `/accounts/{accountId}` | Get account |
| `PUT` | `/accounts/{accountId}` | Update account |
| `DELETE` | `/accounts/{accountId}` | Delete account if balance is zero |
| `POST` | `/accounts/{accountId}/deposit` | Deposit funds with idempotency support |
| `POST` | `/accounts/{accountId}/withdraw` | Withdraw funds with idempotency support |

### Planned Group 3 Endpoints For Future Merge

These routes are not available from the current merged backend yet, but the frontend scaffold should be designed around them.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/accounts/{accountId}/transactions` | Retrieve transaction history for a date range |
| `GET` | `/accounts/{accountId}/transactions/export` | Export transaction history PDF |
| `POST` | `/accounts/{accountId}/standing-orders` | Create standing order |
| `GET` | `/accounts/{accountId}/standing-orders` | List standing orders |
| `DELETE` | `/standing-orders/{standingOrderId}` | Cancel standing order |
| `GET` | `/accounts/{accountId}/statements/{period}` | Retrieve monthly statement |
| `GET` | `/accounts/{accountId}/insights` | Retrieve spending insights |
| `PUT` | `/accounts/{accountId}/transactions/{transactionId}/category` | Recategorise a transaction |

### Known Runtime Limitation In The Current Merged Backend

- Freshly authenticated deposit and withdraw requests are currently rejected by the backend with `Authenticated user not found` or `Authentication required`.
- The frontend should surface that failure clearly rather than presenting a vague generic error.

### Disabled or Missing Backend Capabilities

| Capability | Status | Frontend Handling |
|---|---|---|
| Password reset API | Not implemented | Placeholder page only |
| Refresh token API | Not implemented | No automatic refresh flow |
| Next check number | Not implemented | No UI support |
| Current-customer lookup by token | Not implemented | Browser stores last known `customerId` |

---

## 4. Data Contracts

### Registration Request

Frontend registration is a two-step orchestration:

1. `POST /api/auth/register` with credentials
2. `POST /api/auth/login` with the same credentials
3. `POST /api/customers` with the collected customer profile using the returned access token

The registration form therefore collects both auth fields and the initial customer profile fields.

```json
{
  "username": "user@example.com",
  "password": "PassWord123!",
  "name": "Jane Doe",
  "address": "123 Main St",
  "type": "PERSON"
}
```

### Register API Request

```json
{
  "username": "user@example.com",
  "password": "PassWord123!"
}
```

### Registration Response

```json
{
  "userId": "uuid",
  "username": "user@example.com",
  "roles": ["CUSTOMER"],
  "externalSubjectId": null,
  "customerId": null,
  "isActive": true,
  "createdAt": "2026-04-09T12:00:00Z"
}
```

### Login Response

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Customer Request

```json
{
  "name": "Jane Doe",
  "address": "123 Main St",
  "type": "PERSON"
}
```

### Customer Response

```json
{
  "customerId": 1,
  "name": "Jane Doe",
  "address": "123 Main St",
  "type": "PERSON",
  "accounts": [],
  "createdAt": "2026-04-09T12:00:00Z",
  "updatedAt": "2026-04-09T12:00:00Z"
}
```

### Account Create Request

```json
{
  "accountType": "SAVINGS",
  "balance": "100.00",
  "interestRate": "0.0500"
}
```

### Account Response

```json
{
  "accountId": 100001,
  "customerId": 1,
  "accountType": "SAVINGS",
  "status": "ACTIVE",
  "balance": "100.00",
  "interestRate": "0.0500",
  "createdAt": "2026-04-09T12:00:00Z",
  "updatedAt": "2026-04-09T12:00:00Z"
}
```

### Error Response

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable message",
  "field": null
}
```

### Planned Transaction History Response

```json
{
  "accountId": 100001,
  "startDate": "2026-03-01T00:00:00Z",
  "endDate": "2026-03-31T23:59:59Z",
  "transactionCount": 3,
  "transactions": [
    {
      "transactionId": 901,
      "amount": "45.50",
      "type": "WITHDRAW",
      "status": "SUCCESS",
      "timestamp": "2026-03-04T09:30:00Z",
      "description": "Groceries",
      "idempotencyKey": "uuid"
    }
  ]
}
```

### Planned Standing Order Create Request

```json
{
  "payeeAccount": "GB82WEST12345698765432",
  "payeeName": "Hydro Utility",
  "amount": "120.00",
  "frequency": "MONTHLY",
  "startDate": "2026-05-01T09:00:00Z",
  "endDate": "2026-12-01T09:00:00Z",
  "reference": "HYDRO2026"
}
```

### Planned Standing Order Response

```json
{
  "standingOrderId": "uuid",
  "sourceAccountId": 100001,
  "payeeAccount": "GB82WEST12345698765432",
  "payeeName": "Hydro Utility",
  "amount": "120.00",
  "frequency": "MONTHLY",
  "startDate": "2026-05-01T09:00:00Z",
  "endDate": "2026-12-01T09:00:00Z",
  "reference": "HYDRO2026",
  "status": "ACTIVE",
  "nextRunDate": "2026-05-01T09:00:00Z",
  "message": "Standing order created"
}
```

### Planned Monthly Statement Response

```json
{
  "accountId": 100001,
  "period": "2026-03",
  "openingBalance": "1000.00",
  "closingBalance": "875.25",
  "totalMoneyIn": "500.00",
  "totalMoneyOut": "624.75",
  "transactions": [],
  "versionNumber": 1,
  "correctionSummary": null,
  "generatedAt": "2026-04-01T00:30:00Z"
}
```

### Planned Spending Insights Response

```json
{
  "accountId": 100001,
  "period": {
    "year": 2026,
    "month": 3,
    "isComplete": true
  },
  "totalDebitSpend": "624.75",
  "transactionCount": 12,
  "hasUncategorised": false,
  "hasExcludedDisputes": false,
  "dataFresh": true,
  "categoryBreakdown": [
    {
      "category": "Food & Drink",
      "totalAmount": "210.00",
      "percentage": "33.61"
    }
  ],
  "topTransactions": [],
  "sixMonthTrend": []
}
```

---

## 5. Frontend Routes

| Route | Access | Status | Notes |
|---|---|---|---|
| `/` | Public | Active | Landing page and authenticated jump-off page |
| `/register` | Public | Active | Register against `/api/auth/register` |
| `/login` | Public | Active | Login against `/api/auth/login` |
| `/password-reset` | Public | Placeholder | No backend endpoint yet |
| `/customer/create` | Authenticated | Fallback | Only needed when an authenticated user still has no linked customer |
| `/customer` | Authenticated | Active | Admin customer-selection entry for the customer profile page |
| `/customer/:customerId` | Authenticated | Active | View customer |
| `/customer/:customerId/edit` | Authenticated | Active | Edit customer |
| `/customer/:customerId/accounts` | Authenticated | Active | List accounts and create account on one page |
| `/customer/:customerId/accounts/create` | Authenticated | Redirect | Redirects to the combined accounts page |
| `/accounts/:accountId` | Authenticated | Active | View account |
| `/accounts/:accountId/edit` | Authenticated | Active | Alternate route for account edit context |
| `/accounts/:accountId/deposit` | Authenticated | Active | Deposit funds with idempotency key |
| `/accounts/:accountId/withdraw` | Authenticated | Active | Withdraw funds with idempotency key |
| `/accounts/:accountId/transactions` | Authenticated | Planned scaffold | Transaction history with date pickers, status display, and export action |
| `/accounts/:accountId/standing-orders` | Authenticated | Planned scaffold | Standing order list with create form above it |
| `/accounts/:accountId/statements` | Authenticated | Planned scaffold | Monthly statement lookup by year and month |
| `/accounts/:accountId/insights` | Authenticated | Planned scaffold | Spending insights charts for a selected period |
| `/accounts/transfer` | Authenticated | Deprecated redirect | Redirects to `/` because the dedicated transfer page is no longer exposed in this frontend |

---

## 6. Session and Routing Rules

### Authentication

- Store `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, decoded `sub`, decoded `roles`, and last known `customerId` in local storage.
- Persist a customer-context mapping keyed by JWT `sub` so returning customer users regain their linked customer context after logout and login on the same browser.
- Synchronize that `sub` to `customerId` mapping whenever the settled authenticated state contains both values, so registration and later logins stay linked reliably.
- Attach `Authorization: Bearer <accessToken>` to protected requests.
- Treat backend authorization as the source of truth.

### Default Authenticated Routing

- If the authenticated user has an admin role, send them to `/customer` so customer selection happens from the customer profile page itself.
- If the app has a remembered `customerId`, send the user to `/customer/{customerId}`.
- If the app does not have a remembered `customerId`, send the user to `/` rather than forcing customer creation.

This matters because the backend does not expose a "get my customer profile by token" endpoint. A returning user may already have a customer record even when the browser has lost local context.

### Customer Context Rules

- When registration finishes its customer-bootstrap sequence, remember the created `customerId` locally.
- When customer create succeeds from the fallback customer-create page, remember `customerId` locally.
- When customer fetch succeeds, remember `customerId` locally.
- When a customer user logs back in on the same browser, restore their remembered customer context from the persisted `sub` to `customerId` mapping.
- When an admin switches customer from the profile or accounts page, update the remembered `customerId` to the selected customer.
- When the currently remembered customer is deleted, clear local `customerId`.

---

## 7. Page-Level Behaviour

### Registration Page

- Accept `username`, `password`, `name`, `address`, and `type`.
- Validate email shape on the client.
- Require password, name, and address before submit.
- Submit the auth registration request, then log in automatically, then create the customer profile, then route directly to the new customer profile.
- Use backend enum values:
  - `PERSON`
  - `COMPANY`

### Login Page

- Accept `username` and `password`.
- Persist login response and decoded JWT metadata.
- Restore any previously remembered customer context for the authenticated `sub` before deciding the post-login fallback route.
- Redirect to the originally requested protected route if present.
- Otherwise route to remembered customer profile or `/`.

### Home Page

- Show session summary and quick navigation for authenticated and unauthenticated users.
- For admins, allow opening any customer or account by ID from the overview page.
- For authenticated users, expose quick-navigation buttons for account detail, deposit, and withdraw when an account ID is entered.
- For authenticated users, also expose quick-navigation entry points for transaction history, standing orders, monthly statements, and spending insights for a chosen account.
- Keep the fallback customer-create link available when no remembered customer context exists.

### Password Reset Page

- Remains in the app as a placeholder page.
- Must explain that the backend does not currently expose the endpoint.

### Customer Create Page

- Send `name`, `address`, and `type`.
- Use backend enum values:
  - `PERSON`
  - `COMPANY`
- On success, store `customerId` and route to customer details.
- Keep this page available as a fallback flow for authenticated users who still do not have a linked customer context.

### Customer Detail Page

- Expose `/customer` as the admin entry point for selecting which customer profile to open.
- Show `customerId`, `name`, `address`, and `type`.
- Show `Edit Customer` and `Accounts` actions below the profile details instead of beside the header.
- If the current user is an admin, show a customer switcher populated from `GET /api/customers` and route to the selected customer profile.
- If an admin opens the customer profile page without a selected customer, show the switcher and a selection prompt instead of requiring the overview page first.
- If the current user is not an admin, do not show a customer switcher.
- Show delete only for admin users.

### Customer Edit Page

- Allow editing `name`, `address`, and `type`.
- Use backend enum values `PERSON` and `COMPANY`.
- Refresh customer data after successful update.

### Customer Accounts Page

- List customer accounts from `GET /customers/{customerId}/accounts`.
- If the backend responds with a generic customer-not-found error for a deleted customer, translate it in the UI to a clearer deleted-or-unavailable customer message.
- Show empty state if no active accounts are returned.
- Include the account-create form on the same page.
- Show per-account actions for detail, deposit, and withdraw directly in the account list.
- Show per-account actions for transaction history, standing orders, monthly statement, and spending insights directly in the account list so the Group 3 scaffold is discoverable without going through account detail first.
- Keep account-list actions compact, including quick links for deposit, withdraw, transaction history, standing orders, monthly statement, and spending insights.
- Allow `CHECKING` and `SAVINGS`.
- Always require `balance`.
- Only submit `interestRate` for `SAVINGS`.
- If the current user is an admin, show a customer switcher populated from `GET /api/customers` and route to the selected customer accounts page.
- If the current user is not an admin, do not show a customer switcher.

### Account Detail Page

- Show account core fields including `accountId`, `customerId`, `status`, `balance`, and `interestRate` when present.
- Allow deposit and withdraw navigation.
- Expose a transfer-funds button from account detail as a quick action.
- Include a statement-month picker in account detail so users can launch monthly statements with a preselected period.
- Expose quick links for transaction history, standing orders, monthly statement, and spending insights from account detail.
- Allow account deletion when the user can access the account and the balance is zero.
- After account deletion succeeds, route back to the customer accounts page and show a success banner indicating that the account was deleted.
- If the backend responds with a generic account-not-found error for a deleted account, translate it in the UI to a clearer deleted-or-unavailable account message.
- Explain that savings accounts are the only type with a mutable field in the current backend.

### Deposit and Withdraw Pages

- Submit to `/accounts/{accountId}/deposit` and `/accounts/{accountId}/withdraw`.
- Require an idempotency key.
- Render the updated account payload and transaction payload after success.
- When the backend responds with `Authenticated user not found` or `Authentication required`, show a clear backend-auth-failure message rather than a generic error.

### Transaction History Page

- Route: `/accounts/:accountId/transactions`.
- Render a date-range form with separate start-date and end-date pickers.
- Default the UI to the last 28 days when the user has not chosen a range yet.
- Do not auto-request history on first render; request only after the user clicks `Apply Range`.
- Request history in a way that preserves the backend ordering contract, but also render clearly with pending entries visually separated from posted or final entries.
- Display transaction timestamp, description, type, amount, success-or-failed status, and a separate pending-or-posted state badge when the backend provides that distinction.
- Keep the list chronological within each backend status grouping.
- Expose an `Export PDF` action that uses the same selected range.
- Surface empty-state messaging when no transactions exist for the selected period.
- If the backend later exposes a freshness indicator, reserve space for a non-blocking stale-data banner.

### Standing Orders Page

- Route: `/accounts/:accountId/standing-orders`.
- Render the create-standing-order form above the existing orders list.
- Collect `payeeAccount`, `payeeName`, `amount`, `frequency`, `startDate`, optional `endDate`, and `reference`.
- Render current standing orders with `payeeName`, `amount`, `frequency`, `status`, `nextRunDate`, `startDate`, and `endDate`.
- Expose per-row cancellation for orders that are not already cancelled, locked, or terminated.
- Show a clear message when cancellation is blocked by the 24-hour processing lock.
- Preserve room in the UI for backend states such as `LOCKED` and `TERMINATED` even if the first scaffold is read-only against mock data.

### Monthly Statement Page

- Route: `/accounts/:accountId/statements`.
- Use a numeric Year input and Month dropdown selector that combine into `YYYY-MM` rather than a start and end date range.
- Do not auto-request statements on first render; request only after the user clicks `Load Statement`.
- Keep the statement layout intentionally close to transaction history so the user sees familiar transaction rendering.
- Display opening balance, closing balance, total money in, total money out, version number, correction summary when present, and generated timestamp.
- Render the full transaction list for the selected period, including both success and failed outcomes.
- Show dedicated states for period not closed yet, no statement found, and statement beyond self-service retention.

### Spending Insights Page

- Route: `/accounts/:accountId/insights`.
- Provide a numeric Year input and Month dropdown selector that combine into `YYYY-MM` for backend insights requests.
- Do not auto-request insights on first render; request only after the user clicks `Load Insights`.
- Render a vertical bar chart (column style with height-based bars) for the six-month trend returned by the backend.
- Render a pie chart for the selected month category breakdown without an inner ring treatment.
- Display summary metrics such as total debit spend, transaction count, uncategorised flag, and data freshness.
- When backend insights are unavailable, show a clearly labeled mock-data preview so chart layout and page behavior remain testable.
- Ensure mock insights remain internally consistent: if an uncategorised category bucket is present, the uncategorised summary indicator should report `Yes`.
- Keep the page scaffold compatible with later support for recategorising transactions, but do not prioritize that interaction in the first UI pass unless implementation needs it.
- Treat deposits as excluded from charts because the backend insight contract is debit-spend only.

### Notifications

- Do not add notification pages, badges, or evaluation UI in this iteration.
- Future backend notification events may be consumed later, but they should not influence current navigation or layout decisions.

---

## 8. Validation Rules

### Client Validation

- Email must look like an email address on login and registration.
- Password is required on login and registration.
- Name and address are required on registration because registration now boots the customer profile.
- Customer `type` must be one of `PERSON` or `COMPANY`.
- Account `accountType` must be `CHECKING` or `SAVINGS`.
- `interestRate` is relevant only for `SAVINGS`.
- Deposit and withdraw require an idempotency key.
- Transaction history filters must not allow a date range longer than 366 days.
- Standing order `frequency` must be one of `DAILY`, `WEEKLY`, `MONTHLY`, or `QUARTERLY`.
- Standing order `startDate` must be at least 24 hours in the future.
- Standing order `endDate`, when provided, must be later than `startDate`.
- Standing order `reference` must be 1 to 18 alphanumeric characters.
- Monthly statement lookup must require a valid numeric year and month selection that forms `YYYY-MM`.
- Spending insights input must require a valid numeric year and month selection that forms `YYYY-MM`.

### Backend Constraints the UI Must Respect

- `SAVINGS` account create requires `interestRate`.
- `CHECKING` account create must not include `interestRate`.
- Account delete only succeeds when balance is exactly zero.
- Deleted accounts are no longer retrievable through the active account endpoints.
- Customer delete only succeeds when no active accounts remain.
- Deleted customers still come back as generic not-found from the current backend, so the frontend must present a clearer deleted-or-unavailable message in the accounts UI.
- Creating a second customer for a linked user may fail with `CUSTOMER_ALREADY_LINKED`.
- Withdraw fails when the account balance is lower than the requested amount.
- Transaction history export requires a valid selected range before enabling the export action.
- Standing order creation fails when `payeeAccount` is invalid, the order duplicates an active one, or cancellation is attempted within 24 hours of the next run.
- Monthly statements may return `409` when the selected period is not closed and `410` when the statement is outside the self-service retention window.
- Spending insights may return `409` when the requested month has not started.

---

## 9. Authorization Rules

- All customer and account pages require authentication.
- Customer delete is admin-only in the UI because the backend restricts it to admins.
- Admin-only customer switching is available on the customer profile and customer accounts pages.
- Non-admin users do not get a customer switcher and should navigate using their remembered customer context only.
- Account delete is not limited to admins in the UI because the backend allows owner-or-admin access subject to the zero-balance rule.
- Deposit and withdraw must rely on backend ownership checks for the target account.
- Transaction history relies on `TRANSACTION:READ`.
- Standing orders rely on `SO:CREATE`, `SO:READ`, and `SO:CANCEL`.
- Monthly statements rely on `STATEMENT:READ`.
- Spending insights rely on `INSIGHTS:READ`.
- Notifications remain out of scope and therefore have no frontend authorization surface yet.

---

## 10. API Client Requirements

### Frontend Networking Model

- Use a single backend base URL rather than separate service URLs.
- In local development, the Vite app proxies these path groups to the merged backend:
  - `/api`
  - `/accounts`
  - `/customers`
  - `/standing-orders`

### Environment Variables

- `VITE_BANKING_API_BASE_URL` for direct frontend-to-backend requests outside dev proxy mode
- `VITE_BANKING_API_PROXY_TARGET` for local dev proxy target override

Legacy variables such as `VITE_LOGIN_API_BASE_URL` and `VITE_ACCOUNT_SERVICE_BASE_URL` may still be honored for compatibility, but the canonical model is now a single backend.

---

## 11. Testing Expectations

### Expected Working Flows

- Register user and automatically bootstrap customer profile
- Login user
- Create customer from the fallback authenticated flow
- View customer
- Update customer
- Switch customer context as admin from customer profile
- List customer accounts and create an account from the same page
- View account
- Deposit funds with idempotency key
- Withdraw funds with idempotency key
- Update savings account interest rate
- Delete zero-balance account

### Current Known Failing Flow

- Deposit and withdraw currently fail against the merged backend because the backend money-movement layer is rejecting authenticated requests.

### Planned Scaffold-Only Flows

- Open transaction history, choose a date range, render list state, and prepare a PDF export action.
- Open standing orders, view existing orders, and submit the create form.
- Open monthly statements, choose a year and month, and render statement layout states.
- Open spending insights, choose a period, and render bar-chart and pie-chart placeholders from mocked or adapter-backed data.

### Expected Placeholder Flows

- Password reset page should render an explanatory message
- Any Group 3 page may temporarily use mocked data or a local adapter until the backend merge lands, but the route structure and view-model shape must match the future contract defined here.

### Recommended Manual Checks

1. Register a new user and confirm the app lands directly on the created customer profile.
2. Log out and log back in with the same user.
3. Create one savings account and one checking account from the combined accounts page.
4. Deposit into one account and withdraw from another using idempotency keys.
5. Update the savings account interest rate.
6. Open the transaction history, standing orders, monthly statements, and insights routes for a known account and confirm the scaffolding renders without depending on notification flows.
7. If testing as admin, switch between customers from the profile and accounts pages.

---

## 12. Definition of Done

The frontend is considered aligned with the merged backend when:

- The app calls only the backend routes that actually exist.
- Auth and customer requests no longer target a separate port.
- Registration creates a customer and updates local customer context in one flow.
- Customer type values match the backend enums.
- Customer profile and customer accounts pages expose admin-only customer switching.
- Customer accounts and account creation live on one page.
- Deposit and withdraw remain fully operational with idempotency support.
- The transaction history page supports date pickers, chronological rendering, success or pending display, and PDF export wiring.
- The standing orders page presents a create form above the order list.
- The monthly statement page uses a year-month input and statement-style rendering.
- The spending insights page renders a bar chart for a selected period and a pie chart for one month.
- Notifications are intentionally absent from the frontend scope for now.
- The frontend specification matches the implementation and backend constraints.

---

## 13. Local Run Assumptions

- Backend runs from `banking-platform-merged/banking-platform` on port `8080`.
- Frontend runs from `banking-platform/frontend-app` on port `5173`.
- Default local configuration should work without extra environment variables if the backend stays on `8080`.
