# Work Order Management (Spring Boot)

A simple Work Order Management backend built with Java 17, Spring Boot 3, and PostgreSQL. The API supports creating divisions and projects with access limited by user role headers (admin, manager, staff).

## Requirements
- Java 17
- Maven
- PostgreSQL (for local runtime)

## Running locally
1. Configure your PostgreSQL connection in `src/main/resources/application.yml` or export standard Spring datasource environment variables.
2. Start the application:
   ```bash
   mvn spring-boot:run
   ```
3. Open API documentation is available at `http://localhost:8080/swagger-ui.html` after the app starts.

## API usage
All protected endpoints expect an `X-ROLE` header with one of `ADMIN`, `MANAGER`, or `STAFF`. Managers and staff should also send `X-DIVISION-ID` to scope their access.

Key endpoints:
- `POST /api/divisions` (ADMIN) – create division.
- `GET /api/divisions` – list divisions.
- `POST /api/projects` – create project under a division.
- `GET /api/projects/division/{divisionId}` – list projects per division.
- `GET /api/projects/{id}` – project detail with division info.
- `PUT /api/projects/{id}` – update project.
- `DELETE /api/projects/{id}` – delete project.
- `POST /api/projects/{id}/resume` – reopen project if previously closed.
- `POST /api/accesses` (ADMIN) – create an access/permission code.
- `GET /api/accesses` – list access definitions.
- `POST /api/roles` (ADMIN) – create role with access assignments.
- `GET /api/roles` – list roles and their accesses.
- `POST /api/users` (ADMIN) – create a user with roles (and optional division).
- `GET /api/users` – list users with their roles and access mappings.

## Testing
An in-memory H2 database (PostgreSQL compatibility mode) is configured for tests. Run:
```bash
mvn test
```
