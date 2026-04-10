# Digital Banking Platform Backend — Group 1

This backend implements the **auth and customer layer** from your Group 1 specification:

- US-101 Register
- US-102 Login
- US-103 Create Customer
- US-104 Update Customer
- US-105 Get Customer

## Stack

- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- H2 database
- JWT (HS256) via JJWT
- Swagger / OpenAPI
- JUnit + Mockito

## What is included

```text
src/main/java/com/group1/banking
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── mapper
├── repository
├── security
├── service
└── service/impl
```

## Important implementation notes

### 1) Roles model
Your spec mixes two different role designs:
- `User.roles` as a list of role values inside `User`
- a separate `Roles` entity with permissions

For the generated backend, I normalized this to:
- `RoleName` enum: `CUSTOMER`, `ADMIN`
- permission authorities derived in security code

That keeps the JWT contract simple and matches your endpoint authorization rules.

### 2) Customer immutability rule
The `Customer` entity in the spec does **not** define `email` or `accountNumber`, but the PATCH spec says both must be blocked.

To preserve your business rule, the generated backend handles those as **blocked DTO fields** in `PatchCustomerRequest`. If either is present, the request is rejected with:

```json
{
  "code": "FIELD_NOT_UPDATABLE",
  "message": "email and accountNumber cannot be updated.",
  "field": null
}
```

### 3) Ownership rule
The spec says CUSTOMER users must pass an ownership check. This backend includes an `OwnershipService` for `GET` and `PATCH`. Since `POST /api/customers` creates the customer first, ownership linking back to `User.customerId` is left ready for your next step or Group 2 integration.

## How to run

### 1. Set JWT secret
Create a local `.env` or export an environment variable:

```bash
export JWT_SECRET="ThisIsADevOnlySecretKeyForHs256JwtTokenMinimum32Chars"
```

### 2. Start the app

```bash
mvn spring-boot:run
```

### 3. Useful URLs

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
- Health: `http://localhost:8080/actuator/health`

## H2 settings

```properties
spring.datasource.url=jdbc:h2:mem:digitalbankdb
spring.datasource.username=sa
spring.datasource.password=
```

## JWT claims

Access and refresh tokens include:

```json
{
  "sub": "<uuid>",
  "roles": ["CUSTOMER"],
  "iat": 1712342078,
  "exp": 1712345678
}
```

## Endpoint summary

### Register
`POST /api/auth/register`

Request:
```json
{
  "username": "lalitha@example.com",
  "password": "Secure@123"
}
```

### Login
`POST /api/auth/login`

Request:
```json
{
  "username": "lalitha@example.com",
  "password": "Secure@123"
}
```

### Create Customer
`POST /api/customers`

Headers:
```http
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:
```json
{
  "name": "Jane Doe",
  "address": "123 Main St, Toronto, ON",
  "type": "PERSON"
}
```

### Update Customer
`PATCH /api/customers/{customerId}`

Request:
```json
{
  "name": "Jane Smith",
  "address": "456 New St, Toronto, ON"
}
```

### Get Customer
`GET /api/customers/{customerId}`

## Postman / API automation
This project includes:

- `postman/Group1-Digital-Banking.postman_collection.json`
- `postman/Group1-Local.postman_environment.json`

The collection contains:
- register
- login
- create customer
- get customer
- update customer
- negative tests for duplicate user, invalid login, and immutable customer fields

The login request stores the access token automatically into a Postman variable.

## Test coverage note
I included starter unit/controller tests and JaCoCo wiring, but I could not execute Maven in this environment because Maven is not installed in the container. So the code is generated and structured for a normal Spring Boot project, but it is **not runtime-verified inside this chat session**.

## Suggested next step
After importing into STS / IntelliJ / VS Code, run:

```bash
mvn clean test
mvn spring-boot:run
```

Then import the Postman collection and environment and run the requests in order.
