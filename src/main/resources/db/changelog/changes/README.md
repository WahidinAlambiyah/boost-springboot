# Changelog Module Layout & Naming Convention

Semua changelog dipisah per modul untuk memudahkan traceability:

- `db/changelog/changes/iam/...`
- `db/changelog/changes/catalog/...`
- `db/changelog/changes/scheduling/...`
- `db/changelog/changes/billing/...`
- `db/changelog/changes/notification/...`
- dst.

## Aturan ID changeSet

Gunakan prefix modul pada `id` untuk changelog baru (contoh):

- `iam-016-create-login-attempt-index`
- `catalog-001-create-class-tags`
- `scheduling-003-add-session-capacity`

> Catatan kompatibilitas: changeSet legacy (002–015) tetap mempertahankan `id` lama agar tidak mengubah riwayat Liquibase yang sudah tercatat.

## Aturan perubahan skema

Perubahan skema existing wajib **additive**:

- boleh: tambah tabel/kolom/index/constraint baru
- hindari: `drop`/`rename` langsung pada objek existing

Jika perlu deprecate kolom/tabel, lakukan bertahap (additive + backfill + rollout aplikasi + cleanup terpisah setelah aman).
