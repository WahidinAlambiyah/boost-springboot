# Boost Spring Boot API

Production-ready Spring Boot REST API with JWT authentication, PostgreSQL, Redis, Liquibase, and Swagger UI.

## Prerequisites

- Java 21 (latest LTS)
- Maven 3.9+
- Docker + Docker Compose

## Run infrastructure (PostgreSQL + Redis)

```bash
docker compose --env-file .env.example up -d
```

## Run the application

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

## Swagger UI

`http://localhost:8080/swagger-ui.html`

## Health check

`GET http://localhost:8080/actuator/health`

## Sample curl commands

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"Password123!","fullName":"Demo User"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"Password123!"}'
```

### Get current user profile

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <accessToken>"
```

### Admin: list users

```bash
curl http://localhost:8080/api/users?page=0&size=10 \
  -H "Authorization: Bearer <accessToken>"
```

### Admin: create user

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin2","email":"admin2@example.com","password":"Password123!","fullName":"Admin Two","roles":["ADMIN"],"active":true}'
```

### Admin: update user

```bash
curl -X PUT http://localhost:8080/api/users/<userId> \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Updated Name","roles":["USER"]}'
```

### Admin: delete user (soft delete)

```bash
curl -X DELETE http://localhost:8080/api/users/<userId> \
  -H "Authorization: Bearer <accessToken>"
```

## Schema usage (protoone)

- The application uses schema `protoone` (not `public`).
- PostgreSQL search path is configured with `currentSchema=protoone,public` in `application.yml`.
- Liquibase is configured to create and manage objects in the `protoone` schema.

### Create schema manually (if needed)

```sql
CREATE SCHEMA IF NOT EXISTS protoone;
```

## JWT choice

This project uses HS256 with a shared secret for simplicity in single-service deployments. Update `JWT_SECRET` to a 32+ byte value before production use.
