# TailAdmin Pro Migration Plan

Tujuan dokumen ini adalah memandu migrasi UI admin existing ke gaya TailAdmin Pro secara bertahap tanpa merusak auth, RBAC menu, API service, route, dan flow backend yang sudah berjalan.

## Prinsip Utama

- TailAdmin Pro dipakai sebagai UI/layout layer, bukan mengganti business logic aplikasi.
- Auth, JWT, `useAuthStore`, protected route, permission guard, dan API client existing tetap dipertahankan.
- Menu sidebar tetap berasal dari backend/RBAC. Template hanya mengubah tampilan dan interaksi.
- Migrasi dilakukan per phase dan per branch agar mudah rollback.
- Source TailAdmin Pro berlisensi tidak dimasukkan ke repo public kecuali lisensi mengizinkan.

## Phase 0 — Preparation & Safety

Output:

- Branch kerja khusus TailAdmin.
- Dokumen migration plan.
- Review kompatibilitas versi Next, React, Tailwind, dan dependency TailAdmin.

Checklist:

- Pastikan source TailAdmin Pro tersedia secara legal.
- Cek apakah TailAdmin Pro memakai Next.js, React, Tailwind, icon package, chart package, datepicker, dan UI helper lain.
- Jangan replace folder `src/app`, `src/lib`, `src/store`, atau service API existing.

## Phase 1 — Design Token & Shared UI Foundation

Output:

- Folder `frontend/src/app/components/tailadmin` untuk komponen UI adaptasi.
- Komponen dasar: `TailAdminCard`, `TailAdminButton`, `TailAdminBadge`, `TailAdminSectionTitle`.
- Style konsisten untuk border, radius, shadow, spacing, background, dan typography.

Scope coding:

- Tidak menyentuh API.
- Tidak mengubah route.
- Tidak mengubah RBAC.
- Membuat komponen wrapper agar halaman existing bisa di-upgrade bertahap.

## Phase 2 — Layout Shell Migration

Output:

- App shell bergaya TailAdmin.
- Sidebar responsive + collapsed/expanded.
- Topbar dengan hamburger, user dropdown area, dan layout yang lebih modern.
- Tetap menggunakan menu backend dari `useAuthStore`.

Scope coding:

- `frontend/src/app/components/app-shell.tsx`
- `frontend/src/app/components/sidebar.tsx`
- `frontend/src/app/components/topbar.tsx`
- Test sidebar/topbar disesuaikan.

## Phase 3 — Dashboard Migration

Output:

- Dashboard cards/statistics bergaya TailAdmin.
- Quick links lebih rapi.
- Permission chips lebih readable.

Scope coding:

- `frontend/src/app/(protected)/dashboard/page.tsx`
- Komponen statistik/card reusable bila diperlukan.

## Phase 4 — CRUD Pages Migration

Output:

- Table, form, search, pagination, empty state, loading state bergaya TailAdmin.
- Applied pada modul utama: Academies, Academy Locations, Coach Profiles, Students, Assessment Skills.

Scope coding:

- Komponen table/form reusable dulu.
- Upgrade halaman satu per satu agar mudah test.

## Phase 5 — Advanced Pages & Reports

Output:

- Attendance, Scheduling, Billing, Student Progress Report memakai komponen TailAdmin.
- Report card dan timeline lebih readable.
- Optional chart integration hanya jika benar-benar dibutuhkan.

## Phase 6 — Polish, Accessibility, Responsive, Dark Mode

Output:

- Mobile sidebar drawer.
- Better keyboard focus.
- Dark mode jika diperlukan.
- Visual QA untuk desktop, tablet, dan mobile.

## Phase 7 — Cleanup & Documentation

Output:

- Hapus komponen lama yang tidak dipakai.
- Update README frontend.
- Tambah screenshot/manual QA checklist.
- Pastikan `npm run lint`, `npm run typecheck`, dan test lulus.

## Coding Rule untuk Codex/AI Assistant

Saat mengerjakan TailAdmin migration:

1. Kerjakan satu phase per PR.
2. Jangan mengubah API contract kecuali diminta.
3. Jangan menghapus fallback menu/RBAC logic.
4. Jangan copy asset berlisensi ke repo tanpa konfirmasi lisensi.
5. Gunakan komponen wrapper di `components/tailadmin` agar migrasi aman.
6. Setelah perubahan UI, jalankan minimal typecheck dan test terkait jika environment tersedia.

## Status

- Phase 0: Started
- Phase 1: Next
- Phase 2: Partially available via collapsible sidebar baseline
