# Frontend Quickstart (Minimal)

## 1) Prerequisite
- **Node.js**: rekomendasi **v20 LTS**.
- **npm install**: jalankan `npm ci` (lockfile-based, konsisten untuk CI/local).

```bash
cd frontend
node -v
npm ci
```

## 2) Environment variables wajib
Gunakan `.env.local` (copy dari `.env.example`) dan pastikan minimal variabel ini terisi:

- `NEXT_PUBLIC_API_URL` **(wajib)** → base URL backend (contoh: `http://localhost:8080` atau host dev API).

Variabel lain yang dipakai di repo:
- `NEXT_PUBLIC_APP_ENV` (development/staging/production).
- `NEXT_PUBLIC_RUNTIME_LOG_ENDPOINT` (opsional untuk runtime error logging).

Contoh cepat:

```bash
cp .env.example .env.local
```

## 3) Scripts utama
Jalankan dari folder `frontend/`.

- `npm run dev` → jalankan Next.js dev server.
- `npm run lint` → lint dengan ESLint.
- `npm run typecheck` → cek TypeScript (`tsc --noEmit`).
- `npm run test` → unit/integration test (Vitest).
- `npm run test:e2e` → E2E Playwright (auth smoke).

## 4) Auth flow singkat
1. User login di `/login`.
2. Frontend call `POST /api/auth/login`, simpan `accessToken` + `refreshToken`.
3. Frontend call `GET /api/users/me` untuk hydrate user profile + permissions.
4. Semua request API bawa `Authorization: Bearer <accessToken>`.
5. Jika response `401`, interceptor akan coba `POST /api/auth/refresh` sekali lalu retry request awal.
6. Jika refresh gagal, sesi di-clear dan user diarahkan lagi ke `/login`.
7. Logout memanggil `POST /api/auth/logout` lalu clear token lokal.

## 5) Route & permission guard
### Public routes
- `/login`
- `/forbidden`

### Protected routes (butuh sesi valid)
- `/dashboard` (home redirect ke sini)
- `/catalog` → `CLASS_READ` **atau** `CLASS_WRITE`
- `/scheduling` → salah satu dari: `SCHEDULE_READ`, `SCHEDULE_WRITE`, `SESSION_READ`, `SESSION_WRITE`, `SESSION_RESCHEDULE`
- `/enrollment` → `ENROLLMENT_READ` **atau** `ENROLLMENT_WRITE`
- `/attendance` → `ATTENDANCE_READ` **atau** `ATTENDANCE_MARK`
- `/billing` → `BILLING_READ` **atau** `BILLING_WRITE`
- `/notification` → `NOTIFICATION_READ` **atau** `NOTIFICATION_WRITE`
- `/admin` → `ROLE_ADMIN` **atau** bundle penuh: `USER_WRITE` + `ROLE_WRITE` + `PERMISSION_WRITE`

Jika tidak lolos guard, user di-redirect ke `/forbidden`.

## 6) Troubleshooting umum
### A) 401 loop (login sukses tapi balik login lagi)
Checklist:
- Pastikan `NEXT_PUBLIC_API_URL` benar (host/port/protocol tepat).
- Cek endpoint refresh hidup: `POST /api/auth/refresh`.
- Cek respons refresh benar-benar mengembalikan token baru.
- Hapus storage browser (`accessToken`, `refreshToken*`) lalu login ulang.
- Pastikan backend clock/time sinkron (token expiry sensitif waktu).

### B) CORS error di browser
Checklist backend:
- Origin frontend (`http://localhost:3000`, dll) harus masuk ke CORS `allowedOrigins` backend.
- Method/header yang dipakai (`Authorization`, `Content-Type`) harus diizinkan.
- Jika pakai credentials/cookie mode, `allowCredentials` dan origin tidak boleh wildcard sembarang.

### C) Env mismatch (lokal oke, staging/prod gagal)
Checklist:
- Verifikasi nilai `NEXT_PUBLIC_API_URL` di environment target (bukan nilai lokal lama).
- Pastikan `NEXT_PUBLIC_APP_ENV` sesuai environment deployment.
- Rebuild/redeploy setelah ubah env (Next.js membaca env saat build/start, bukan selalu realtime).
