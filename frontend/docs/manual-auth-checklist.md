# Manual Auth Regression Checklist

Tanggal verifikasi: 2026-05-05.

## Skenario wajib

1. Akses `/` menampilkan landing page publik.
2. Akses `/dashboard` tanpa login tetap diarahkan ke halaman login.
3. Login dengan akun valid lalu akses `/dashboard` tetap berjalan normal.

## Catatan

- Jangan ubah `src/app/(protected)/layout.tsx`.
- Jangan ubah apa pun di `src/features/auth/*`.
- Jangan ubah `src/store/auth-store.ts`.
- Jangan ubah daftar route protected yang sudah ada.
