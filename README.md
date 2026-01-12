# Quarkus User Auth (JWT + Refresh Token)

Production-ready starter for authentication and user management using Quarkus 3.30.6, JDK 25, PostgreSQL, and Redis.

## 1) Bootstrap Command (Chosen)

**Command:**
```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.30.6:create \
  -DprojectGroupId=com.alambiyah \
  -DprojectArtifactId=quarkus-user-auth \
  -Dextensions="rest,rest-jackson,hibernate-orm-panache,jdbc-postgresql,hibernate-validator,smallrye-jwt,redis-client,liquibase,smallrye-openapi"
```

**Reason:** the Maven plugin guarantees the exact Quarkus version (3.30.6) and generates `mvnw` for consistent builds across environments.

## 2) Project Structure

```
.
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── .env.example
└── src
    ├── main
    │   ├── java
    │   │   └── com/alambiyah/userauth
    │   │       ├── application
    │   │       │   └── service
    │   │       ├── domain
    │   │       │   ├── entity
    │   │       │   └── model
    │   │       ├── dto
    │   │       ├── framework
    │   │       │   ├── cache
    │   │       │   ├── exception
    │   │       │   ├── mapper
    │   │       │   ├── persistence
    │   │       │   ├── security
    │   │       │   └── startup
    │   │       └── interfaces
    │   │           └── api
    │   └── resources
    │       ├── application.properties
    │       └── db/changelog/db.changelog-master.xml
    └── test
        ├── java/com/alambiyah/userauth/AuthResourceTest.java
        └── resources/application.properties
```

## 3) Prerequisites

- **JDK 25 (LTS)**
- **Docker** (for PostgreSQL + Redis and Dev Services in tests)

## 4) Run Locally

### Step 1: Start PostgreSQL + Redis
```bash
docker compose --env-file .env.example up -d
```

### Step 2: Run Quarkus in Dev Mode
```bash
./mvnw quarkus:dev
```

The app will be available at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui`
OpenAPI: `http://localhost:8080/openapi`

## 5) Example cURL

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"user1",
    "email":"user1@example.com",
    "fullName":"User One",
    "password":"User12345!"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail":"user1",
    "password":"User12345!"
  }'
```

### /api/me
```bash
curl -X GET http://localhost:8080/api/me \
  -H "Authorization: Bearer <accessToken>"
```

### Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken":"<refreshToken>"
  }'
```

### Admin: List Users
Seed admin account via env (`ADMIN_SEED_ENABLED=true`) or update `.env.example` and restart.

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <adminAccessToken>"
```

## 6) JWT & Refresh Token Design

- **Access token TTL:** 15 minutes
- **Refresh token TTL:** 7 days
- **JWT claims:** `sub` (userId), `preferred_username` (username), `groups` (roles), `exp`
- **Refresh token:** opaque random token, SHA-256 hashed before storing in Redis.
- **Rotation:** refresh consumes the existing token, issues a new refresh token, and deletes the old one.

## 7) Security Notes

- Passwords are hashed using **BCrypt** (`BcryptUtil`).
- Do **not** expose refresh tokens in query string; use request body only.
- Use **HTTPS** in production to protect tokens.
- Store secrets via environment variables (e.g., `JWT_SECRET`, DB credentials).

## 8) Testing

Tests use Quarkus Dev Services (requires Docker):
```bash
./mvnw test
```

## 9) Configuration Highlights

- PostgreSQL via `quarkus.datasource.*`
- Liquibase auto-migrate on startup
- Redis via `quarkus.redis.hosts`
- Swagger UI enabled in dev
- Admin seeding controlled via env variables

---

If you want additional features (pagination, rate limiting, audit logging), just ask.
