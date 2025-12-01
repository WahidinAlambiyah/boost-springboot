# Boost Spring Boot

Spring Boot starter implementing the requested landing behavior and backend capabilities. The project ships feature-first packages for user authentication, menu management, and ordering with ULID identifiers, JWT-in-cookie auth, Redis caching, Neo4j categories, and basic event sourcing.

## Endpoints
- `GET /user/auth/login` — username/password login that issues a JWT cookie.
- `POST /user/auth/register` — create a customer or admin account.
- `POST /menu` — create menu item.
- `PUT /menu/{id}` — update menu item.
- `DELETE /menu/{id}` — delete menu item.
- `GET /menu` — list all menu items.
- `POST /order` — create an order with items.
- `PUT /order/{id}` — update order items or status.
- `DELETE /order/{id}` — remove an order.
- `GET /order` — list all orders or filter by `userId`.

## Features
- Package-by-feature layout (user, menu, order, account, profile, transaction, category, event) aligned with MVC/DDD principles.
- DTOs for requests, entities/repositories for persistence, services for orchestration, controllers for HTTP endpoints.
- PostgreSQL via Spring Data JPA, Redis caching for orders, Neo4j graph node for categories, Flyway ready for migrations.
- JWT generation stored as HTTP-only cookie during login.
- ULID-based identifiers for predictable sortable IDs.
- Domain event log table for simple event sourcing and observability.

## Configuration
Set database/cache/graph credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/boost
    username: boost
    password: boost
  data:
    redis:
      host: localhost
      port: 6379
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: secret
jwt:
  secret: very-secret-key-change-me
  expiration-minutes: 120
```

## Running
```bash
mvn spring-boot:run
```

## Order Flow UI Hooks
- Login/register endpoints support pop-up driven flows.
- Middleware-friendly JWT cookie.
- Device/browser allowlists configured via `app.login` properties for validation messaging.
- Order confirmation and status fields (`ONGOING`, `DONE`, `CANCEL`) returned in API responses.

Generate the project, push to your Git host, and hook your UI against these endpoints to mirror the provided screen capture.
