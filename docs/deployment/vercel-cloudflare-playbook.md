# Playbook Deploy Frontend ke Vercel + DNS Cloudflare

Dokumen ini men-standardisasi setup frontend (Next.js) dari repo `frontend` ke Vercel, pengaturan DNS Cloudflare, dan verifikasi post-deploy untuk auth/RBAC/cross-module API.

## 1) Setup project frontend di Vercel

> Prasyarat: punya akses ke team/project Vercel + Cloudflare zone domain.

1. Masuk ke folder frontend:

   ```bash
   cd frontend
   ```

2. Login Vercel CLI dan link project:

   ```bash
   vercel login
   vercel link
   ```

3. Saat wizard `vercel link`, pilih:
   - Scope/team yang benar.
   - Project existing (jika sudah ada), atau create project baru.
   - Root directory: `.` (karena sudah berada di `frontend`).

4. Deploy awal untuk mendapatkan Vercel target domain:

   ```bash
   vercel --prod
   ```

5. Catat output domain Vercel (contoh: `boost-frontend-abc123.vercel.app`) untuk dipakai sebagai target CNAME Cloudflare.

## 2) Set environment production `NEXT_PUBLIC_API_URL`

Set environment variable production ke domain backend API (contoh: `https://api.domainkamu.com`).

### Opsi A — via CLI (direkomendasikan)

```bash
printf 'https://api.domainkamu.com' | vercel env add NEXT_PUBLIC_API_URL production
```

Jika sudah ada value lama dan ingin update, gunakan dashboard Vercel (Environment Variables) untuk edit value, lalu redeploy production:

```bash
vercel --prod
```

### Opsi B — via Dashboard Vercel

- Project Settings → Environment Variables.
- Tambahkan `NEXT_PUBLIC_API_URL`.
- Environment: **Production**.
- Redeploy deployment production terbaru.

## 3) Atur DNS Cloudflare

Tambahkan record di zone Cloudflare:

- **Type**: CNAME
- **Name**: `app` (untuk `app.domainkamu.com`)
- **Target**: `<target-vercel>.vercel.app`
- **Proxy status**: Proxied (orange cloud) *boleh*, tetapi perhatikan rule di bawah.

Setelah DNS propagate, tambahkan domain `app.domainkamu.com` di Vercel Project → Domains agar certificate Vercel tervalidasi.

## 4) SSL mode + cache rule agar auth flow aman

Di Cloudflare, gunakan baseline berikut:

1. **SSL/TLS mode**: `Full (strict)`.
2. **Always Use HTTPS**: ON.
3. **Automatic HTTPS Rewrites**: ON.
4. **Caching**:
   - Jangan cache response HTML app/auth pages.
   - Buat Cache Rule untuk bypass path sensitif (minimal):
     - `/login*`
     - `/api/auth/*` (jika auth endpoint lewat domain frontend)
     - path callback auth (misalnya `/auth/callback*` jika ada).
   - Untuk rule tersebut set **Cache eligibility: Bypass**.

Tujuan rule ini: mencegah token/session state stale akibat edge cache di route auth.

## 5) Verifikasi post-deploy (checklist wajib)

Jalankan validasi ini pada domain production `https://app.domainkamu.com`:

### A. Login

- Buka halaman login.
- Login dengan akun valid.
- Pastikan diarahkan ke dashboard.
- Pastikan tidak ada loop redirect `/login` ↔ `/dashboard`.

### B. Refresh token flow

- Login lalu biarkan akses token mendekati expiry (atau set expiry pendek di staging untuk simulasi).
- Trigger request API setelah expiry.
- Pastikan app melakukan refresh token otomatis.
- Pastikan user tetap login dan request retry sukses.

### C. Akses halaman berdasarkan permission (RBAC)

Uji minimal 2 role:

- Role dengan permission tinggi dapat akses halaman admin/modul sensitif.
- Role terbatas ditolak (403/redirect ke forbidden) sesuai policy.

### D. API lintas modul sukses

Uji call API dari beberapa modul frontend (contoh: dashboard, billing, attendance):

- Semua request menuju `NEXT_PUBLIC_API_URL` production.
- Tidak ada CORS issue.
- Response code sesuai ekspektasi (2xx untuk skenario normal).

## 6) Acceptance criteria

Deploy dianggap selesai jika:

1. `app.domainkamu.com` resolve ke deployment Vercel aktif.
2. `NEXT_PUBLIC_API_URL` production mengarah ke backend API domain.
3. Login + refresh token flow berjalan tanpa loop/session drop.
4. RBAC route guard sesuai permission.
5. API lintas modul sukses tanpa error CORS/cache.

## 7) Troubleshooting cepat

- **Masih hitting environment lama**: pastikan redeploy setelah update env.
- **Loop login setelah Cloudflare ON**: cek cache bypass untuk path auth + cookie/headers tidak di-strip.
- **SSL error**: pastikan mode `Full (strict)` + domain sudah verified di Vercel.
- **403 tak sesuai role**: validasi mapping permission frontend vs claim backend.
