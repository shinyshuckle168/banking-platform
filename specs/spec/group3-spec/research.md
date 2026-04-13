# Research: Group 3 — Digital Banking Platform

**Phase**: 0 — Outline & Research  
**Feature**: Group 3 Stories (US-01 through US-05)  
**Date**: 2026-04-08

---

## 1. Canadian Bank Holiday Detection

### Decision
Use a curated in-process `CanadianHolidayService` backed by a hard-coded or configuration-driven list of statutory federal holidays (New Year's Day, Good Friday, Victoria Day, Canada Day, Civic Holiday, Labour Day, Thanksgiving, Remembrance Day, Christmas Day, Boxing Day). Weekend detection uses `DayOfWeek.SATURDAY` / `DayOfWeek.SUNDAY` from `java.time`.

### Rationale
No external API dependency is needed. Federal statutory holidays are stable and published annually by the Bank of Canada. A static configuration file (YAML or constants class) satisfies the spec requirement to shift `nextRunDate` to the next Canadian business day without a network call.

### Alternatives Considered
- **Joda-Time HolidayCalendar**: Overkill; Joda-Time is superseded by `java.time` in Java 21.
- **Nager.Date public API**: Introduces an external runtime dependency with availability risk. Rejected.
- **Database-driven holiday table**: Adds schema complexity for a stable, low-change dataset. Rejected.

---

## 2. Modulo 97 Checksum Validation (IBAN-style)

### Decision
Implement `Mod97Validator` as a pure utility class using the standard IBAN Modulo 97 algorithm:
1. Move the first four characters to the end of the string.
2. Replace each letter with two digits (A=10, B=11, … Z=35).
3. Compute the integer remainder of the resulting number divided by 97.
4. The account number is valid if the remainder equals 1.

Expose as a custom Bean Validation `@Constraint` annotation (`@ValidPayeeAccount`) applied to the `payeeAccount` field on the standing order request DTO.

### Rationale
The spec mandates Modulo 97 validation for `payeeAccount` with a maximum of 34 characters, which is consistent with IBAN structure. A self-contained utility class with a custom constraint keeps validation declarative and testable in isolation.

### Alternatives Considered
- **Apache Commons Validator IBAN check**: Couples validation to IBAN country-code rules, which the spec does not require. Rejected.
- **Regular expression only**: Cannot catch transposition errors. Rejected.

---

## 3. PDF Statement Generation (US-01 Export)

### Decision
Use **iText 7 Community** (AGPL licence is acceptable for internal tooling; a commercial licence is needed for distribution). The `PdfController` delegates to `PdfStatementService`, which builds the PDF in-memory and returns `byte[]`. The response uses `Content-Type: application/pdf` and `Content-Disposition: attachment`. Idempotency is achieved by storing a hash of the request parameters (accountId + effective startDate + endDate) as a cache key in an `ExportCache` table (accountId, paramHash, pdfData, createdAt). Identical requests within the idempotency window return the cached `pdfData`.

### Rationale
iText 7 is the industry-standard Java PDF library. The spec requires idempotent export — identical requests must return the same file — which requires either deterministic rendering or caching. Caching the rendered PDF bytes keyed on request parameters is simpler and avoids rendering non-determinism.

### Alternatives Considered
- **Apache PDFBox**: Lower-level API; less ergonomic for structured document layouts. Rejected.
- **Jasper Reports**: Heavy dependency, XML templating overhead. Rejected.
- **Deterministic re-generation without cache**: Cannot guarantee bit-identical output if font rasterisation differs across renders. Rejected.

---

## 4. mTLS / API Key Authentication for US-10

### Decision
US-10 (`POST /notifications/evaluate`) is invoked by upstream event producers, not by end-users. Authentication uses one of two mechanisms:

- **mTLS**: The Spring Boot application is configured with `server.ssl.client-auth=need` for the `/notifications/**` path (or a separate internal port). The client certificate's Subject DN is validated against an allow-list stored in configuration.
- **API Key**: If mTLS is not available (e.g., local development), the caller supplies an `X-Api-Key` header. The key is validated against an allow-listed `ServiceRegistry` configuration map (serviceId → apiKey). Keys are stored hashed (SHA-256) in configuration, never in plaintext.

A Spring Security `AuthenticationProvider` (`ServiceAuthenticationProvider`) handles both mechanisms and populates a `ServiceAuthentication` principal. The `SecurityConfig` applies this provider only to `/notifications/**`.

### Rationale
The spec requires "mTLS or API Key from allow-listed ServiceID." Both must be supported. Separating the `/notifications/evaluate` endpoint's security context from the JWT Bearer context (used by all user-facing endpoints) ensures the two auth schemes do not interfere.

### Alternatives Considered
- **Basic Auth**: Transmits credentials without additional encoding protection. Rejected.
- **OAuth2 Client Credentials**: Adds an authorisation server dependency that the spec does not mention. Rejected.

---

## 5. Transaction Categorisation (US-05 Spending Insights)

### Decision
Implement a `CategoryResolver` service that maps transaction descriptions to spending categories using keyword matching. A configuration-driven map (`application.yml`) lists include-keywords per category. The `Transaction` entity gains an optional `category` column (nullable String, max 50) to store the assigned or overridden category. On `SpendingInsightService.getInsights()`, uncategorised eligible transactions are auto-assigned via `CategoryResolver` before aggregation. Manual overrides (from `PUT /accounts/{accountId}/transactions/{transactionId}/category`) write directly to `transaction.category` and return the recalculated breakdown.

### Rationale
The spec fixes the eight categories and states the system must not invent mappings. Keyword-based auto-categorisation with a configurable ruleset satisfies this. The `transaction.category` column stores the last resolved category, so re-aggregation does not require re-evaluating every rule on every request.

### Alternatives Considered
- **ML-based categorisation**: The spec states "the system must not invent or estimate category mappings." ML uncertainty is incompatible. Rejected.
- **Separate `CategoryOverride` table**: Adds a join on every insight query. Storing directly on `Transaction` is simpler and consistent with immutability rules — the category field is the only mutable field after write. Rejected (storing on Transaction is preferred).

---

## 6. Monthly Statement Versioning (US-04)

### Decision
`MonthlyStatementEntity` is persisted with a composite unique constraint on `(accountId, period, versionNumber)`. The latest version is resolved via `findTopByAccountIdAndPeriodOrderByVersionNumberDesc`. Corrections insert a new row with `versionNumber = previousMax + 1` and a populated `correctionSummary`. The original row is never updated or deleted.

### Rationale
The spec mandates that "originals must never be overwritten." An append-only versioned table satisfies this with a simple `MAX(versionNumber)` query for the default retrieval path.

### Alternatives Considered
- **Event-sourced statement log**: Too complex for this scope. Rejected.
- **Separate `StatementCorrection` table**: Requires a join to reconstruct the full statement. Rejected.

---

## 7. Idempotency Store

### Decision
Add an `IdempotencyRecord` table: `idempotencyKey` (PK, VARCHAR 255), `responseBody` (TEXT or BLOB), `statusCode` (INT), `createdAt` (TIMESTAMP). On each monetary operation, check for an existing record before executing. If found, return the stored response. Records older than 72 hours are purged by a scheduled `@Scheduled` task running at 03:00 UTC daily.

### Rationale
The spec allows purging idempotency records after 24–72 hours. Storing the full serialised response against the key is the simplest correct implementation. Transaction records are retained independently.

### Alternatives Considered
- **Redis cache**: Adds an infrastructure dependency not described in the platform stack. Rejected.
- **In-memory ConcurrentHashMap**: Does not survive application restart. Rejected.

---

## 8. React Query v5 Patterns

### Decision
Use `useQuery` for all read operations (GET). Use `useMutation` for write operations (POST, PUT, DELETE). All queries are keyed by resource + parameters (e.g., `['transactions', accountId, startDate, endDate]`). On mutation success, invalidate related queries. Error responses from the API use the `ErrorResponse` schema; the frontend extracts `code`, `message`, and `field` from the response body for display.

### Rationale
React Query v5 is specified in the stack. Its built-in stale-while-revalidate pattern, automatic retry, and cache invalidation cover all interactive requirements (real-time recategorisation, standing order list refresh after create/cancel).

### Alternatives Considered
- **SWR**: Not in the specified stack. Rejected.
- **Manual fetch + useState**: Does not satisfy the real-time recategorisation requirement without significant boilerplate. Rejected.
