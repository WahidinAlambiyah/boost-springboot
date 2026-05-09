# TailAdmin Pro Migration Plan

Dokumen ini menjadi fase kerja untuk mengadopsi TailAdmin Pro ke frontend admin tanpa mengganti logic aplikasi yang sudah berjalan.

## Tujuan

- Memakai TailAdmin Pro sebagai UI/theme layer untuk admin dashboard.
- Mempertahankan auth, RBAC, backend menu, route, dan API service existing.
- Menghindari rewrite besar yang berisiko merusak fitur.
- Memungkinkan migrasi bertahap per layout, komponen, dan halaman.

## Prinsip Integrasi

1. Jangan replace folder `src/app` TailAdmin Pro secara langsung.
2. Jangan timpa file auth existing seperti `src/lib/api.ts`, `src/store/auth.ts`, protected layout, dan permission guard.
3. Komponen TailAdmin Pro ditempatkan sebagai adapter/theme components di project existing.
4. Route existing tetap menjadi sumber kebenaran.
5. Backend menu/RBAC tetap menjadi sumber menu, bukan hardcoded menu TailAdmin.
6. Dependency TailAdmin Pro hanya ditambahkan kalau benar-benar dipakai.
7. Source TailAdmin Pro harus mengikuti lisensi pembelian dan jangan dipublish ke repo publik jika lisensi tidak mengizinkan.

## Phase 0 - Assessment dan Guardrail

Status: Started

Checklist:

- Cek stack frontend existing: Next.js, React, Tailwind CSS.
- Cek komponen layout existing: `AppShell`, `Topbar`, `Sidebar`.
- Buat branch aman untuk integrasi.
- Buat dokumen migrasi ini.
- Identifikasi komponen TailAdmin Pro yang boleh diambil:
  - layout shell
  - sidebar style
  - header/topbar style
  - card/stat components
  - table components
  - form components
  - badge/status components
  - modal/dialog components

Output:

- Branch kerja: `codex/tailadmin-template-foundation`
- Dokumen roadmap ini.

## Phase 1 - Theme Foundation

Status: Started

Tujuan:

- Menyiapkan komponen UI foundation bernuansa TailAdmin tanpa butuh source Pro dulu.
- Membuat wrapper component yang mudah diganti dengan komponen TailAdmin Pro asli nanti.

Pekerjaan:

- Buat folder `src/app/components/tailadmin`.
- Buat `tailadmin-card.tsx` untuk card layout.
- Buat `tailadmin-page-shell.tsx` untuk wrapper halaman.
- Buat `tailadmin-stat-card.tsx` untuk dashboard stat.
- Pastikan komponen tidak membawa logic bisnis.

Kriteria selesai:

- TypeScript compile.
- Komponen bisa dipakai oleh dashboard atau halaman admin lain.
- Tidak mengubah API service dan RBAC.

## Phase 2 - Layout Integration

Status: Planned

Tujuan:

- Mengubah `AppShell`, `Topbar`, dan `Sidebar` agar lebih mendekati TailAdmin Pro.
- Tetap memakai menu dari backend.

Pekerjaan:

- Refine sidebar: logo area, icon slot, active state, nested menu visual, collapsed/expanded mode.
- Refine topbar: search area opsional, profile dropdown, notification placeholder.
- Tambah responsive mobile drawer.
- Pastikan collapse state tidak merusak content width.

Kriteria selesai:

- Sidebar bisa collapse/expand.
- Mobile layout tidak overflow.
- Backend menu tetap berjalan.
- Logout tetap berjalan.

## Phase 3 - Dashboard Migration

Status: Planned

Tujuan:

- Mengubah tampilan dashboard agar memakai card/stat style TailAdmin.

Pekerjaan:

- Ganti quick links ke TailAdmin-style cards.
- Ganti permission badges menjadi lebih rapi.
- Tambah summary widgets bila backend sudah tersedia.

Kriteria selesai:

- Dashboard lebih modern.
- Tidak ada perubahan endpoint backend.

## Phase 4 - CRUD Components Migration

Status: Planned

Tujuan:

- Menstandardisasi table, form, search, pagination, modal, empty state, dan error state.

Pekerjaan:

- Buat adapter DataTable TailAdmin-style.
- Buat form fields style TailAdmin.
- Migrasi halaman prioritas:
  1. Academies
  2. Academy Locations
  3. Students
  4. Assessment Skills
  5. Attendance

Kriteria selesai:

- Pola UI konsisten.
- Existing tests tetap jalan.

## Phase 5 - TailAdmin Pro Source Mapping

Status: Waiting for TailAdmin Pro source

Tujuan:

- Mapping source TailAdmin Pro asli ke project.

Pekerjaan setelah source tersedia:

- Bandingkan struktur TailAdmin Pro dengan project existing.
- Ambil hanya komponen presentational.
- Mapping asset/icon seperlunya.
- Cek dependency tambahan.
- Hindari membawa auth mock/dashboard dummy route TailAdmin.

Kriteria selesai:

- Source Pro masuk terkontrol.
- Tidak ada file sensitif/lisensi yang dilanggar.

## Phase 6 - Hardening

Status: Planned

Tujuan:

- Finalisasi kualitas sebelum merge.

Pekerjaan:

- Run `npm run typecheck`.
- Run `npm run lint`.
- Run `npm run test`.
- Manual smoke test login, dashboard, sidebar, CRUD page, logout.
- Review responsive desktop/mobile.

## Catatan untuk Codex/AI Coding Agent

Saat mengerjakan task TailAdmin Pro:

- Jangan mengganti backend API contract.
- Jangan menghapus permission guard.
- Jangan membuat menu hardcoded sebagai sumber utama.
- Jangan memindahkan semua route TailAdmin Pro ke project ini.
- Prioritaskan adapter components dan incremental migration.
