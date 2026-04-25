# Release Notes — modular-monolith-baseline-v1.0.0

Tanggal rilis: 25 April 2026  
Tag: `modular-monolith-baseline-v1.0.0`

## 1) Ruang lingkup refactor

Baseline ini menandai selesainya fase refactor backend ke pola **modular monolith** dengan ruang lingkup berikut:

- Penegasan batas modul berdasarkan domain inti: `iam`, `catalog`, `scheduling`, `billing`, `notification`, dan `common`.
- Penataan dependency lintas modul agar tidak membentuk cyclic dependency.
- Konsolidasi aturan akses lintas modul melalui service contract internal (bukan akses repository/entity langsung).
- Penguatan guardrail arsitektur melalui dokumen aturan modul dan architecture test.

## 2) Jaminan backward compatibility API

Pada baseline ini, kontrak API eksternal dipertahankan backward-compatible dengan prinsip:

- Endpoint publik yang sudah digunakan klien tidak mengalami breaking change pada path utama.
- Format payload request/response existing dipertahankan, termasuk semantik field inti.
- Perubahan internal modul dibatasi pada struktur package, ownership data, dan dependency internal tanpa mengubah kontrak API publik.

Catatan: setiap perubahan API setelah baseline ini wajib mengikuti versioning policy dan changelog release berikutnya.

## 3) Aturan module boundary final

Aturan final boundary yang menjadi acuan resmi:

1. **Single ownership data**: setiap tabel/entity dimiliki satu modul owner.
2. **No direct cross-module persistence access**: dilarang mengakses repository/entity modul lain secara langsung.
3. **Interaksi lintas modul via service contract**: gunakan `*QueryService`/`*CommandService` milik owner modul.
4. **Arah dependency satu arah** sesuai blueprint modular monolith; `common` hanya boleh menjadi fondasi dan tidak bergantung ke modul domain.
5. **Asynchronous decoupling** untuk kebutuhan long-running/loosely coupled flow melalui event/outbox.
6. **Review gate wajib**: setiap PR menolak import implementasi internal modul lain dan penambahan edge dependency yang berpotensi membentuk cycle.

Referensi aturan detail: `docs/architecture/modular-monolith.md` dan `docs/development/modular-guidelines.md`.

## 4) Langkah berikutnya (frontend phase)

Baseline ini menjadi titik restart resmi sebelum fase frontend dimulai.

Prioritas fase berikutnya:

1. Menyusun contract mapping API-to-UI per modul (IAM, Catalog, Scheduling, Billing).
2. Menetapkan strategi auth frontend (access token, refresh flow, route guard, permission-aware UI).
3. Menyusun design system dasar + state management convention.
4. Implementasi dashboard dan alur operasional utama berbasis kontrak API existing.
5. Menyiapkan e2e smoke suite frontend terhadap baseline backend ini.

## 5) Status baseline

Tag `modular-monolith-baseline-v1.0.0` ditetapkan sebagai **official restart point** untuk melanjutkan inisiatif frontend tanpa menggeser boundary modular backend yang sudah disepakati.
