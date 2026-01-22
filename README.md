# Go Users API

Go Users API adalah project contoh CRUD user dengan JWT auth berbasis Gin + GORM + PostgreSQL + Redis.

## Struktur Folder

```
cmd/api
internal
  auth
  cache
  config
  db
  middleware
  response
  user
  utils
migrations
docs
```

## Prasyarat

- Go 1.25.5
- Docker + Docker Compose
- (Opsional) golang-migrate CLI
- (Opsional) swag CLI

## Menjalankan Secara Lokal

1. Salin `.env.example` menjadi `.env` lalu sesuaikan nilainya.
2. Jalankan layanan dengan Docker Compose:

```bash
docker compose up -d
```

API akan berjalan di `http://localhost:8080`.

## Migrasi Database

Gunakan golang-migrate CLI:

```bash
make migrate-up
```

Untuk rollback:

```bash
make migrate-down
```

## Swagger

Generate dokumentasi Swagger:

```bash
make swag
```

Setelah itu akses UI di:

```
http://localhost:8080/swagger/index.html
```

## Contoh Curl

### Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"full_name":"Jane Doe","email":"jane@example.com","username":"jane","password":"secret123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"jane@example.com","password":"secret123"}'
```

### Akses Endpoint Protected

```bash
curl -X GET http://localhost:8080/api/v1/me \
  -H 'Authorization: Bearer <access_token>'
```

## Catatan

- Refresh token disimpan di Redis dengan key `refresh:<jti>`.
- Set `REDIS_ENABLED=false` jika ingin menonaktifkan Redis (refresh token disimpan in-memory).
- Access token berlaku 15 menit, refresh token 7 hari (configurable).
- Role `ADMIN` bisa CRUD semua user; `USER` hanya boleh akses profil sendiri.
