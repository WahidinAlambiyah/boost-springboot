# Boost Spring Boot Starter

Template Spring Boot 3.3 (Java 21) untuk prototyping maupun baseline enterprise dengan integrasi Postgres, Liquibase, JWT, serta konfigurasi opsional Redis dan Kafka.

## Menjalankan layanan pendukung
Gunakan Docker Compose untuk database, Redis, dan Kafka:

```bash
docker compose up -d
```

Nilai koneksi default di `application.yml` mengarah ke service pada compose (Postgres di `localhost:5432`, Redis `6379`, Kafka `9092`).

## Konfigurasi aplikasi
- `app.jwt.secret` dan `app.jwt.expiration-ms` mengontrol token JWT.
- `app.redis.enabled` dan `app.kafka.enabled` dapat diubah ke `true` untuk mengaktifkan konfigurasi Redis atau Kafka ketika service tersedia.
- Liquibase dijalankan otomatis saat startup menggunakan changelog `db/changelog/db.changelog-master.yaml` yang mencakup tabel `users`, `roles`, serta data awal admin.

## Endpoint utama
- `POST /api/auth/register` — registrasi user baru.
- `POST /api/auth/login` — login dan memperoleh JWT. Akun awal: `admin` / `password`.
- `GET /api/users` — daftar user (memerlukan autentikasi).
- `GET /api/users/{id}` — detail user.
- `POST /api/users` — buat user baru (ADMIN).
- `PUT /api/users/{id}` — perbarui user (ADMIN).
- `DELETE /api/users/{id}` — hapus user (ADMIN).
- `POST /api/users/{id}/roles` — atur ulang role user, membutuhkan peran `ROLE_ADMIN`.
- `GET /api/roles` — daftar role (ADMIN).
- `GET /api/roles/{id}` — detail role (ADMIN).
- `POST /api/roles` — buat role baru (ADMIN).
- `PUT /api/roles/{id}` — perbarui role (ADMIN).
- `DELETE /api/roles/{id}` — hapus role (ADMIN).

## Best practice untuk prototipe dan enterprise
- **Pemisahan konfigurasi:** gunakan profil dan flag `app.redis.enabled`/`app.kafka.enabled` untuk mengaktifkan komponen tambahan hanya saat diperlukan.
- **Migrations first:** jangan gunakan `ddl-auto` selain `validate`; tambahkan perubahan skema ke Liquibase agar mudah dilacak.
- **JWT security:** simpan secret di secret manager/variable environment dan rotasi secara berkala; batasi masa berlaku token.
- **Package by layer:** struktur `controller`, `service`, `repository`, `domain`, `security`, dan `config` memudahkan refactor dan pengamanan boundary.
- **Testing & linting:** tambahkan uji integrasi untuk endpoint autentikasi serta kontrak repository; gunakan testcontainers untuk Postgres/Kafka di pipeline.
- **Observability:** tambahkan Actuator, logging terstruktur, dan traceId pada header untuk produksi.
- **Hardening data:** aktifkan TLS untuk koneksi database/Kafka/Redis di lingkungan produksi dan gunakan kredensial terpisah per lingkungan.
- **Async & messaging:** untuk prototipe Kafka, buat topic melalui config atau skrip bootstrap; bungkus producer/consumer dengan interface service agar mudah diganti atau di-mock.

## Menjalankan aplikasi
```bash
mvn spring-boot:run
```
Pastikan variabel lingkungan database/JWT secret telah di-set jika berbeda dari nilai default.

## Dokumentasi API
Springdoc OpenAPI tersedia di lingkungan dev untuk memudahkan eksplorasi dan testing:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
