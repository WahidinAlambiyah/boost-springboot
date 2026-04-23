# Boost Spring Boot RBAC API

**Commercial / Proprietary (Not Open Source)** — this repository is licensed per end client only.

## License Summary (Commercial, Per Client)
- **Allowed**: Use and modify the Software for **one (1) end client** per license purchase.
- **Allowed**: Deploy internally for the permitted client and make backups.
- **Not allowed**: Redistribute, publish, sublicense, resell, or provide the Software to other clients.
- **Not allowed**: Use as a template/boilerplate for multiple clients or productize it for resale.

See [LICENSE](LICENSE) and [TERMS.md](TERMS.md). Indonesian summaries are in [LICENSE_ID.md](LICENSE_ID.md) and [TERMS_ID.md](TERMS_ID.md).

## Do Not Commit Secrets
Keep secrets out of git. Use `.env.example` as a template and store real secrets in `.env`, which is already ignored by `.gitignore`.

If `.env` was previously committed, remove it from the repo history with:
```bash
git rm --cached .env
git commit -m "chore: stop tracking .env"
```
Then rotate any exposed secrets.

## Third-Party Licenses
Third-party dependency notices and the update checklist live in [LICENSES/THIRD_PARTY.md](LICENSES/THIRD_PARTY.md).

## Password Reset (Email OTP / Token)
Password reset supports OTP or token link modes.

### Request reset
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","mode":"OTP"}'
```

### Resend OTP
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/resend \
  -H "Content-Type: application/json" \
  -d '{"resetRequestId":"<uuid>"}'
```

### Confirm with OTP
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{"resetRequestId":"<uuid>","otp":"123456","newPassword":"NewPassword123!"}'
```

### Confirm with token
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{"token":"<token>","newPassword":"NewPassword123!"}'
```

### Email config
Set the SMTP password in the environment:
```
MAIL_PASSWORD=your-smtp-password
```

## Contact
For licensing inquiries: <CONTACT_EMAIL>

API backend Java 21 + Spring Boot 3.x dengan JWT, RBAC berbasis database, PostgreSQL, Redis (opsional), Liquibase, JPA, Validation, dan OpenAPI/Swagger.

## Prasyarat

- Java 21 (LTS)
- Maven 3.9+
- Docker + Docker Compose

## Menjalankan Infrastruktur (PostgreSQL + Redis)

```bash
docker compose --env-file .env.example up -d
```

> Redis opsional. Jika `REDIS_ENABLED=false`, aplikasi memakai in-memory store untuk refresh token/blacklist (tidak persisten).

## Menjalankan Aplikasi

```bash
mvn spring-boot:run
```

Aplikasi berjalan di `http://localhost:8080`.

## Observability (Logging, Metrics, Tracing)

### JSON Logging + Correlation ID
Log output menggunakan format JSON dengan field MDC `correlationId`. Setiap request akan membawa header `X-Correlation-Id`. Jika header tidak dikirim, server akan membuat UUID baru dan selalu mengembalikan header tersebut di response.

### Metrics & Health (Actuator)
Endpoint yang diekspos:
- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

### Tracing (Optional)
Gunakan salah satu opsi berikut:
- **Option A: OpenTelemetry Java Agent**  
  Jalankan aplikasi dengan agent OpenTelemetry:
  ```bash
  java -javaagent:/path/to/opentelemetry-javaagent.jar \
    -Dotel.service.name=boost \
    -jar target/boost-0.0.1-SNAPSHOT.jar
  ```
- **Option B: Micrometer Tracing Bridge**  
  Tambahkan dependency Micrometer tracing bridge yang kompatibel dengan Spring Boot 3.x (misalnya `micrometer-tracing-bridge-otel`) lalu konfigurasi exporter sesuai kebutuhan.

## Menjalankan Test

```bash
mvn test
```

## Konfigurasi `.env`

Contoh konfigurasi ada di `.env.example`.

```ini
DB_NAME=boost
DB_USER=boost
DB_PASSWORD=boost
DB_SCHEMA=fastworks_springboot
DB_HOST=localhost
DB_PORT=5432
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_ENABLED=false
SECURITY_ENABLED=true
METHOD_SECURITY_ENABLED=true
JWT_SECRET=change-me-please-change-me-please-change-me
```

## Database Schema

- Schema: `fastworks_springboot`
- Liquibase otomatis menjalankan migrasi saat startup

Jika kamu ingin membuat schema manual:

```sql
CREATE SCHEMA IF NOT EXISTS fastworks_springboot;
```

## Postgres URL (Host vs Container)

- **Aplikasi berjalan di host** (default):
  `jdbc:postgresql://localhost:5432/boost?currentSchema=fastworks_springboot`
- **Aplikasi berjalan di container** (misal via Dockerfile):
  `jdbc:postgresql://postgres:5432/boost?currentSchema=fastworks_springboot`

## Swagger UI

`http://localhost:8080/swagger-ui.html`

## Seed Default Admin

Liquibase menyiapkan user admin default:

- **Username:** `admin`
- **Password:** `Admin123!`

Silakan ubah via:
- Update Liquibase seed (`db/changelog/changes/007-seed-default-rbac.yaml`), atau
- Create user baru via endpoint `/api/users`.

## Toggle Security & Redis

- Nonaktifkan security:
  - `SECURITY_ENABLED=false` → semua endpoint `permitAll()`
- Nonaktifkan method security:
  - `METHOD_SECURITY_ENABLED=false` → `@PreAuthorize` tidak dievaluasi
- Nonaktifkan Redis:
  - `REDIS_ENABLED=false` → refresh token/blacklist tersimpan in-memory (non-persisten)

## Daftar Endpoint Utama

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### Users (permission-based)
- `GET /api/users` → `USER_READ`
- `GET /api/users/{id}` → `USER_READ`
- `POST /api/users` → `USER_WRITE` + `ROLE_ADMIN`
- `PUT /api/users/{id}` → `USER_WRITE` + `ROLE_ADMIN`
- `DELETE /api/users/{id}` → `USER_DELETE`
- `PUT /api/users/{id}/roles` → `USER_WRITE` + `ROLE_ADMIN`

### Roles
- `GET /api/roles` → `ROLE_READ`
- `GET /api/roles/{id}` → `ROLE_READ`
- `POST /api/roles` → `ROLE_WRITE`
- `PUT /api/roles/{id}` → `ROLE_WRITE`
- `DELETE /api/roles/{id}` → `ROLE_DELETE`
- `PUT /api/roles/{id}/permissions` → `ROLE_WRITE`

### Permissions
- `GET /api/permissions` → `PERMISSION_READ`
- `GET /api/permissions/{id}` → `PERMISSION_READ`
- `POST /api/permissions` → `PERMISSION_WRITE`
- `PUT /api/permissions/{id}` → `PERMISSION_WRITE`
- `DELETE /api/permissions/{id}` → `PERMISSION_DELETE`

## RBAC Defense-in-Depth

Operasi sensitif RBAC sekarang diamankan di **dua lapis**:
- **Controller layer** dengan `@PreAuthorize` untuk proteksi endpoint HTTP.
- **Service layer** dengan `@PreAuthorize` yang konsisten dengan authority controller, sehingga rule RBAC tetap enforced walaupun method service dipanggil dari jalur selain controller.

Catatan: proteksi service layer method-security hanya aktif jika `METHOD_SECURITY_ENABLED=true`.

## Contoh Request/Response

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"Password123!"}'
```

Response:

```json
{
  "status": 201,
  "message": "Registered",
  "data": {
    "accessToken": "<token>",
    "refreshToken": "<token>",
    "tokenType": "Bearer"
  }
}
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}'
```

### Contoh CRUD Role (Create)

```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"code":"REPORT_VIEWER","name":"Report Viewer","description":"View reports"}'
```

Response:

```json
{
  "status": 201,
  "message": "Role created",
  "data": {
    "id": "<uuid>",
    "code": "REPORT_VIEWER",
    "name": "Report Viewer",
    "permissions": []
  }
}
```
