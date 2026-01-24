# Boost Springboot API (NestJS + TypeORM)

REST API CRUD untuk Users, Roles, Categories, dan Products menggunakan NestJS 11 + TypeORM + PostgreSQL + Redis cache (toggle via env).

## Requirements
- Node.js 24.x LTS
- NPM
- Docker + Docker Compose

## Step-by-step (create project + install deps + run)
```bash
# 1) Generate project
npx @nestjs/cli@11.0.16 new boost-springboot-api --package-manager npm --skip-git

# 2) Masuk folder project
cd boost-springboot-api

# 3) Install dependencies versi sesuai requirement
npm install \
  @nestjs/common@11.1.12 @nestjs/core@11.1.12 @nestjs/platform-express@11.1.12 \
  @nestjs/swagger@11.2.5 @nestjs/typeorm@11.0.0 @nestjs/config@4.0.2 \
  @nestjs/cache-manager@3.1.0 cache-manager@7.2.8 \
  @keyv/redis@^2.8.5 keyv@^5.2.3 cacheable@^1.7.6 \
  typeorm@0.3.28 pg@^8.12.0 class-validator@^0.14.1 class-transformer@^0.5.1 \
  swagger-ui-express@^5.0.1

# 4) Jalankan database + redis
cp .env.example .env
docker compose up -d
# jika sebelumnya sudah pernah up, reset agar init script dieksekusi:
# docker compose down -v && docker compose up -d

# 5) Jalankan aplikasi
npm run start:dev

# 6) Buka Swagger
# http://localhost:3000/docs
```

> Catatan: `synchronize: true` hanya untuk development. Untuk production gunakan migration.

## Environment Variables
Lihat `.env.example`:
- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`: koneksi PostgreSQL
- `DB_SCHEMA`: schema PostgreSQL yang digunakan (contoh: `fastworks`)
- `CACHE_ENABLED`: `true|false` untuk ON/OFF cache
- `REDIS_ENABLED`: `true|false` (jika cache ON tapi Redis OFF, fallback ke memory)
- `REDIS_URL`: URL Redis
- `CACHE_TTL_MS`: TTL cache dalam ms

## Catatan PostgreSQL UUID & Schema
- Project ini memakai UUID untuk primary key. Untuk PostgreSQL, fungsi UUID berasal dari extension.
- `docker-compose.yml` sudah menjalankan init script yang:
  - membuat schema `fastworks`
  - mengaktifkan extension `pgcrypto` (dipakai TypeORM lewat `uuidExtension: 'pgcrypto'`)
- Jika container Postgres sudah terlanjur dibuat sebelum init script ditambahkan, jalankan:
  - `docker compose down -v`
  - `docker compose up -d`

## Swagger
Swagger tersedia di `/docs`.

## CRUD Endpoints
- `GET /roles`, `POST /roles`, `GET /roles/:id`, `PATCH /roles/:id`, `DELETE /roles/:id`
- `GET /users`, `POST /users`, `GET /users/:id`, `PATCH /users/:id`, `DELETE /users/:id`
- `GET /categories`, `POST /categories`, `GET /categories/:id`, `PATCH /categories/:id`, `DELETE /categories/:id`
- `GET /products`, `POST /products`, `GET /products/:id`, `PATCH /products/:id`, `DELETE /products/:id`

## Example cURL (happy path)
```bash
# Create role
curl -X POST http://localhost:3000/roles \
  -H 'Content-Type: application/json' \
  -d '{"name":"admin"}'

# Create user (gunakan roleId dari hasil create role)
curl -X POST http://localhost:3000/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","fullName":"Jane Doe","passwordHash":"hashed-placeholder","roleIds":["<roleId>"]}'

# Create category
curl -X POST http://localhost:3000/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"Electronics"}'

# Create product (gunakan categoryId dari hasil create category)
curl -X POST http://localhost:3000/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Laptop","price":999.99,"categoryId":"<categoryId>"}'
```
