# Boost Spring Boot API

A Spring Boot 3 backend for managing users, items, and purchase orders. The
service exposes JWT-protected REST endpoints with validation, auditing,
pagination, bulk operations, soft deletion, and consistent error handling.

## Table of contents
1. [Tech stack](#tech-stack)
2. [Prerequisites](#prerequisites)
3. [Database setup](#database-setup)
4. [Running the application](#running-the-application)
5. [Running tests & coverage](#running-tests--coverage)
6. [Authentication](#authentication)
7. [API reference](#api-reference)
8. [Validation rules](#validation-rules)
9. [Auditing & soft deletion](#auditing--soft-deletion)
10. [Error responses](#error-responses)
11. [Database migrations](#database-migrations)
12. [Troubleshooting](#troubleshooting)

## Tech stack
- Java 17
- Spring Boot 3 (Web, Validation, Security, Data JPA)
- PostgreSQL 15
- Liquibase for schema migrations
- Maven for dependency management and builds
- JUnit 5 & Mockito for unit testing
- JaCoCo for code coverage reporting

## Prerequisites
- Java 17 SDK
- Maven 3.9+
- Docker (optional, for running PostgreSQL locally)

## Database setup
The application connects to PostgreSQL at `localhost:5432` using the database
`wahidin_purchaseorder` with credentials `wahidin_purchaseorder/wahidin_purchaseorder`. Adjust the
properties in `src/main/resources/application.yml` if required.

For local development a Docker Compose file is available.

```bash
docker compose -f docker-compose.postgres.yml up -d
```

This starts a container named `wahidin_purchaseorder_postgres` that exposes PostgreSQL
on port `5432`, seeds the default credentials, and persists data inside a named
volume (`wahidin_purchaseorder_postgres_data`). If you previously ran the hyphen-
based configuration, bring the stack down with `--volumes` to drop the old data
and allow PostgreSQL to initialize the new role.

### Configuration overrides
Create a `.env` file in the project root to override any Compose variable:

| Variable | Description | Default |
| --- | --- | --- |
| `CONTAINER_NAME` | Docker container name | `wahidin_purchaseorder_postgres` |
| `POSTGRES_DB` | Database name | `wahidin_purchaseorder` |
| `POSTGRES_USER` | Database username | `wahidin_purchaseorder` |
| `POSTGRES_PASSWORD` | Database password | `wahidin_purchaseorder` |
| `POSTGRES_PORT` | Host port for PostgreSQL | `5432` |
| `POSTGRES_IMAGE` | Docker image to use | `postgres:15-alpine` |

Example `.env`:

```dotenv
POSTGRES_PASSWORD=supersecret
POSTGRES_PORT=55432
```

Stop the database with `docker compose -f docker-compose.postgres.yml down`.
Add `--volumes` to also remove stored data.

## Running the application
With PostgreSQL available, launch the API:

```bash
mvn spring-boot:run
```

The service listens on `http://localhost:8080`.

## Running tests & coverage
Execute the full unit test suite:

```bash
mvn test
```

To generate a JaCoCo report (with an 70% minimum line coverage gate) run:

```bash
mvn verify
```

Open `target/site/jacoco/index.html` to inspect coverage metrics.

## Authentication
All business endpoints require a JSON Web Token. Authenticate with the seeded
administrator account (`admin@example.com` / `SuperSecret123!`) to obtain a
token.

```bash
curl --request POST \
  --url http://localhost:8080/api/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
    "email": "admin@example.com",
    "password": "SuperSecret123!"
  }'
```

Include the returned `token` in the `Authorization` header as `Bearer <token>`
for subsequent requests.

## API reference
All list endpoints accept Spring's standard paging parameters: `page` (0-based),
`size`, and `sort` (e.g. `sort=lastName,asc`). Unless noted, responses use DTOs
that omit sensitive fields such as passwords and auditing metadata.

### Users
| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/users` | Page through active users. |
| `GET` | `/api/users/{id}` | Fetch a single active user. |
| `POST` | `/api/users` | Create a user. |
| `POST` | `/api/users/bulk` | Create multiple users in one request. |
| `PUT` | `/api/users/{id}` | Replace all mutable fields for a user. |
| `PATCH` | `/api/users/{id}` | Partially update user fields. |
| `DELETE` | `/api/users/{id}` | Soft-delete a user (marks `deleted=true`). |
| `DELETE` | `/api/users/{id}/permanent` | Permanently remove the record. |

### Items
| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/items` | Page through active items. |
| `GET` | `/api/items/{id}` | Fetch a single active item. |
| `POST` | `/api/items` | Create an item. |
| `POST` | `/api/items/bulk` | Bulk-create items. |
| `PUT` | `/api/items/{id}` | Replace mutable item fields. |
| `PATCH` | `/api/items/{id}` | Partially update item data. |
| `DELETE` | `/api/items/{id}` | Soft-delete an item. |
| `DELETE` | `/api/items/{id}/permanent` | Permanently delete an item. |

### Purchase orders
| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/purchase-orders` | Page through purchase order headers (active only). |
| `GET` | `/api/purchase-orders/{id}` | Fetch a purchase order with details. |
| `POST` | `/api/purchase-orders` | Create a purchase order with header & detail lines. |
| `POST` | `/api/purchase-orders/bulk` | Bulk-create purchase orders. |
| `PUT` | `/api/purchase-orders/{id}` | Replace header and details. |
| `PATCH` | `/api/purchase-orders/{id}` | Partially update header or detail data. |
| `DELETE` | `/api/purchase-orders/{id}` | Soft-delete a purchase order. |
| `DELETE` | `/api/purchase-orders/{id}/permanent` | Permanently remove a purchase order. |

### Common request/response shapes
- **Requests:** `UserRequest`, `ItemRequest`, and `PurchaseOrderRequest` accept
  business data only; auditing fields (`createdBy`, `createdDate`, `updatedBy`,
  `updatedDate`) are not present because the backend sets them automatically.
- **Responses:** `UserResponse`, `ItemResponse`, and
  `PurchaseOrderResponse` return sanitized data that excludes passwords and
  other sensitive properties while supplying identifiers, totals, and auditing
  metadata for display.

### Bulk endpoints
Bulk endpoints expect a JSON array. Validation runs per element, and the
request fails atomically if any entry violates constraints. Example:

```json
[
  {
    "firstName": "Test",
    "lastName": "User",
    "email": "test.user@example.com",
    "phone": "+6281234567890",
    "password": "Str0ngPass!"
  }
]
```

### Partial updates (PATCH)
Patch endpoints accept dedicated `*PatchRequest` DTOs where all fields are
optional. Only provided values overwrite existing data. For users, supplying a
new password triggers re-encoding before persistence.

### Soft vs. permanent delete
Calling `DELETE /api/.../{id}` marks the record as deleted, removes it from
subsequent queries, and blocks authentication for soft-deleted users. To remove
rows from the database entirely, use the `/permanent` endpoints.

## Validation rules
Incoming payloads are validated using Jakarta Bean Validation. Highlights:
- Email fields must be valid addresses.
- Phone numbers must contain 8–15 digits and may start with `+`.
- Required identity fields (e.g., first/last name, item name) cannot be blank.
- Purchase order details must include positive quantities and prices.

Invalid payloads return a `400 Bad Request` response with detailed validation
messages (see [Error responses](#error-responses)).

## Auditing & soft deletion
`AuditUtils` injects auditing metadata during create/update operations, so
clients do not send those fields. Entities include a `deleted` flag that is set
by soft deletes and respected by repository queries and authentication logic.

## Error responses
Errors use a consistent JSON contract:

```json
{
  "timestamp": "2023-11-02T12:34:56.789Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Constraint violations found",
  "path": "/api/users/bulk",
  "validationErrors": {
    "[0].email": "Email must be valid."
  }
}
```

`validationErrors` is present when payload validation fails and maps field paths
(to array indices for bulk requests) to messages.

## Database migrations
Schema changes are managed by Liquibase (`src/main/resources/db/changelog`). A
recent change set adds `deleted` columns to core tables to support soft deletion.
Liquibase runs automatically at application startup.

## Troubleshooting
- **Tests fail with `WeakKeyException`:** Ensure HS256 secrets are at least 256
  bits. The `JwtServiceTest` demonstrates generating a compliant key.
- **Maven cannot download dependencies:** Verify internet connectivity or use a
  local Maven repository mirror.
- **Database connection errors:** Confirm PostgreSQL is running and credentials
  match your environment.

