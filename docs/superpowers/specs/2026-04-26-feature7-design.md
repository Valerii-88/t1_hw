# Feature7 Design

**Date:** 2026-04-26

## Goal

Add a payment-core service to the same repository as the existing product service as a separate Maven module, integrate the payment service with the product service via `RestTemplate`, expose product lookup through the payment service, implement payment execution with real balance debit in the product service, persist successful payments, and return clear client-facing errors for failures on either side.

## Branching Strategy

- Create `feature7` from `origin/pr-6`, not from `main`.
- Reason: feature 7 depends directly on the reviewed version of the product service from feature 6, including API versioning, mapper separation, and cleanup of obsolete demo code.
- Open the feature 7 pull request with base `pr-6`.
- After PR `#7` merges into `main`, retarget or rebase `feature7` onto `main`.

## Scope

In scope:

- Convert the repository into a multi-module Maven project.
- Extract the current product service into its own module.
- Add a new payment-service module with its own Spring Boot application.
- Add synchronous HTTP integration from payment-service to product-service via `RestTemplate`.
- Let clients request user products through payment-service.
- Let clients execute a payment through payment-service.
- Persist only successful payments in payment-service.
- Perform real product balance debit inside product-service.
- Return normalized business and integration errors to clients.

Out of scope:

- Distributed transactions, sagas, outbox, retries, or eventual consistency workflows.
- Storing failed payment attempts.
- Product reservation, hold, or reversal flows.
- Shared DTO/contracts module.
- UI, CLI, demo runners, or CI jobs that start long-running services.

## High-Level Architecture

The repository becomes a parent Maven project with two Spring Boot modules:

1. `product-service`
2. `payment-service`

`payment-service` orchestrates the payment flow. It never accesses product data directly and never uses product repositories/entities. Instead, it talks to `product-service` over HTTP through a dedicated client component based on `RestTemplate`.

`product-service` remains the system of record for products and balances. It exposes read endpoints and a new debit endpoint. All product validation and balance updates remain inside `product-service`.

`payment-service` becomes the system of record for successful payments. It stores payment history only after a successful debit in `product-service`.

## Why This Approach

This design is chosen to avoid predictable review comments based on previous PR feedback:

- API versioning remains explicit through `/api/v1`.
- Controllers stay thin.
- Mapping is handled by Spring-managed mapper beans, not static DTO helpers.
- Service-to-service HTTP calls are isolated in a client bean, not inside controllers.
- Settings live in configuration files, not hardcoded Java constants.
- No extra demo entry points or command runners are introduced for feature 7.
- Error translation is explicit instead of leaking downstream responses directly to clients.

## Repository and Module Layout

Root:

- `pom.xml` becomes a parent aggregator with `packaging` = `pom`.
- Modules:
  - `product-service`
  - `payment-service`

`product-service`:

- `pom.xml`
- `src/main/java/.../feature1/...`
- `src/main/java/.../feature2/...`
- `src/main/java/.../feature3/...`
- `src/main/java/.../feature4/...`
- `src/main/java/.../feature5/...`
- `src/main/java/.../feature6/...`
- `src/main/resources/application.yml`
- `src/main/resources/feature4.properties`
- `src/main/resources/db/migration/...`
- `src/test/java/.../feature1/...`
- `src/test/java/.../feature2/...`
- `src/test/java/.../feature3/...`
- `src/test/java/.../feature6/...`

`payment-service`:

- `pom.xml`
- `src/main/java/.../feature7/...`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/...`
- `src/test/java/.../feature7/...`

No shared Java module is added because that would likely overcomplicate a training task and trigger review comments about unnecessary abstractions.

For minimal churn, existing feature1-feature5 code and their tests stay in `product-service` during the module extraction. This keeps the current test suite intact and avoids unrelated refactoring.

## Product-Service Changes

### Existing Endpoints Kept

- `GET /api/v1/users/{userId}/products`
- `GET /api/v1/products/{productId}`

### New Endpoint

- `POST /api/v1/products/{productId}/debit`

Request body:

- `amount`

Response body:

- `id`
- `accountNumber`
- `balance`
- `productType`
- `userId`

### Debit Rules

`product-service` must:

- validate that `productId` is positive;
- validate that `amount` is positive;
- load the product;
- return `404` if the product is missing;
- return `422` if the balance is insufficient;
- subtract the amount from the product balance;
- save the updated product in one transaction;
- return the debit response.

### Product-Service Error Model

Business errors:

- `400 Bad Request` for invalid ids or amount.
- `404 Not Found` for missing product.
- `422 Unprocessable Entity` for insufficient funds.

Product-service keeps its own exception handler and adds a dedicated exception for insufficient funds.

## Payment-Service API

### Get Products Through Payment Service

- `GET /api/v1/users/{userId}/products`

Flow:

1. Validate `userId`.
2. Call product-service `GET /api/v1/users/{userId}/products`.
3. Map downstream DTOs to payment-service response DTOs.
4. Return the result to the client.

### Execute Payment

- `POST /api/v1/payments`

Request body:

- `userId`
- `productId`
- `amount`
- `description`

Response body:

- `paymentId`
- `userId`
- `productId`
- `amount`
- `description`
- `createdAt`

Only successful payments are stored and returned.

## Payment Execution Flow

1. Validate request fields in payment-service.
2. Request the product by `productId` from product-service.
3. Verify that the product belongs to `userId`.
4. Call product-service debit endpoint with `amount`.
5. If debit succeeds, persist the payment entity in payment-service.
6. Return the successful payment response.

If any step before persistence fails, payment-service returns an error and does not write a payment row.

## Payment-Service Data Model

Table: `payments`

- `id bigserial primary key`
- `user_id bigint not null`
- `product_id bigint not null`
- `amount numeric(19,2) not null`
- `description varchar(255) not null`
- `created_at timestamp not null`

Constraints:

- `amount > 0`

## Payment-Service Components

Main components:

- `PaymentServiceApplication`
- `PaymentController`
- `PaymentService`
- `PaymentRepository`
- `Payment`
- `PaymentMapper`
- `ProductClient`
- `RestTemplateConfig`
- `PaymentExceptionHandler`

DTOs:

- `PaymentRequest`
- `PaymentResponse`
- `PaymentProductResponse`
- `ProductClientProductResponse`
- `ProductDebitRequest`
- `ProductDebitResponse`

Exceptions:

- `PaymentValidationException`
- `ProductOwnershipException`
- `DownstreamProductServiceException`
- `DownstreamProductServiceUnavailableException`

## Configuration

All settings must live in `application.yml`.

`product-service`:

- datasource
- JPA
- Flyway
- dedicated Flyway history table
- server port

`payment-service`:

- datasource
- JPA
- Flyway
- dedicated Flyway history table
- server port
- product-service base URL
- RestTemplate timeouts

No hostnames, ports, URLs, or credentials are hardcoded in Java classes.

## Error Translation Strategy

Payment-service must not blindly proxy downstream HTTP errors. It normalizes them into stable client-facing responses.

Cases:

- invalid request in payment-service -> `400`
- product not found in product-service -> `404`
- product belongs to another user -> `409`
- insufficient funds from product-service -> `422`
- product-service unavailable / timeout / unexpected `5xx` -> `502`

The error body should explicitly indicate whether the failure originated in payment-service validation/business logic or while communicating with product-service.

## Consistency Model

This task uses a simple synchronous consistency model:

1. debit balance in product-service;
2. persist payment in payment-service.

If payment persistence fails after a successful debit, the systems may become temporarily inconsistent. This is accepted as an explicit limitation of the training task. No compensation or saga workflow will be added.

## Testing Strategy

### Product-Service Tests

- Unit tests for debit logic.
- Controller tests for the debit endpoint.
- Negative tests for:
  - invalid product id;
  - invalid amount;
  - missing product;
  - insufficient funds.

### Payment-Service Tests

- Unit tests for payment orchestration.
- Controller tests for:
  - product lookup through payment-service;
  - successful payment execution;
  - invalid request;
  - product owned by another user;
  - downstream `404`;
  - downstream insufficient funds;
  - downstream unavailability / `5xx`.

### Build Validation

- module-level tests for each service;
- root `mvn test` from the parent project;
- focused verification that feature 6 behavior still passes after module extraction.

## Expected Review-Sensitive Decisions

The implementation must preserve these standards to avoid likely review comments:

- Keep API versioning on both services.
- Keep controllers free from business orchestration.
- Use mapper beans for DTO conversion.
- Use a dedicated HTTP client component for `RestTemplate`.
- Keep settings in YAML/properties.
- Do not create feature 7 demo runners.
- Keep changes minimal outside affected modules.
- Add tests for business and integration-style failure paths, not only happy-path cases.

## Open Technical Decisions Already Resolved

- Branch base: `origin/pr-6`
- Separate Maven module: yes
- Product balance must be changed for successful payments: yes
- Failed payments persisted: no
- Payment history persisted: yes
- Cross-service communication style: synchronous HTTP with `RestTemplate`

## Definition of Done

Feature 7 is done when:

- the repo builds as a multi-module Maven project;
- product-service runs as its own module;
- payment-service runs as its own module;
- payment-service can fetch products through product-service;
- payment-service can execute a successful payment;
- product-service debits the real balance;
- payment-service stores the successful payment;
- clients receive stable error responses for local and downstream failures;
- automated tests cover the main success and failure scenarios.
