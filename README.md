# Go Users API

Go Users API adalah project contoh CRUD user dengan JWT auth berbasis Gin + GORM + PostgreSQL + Redis.

## Struktur Folder

```
cmd/api
internal
  auth
  cache
  category
  config
  db
  middleware
  product
  response
  role
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

### Akses Endpoint

```bash
curl -X GET http://localhost:8080/api/v1/users
```

### CRUD Roles

```bash
curl -X POST http://localhost:8080/api/v1/roles \
  -H 'Content-Type: application/json' \
  -d '{"name":"ADMIN","description":"Administrator"}'
```

### CRUD Categories

```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H 'Authorization: Bearer <access_token>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Electronics","slug":"electronics","description":"Kategori elektronik"}'
```

```bash
curl -X GET http://localhost:8080/api/v1/categories
```

### CRUD Products

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H 'Authorization: Bearer <access_token>' \
  -H 'Content-Type: application/json' \
  -d '{"category_id":"<category_uuid>","name":"iPhone 15","sku":"IPHONE15","price":19999.00,"stock":10}'
```

```bash
curl -X GET 'http://localhost:8080/api/v1/products?search=iphone&sort=-price&page=1&size=10'
```

## Catatan

- Refresh token disimpan di Redis dengan key `refresh:<jti>`.
- Set `REDIS_ENABLED=false` jika ingin menonaktifkan Redis (refresh token disimpan in-memory).
- Set `DB_SCHEMA` untuk menggunakan schema Postgres custom (default: `public`).
- Access token berlaku 15 menit, refresh token 7 hari (configurable).
- Saat ini semua endpoint user dapat diakses tanpa otorisasi.
