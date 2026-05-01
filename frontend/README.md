# Frontend Quickstart + QA/UAT Notes

Dokumen ini merangkum cara menjalankan FE secara lokal, route aktif FE-1, mapping permission read/write per modul, dan catatan fallback endpoint students.

## 1) Jalankan lokal (wajib untuk QA/UAT/onboarding)

```bash
cd frontend
node -v
npm ci
cp .env.example .env.local
npm run dev
```

Akses aplikasi di: `http://localhost:3000`

## 2) Environment variable wajib

Gunakan file `.env.local` (copy dari `.env.example`). Minimal variabel berikut **harus** terisi:

- `NEXT_PUBLIC_API_URL` **(wajib)** → base URL backend, contoh: `http://localhost:8080`.

Variabel yang umum dipakai:

- `NEXT_PUBLIC_APP_ENV` → `development | staging | production`.
- `NEXT_PUBLIC_RUNTIME_LOG_ENDPOINT` → opsional untuk runtime error logging.

## 3) Scripts utama

Jalankan dari folder `frontend/`.

- `npm run dev` → jalankan Next.js dev server.
- `npm run lint` → lint dengan ESLint.
- `npm run typecheck` → cek TypeScript (`tsc --noEmit`).
- `npm run test` → unit/integration test (Vitest).
- `npm run test:e2e` → E2E Playwright (auth smoke).

## 4) Daftar route FE-1

### Public routes
- `/login`
- `/forbidden`

### Protected routes (wajib sesi valid + lolos permission guard)
- `/dashboard` (home redirect ke sini)
- `/catalog`
- `/scheduling`
- `/enrollment`
- `/attendance`
- `/billing`
- `/notification`
- `/admin`

Jika tidak lolos guard, user akan di-redirect ke `/forbidden`.

## 5) Permission mapping read/write per modul

| Modul | Permission Read | Permission Write | Catatan Guard Route |
|---|---|---|---|
| Catalog | `CLASS_READ` | `CLASS_WRITE` | `/catalog` butuh salah satu read/write |
| Scheduling | `SCHEDULE_READ`, `SESSION_READ` | `SCHEDULE_WRITE`, `SESSION_WRITE`, `SESSION_RESCHEDULE` | `/scheduling` butuh salah satu permission scheduling/session |
| Enrollment | `ENROLLMENT_READ` | `ENROLLMENT_WRITE` | `/enrollment` butuh salah satu read/write |
| Attendance | `ATTENDANCE_READ` | `ATTENDANCE_MARK` | `/attendance` butuh salah satu read/write |
| Billing | `BILLING_READ` | `BILLING_WRITE` | `/billing` butuh salah satu read/write |
| Notification | `NOTIFICATION_READ` | `NOTIFICATION_WRITE` | `/notification` butuh salah satu read/write |
| Admin | - | `ROLE_ADMIN` **atau** bundle `USER_WRITE` + `ROLE_WRITE` + `PERMISSION_WRITE` | `/admin` |

## 6) Catatan fallback students endpoint

Untuk modul yang membaca daftar siswa, frontend menggunakan endpoint utama students dari `NEXT_PUBLIC_API_URL`.

Jika endpoint utama students gagal (misalnya `404/5xx` karena perbedaan versi API antar environment), QA/UAT perlu validasi fallback berikut:

1. Pastikan request awal ke endpoint students utama tercatat di Network tab.
2. Pastikan frontend melakukan retry ke endpoint fallback students yang disiapkan di layer API client.
3. Jika keduanya gagal, UI harus menampilkan state error yang bisa ditindaklanjuti (bukan blank screen).
4. Lampirkan payload + status code saat pelaporan bug agar tim BE/FE bisa mapping issue kompatibilitas endpoint.

> Rekomendasi QA: selalu uji skenario sukses + fallback minimal sekali di tiap environment (local/dev/staging) sebelum sign-off UAT.

## 7) Auth flow singkat

1. User login di `/login`.
2. Frontend call `POST /api/auth/login`, simpan `accessToken` + `refreshToken`.
3. Frontend call `GET /api/users/me` untuk hydrate user profile + permissions.
4. Semua request API bawa `Authorization: Bearer <accessToken>`.
5. Jika response `401`, interceptor akan coba `POST /api/auth/refresh` sekali lalu retry request awal.
6. Jika refresh gagal, sesi di-clear dan user diarahkan lagi ke `/login`.
7. Logout memanggil `POST /api/auth/logout` lalu clear token lokal.

## 8) Troubleshooting umum

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
