# RBAC Learning v1

## Metadata
- Version: `v1.1`
- Effective date: `2026-04-28`
- Owner: `Security / Backend Team`
- Baseline tag: `product-learning-baseline-v1.0.0`

---

## Tujuan
Dokumen ini mendefinisikan model Role-Based Access Control (RBAC) untuk domain kursus bahasa + olahraga anak, termasuk role, permission, dan aturan implementasi agar konsisten lintas service/API.

---

## Role yang didukung
1. `ADMIN`
2. `INSTRUCTOR`
3. `GUARDIAN`
4. `OPS`
5. `FINANCE`

---

## Permission Dictionary

### Catalog / Delivery
- `PROGRAM_READ`
- `PROGRAM_WRITE`
- `COURSE_READ`
- `COURSE_WRITE`
- `CLASS_READ`
- `CLASS_WRITE`

### Scheduling (canonical)
- `SCHEDULE_READ`
- `SCHEDULE_WRITE`
- `SCHEDULE_RESCHEDULE`

### Scheduling (deprecated alias, transitional)
- `SESSION_READ` → alias deprecated dari `SCHEDULE_READ`
- `SESSION_WRITE` → alias deprecated dari `SCHEDULE_WRITE`
- `SESSION_RESCHEDULE` → alias deprecated dari `SCHEDULE_RESCHEDULE`

### Student / Guardian
- `STUDENT_READ`
- `STUDENT_WRITE`
- `GUARDIAN_READ`
- `GUARDIAN_WRITE`

### Enrollment
- `ENROLLMENT_READ`
- `ENROLLMENT_WRITE`
- `WAITLIST_MANAGE`

### Attendance / Progress
- `ATTENDANCE_READ`
- `ATTENDANCE_MARK`
- `PROGRESS_READ`
- `PROGRESS_WRITE`
- `ASSESSMENT_READ`
- `ASSESSMENT_WRITE`

### Billing / Reporting / Audit
- `BILLING_READ`
- `INVOICE_WRITE`
- `PAYMENT_RECORD`
- `REFUND_APPROVE`
- `REPORT_EXPORT`
- `AUDIT_READ`

---

## Role -> Permission Matrix

| Permission | ADMIN | INSTRUCTOR | GUARDIAN | OPS | FINANCE |
|---|---|---|---|---|---|
| PROGRAM_READ | ✅ |  |  | ✅ |  |
| PROGRAM_WRITE | ✅ |  |  |  |  |
| COURSE_READ | ✅ |  |  | ✅ |  |
| COURSE_WRITE | ✅ |  |  |  |  |
| CLASS_READ | ✅ | ✅ | ✅ | ✅ |  |
| CLASS_WRITE | ✅ |  |  | ✅ |  |
| SCHEDULE_READ | ✅ | ✅ |  | ✅ |  |
| SCHEDULE_WRITE | ✅ |  |  | ✅ |  |
| SCHEDULE_RESCHEDULE | ✅ |  |  | ✅ |  |
| STUDENT_READ | ✅ | ✅* |  | ✅ |  |
| STUDENT_WRITE | ✅ |  |  | ✅ |  |
| GUARDIAN_READ | ✅ |  |  | ✅ |  |
| GUARDIAN_WRITE | ✅ |  |  | ✅ |  |
| ENROLLMENT_READ | ✅ |  | ✅* | ✅ | ✅ |
| ENROLLMENT_WRITE | ✅ |  |  | ✅ |  |
| WAITLIST_MANAGE | ✅ |  |  | ✅ |  |
| ATTENDANCE_READ | ✅ | ✅ | ✅* | ✅ |  |
| ATTENDANCE_MARK | ✅ | ✅ |  |  |  |
| PROGRESS_READ | ✅ | ✅ | ✅* |  |  |
| PROGRESS_WRITE | ✅ | ✅ |  |  |  |
| ASSESSMENT_READ | ✅ | ✅ |  | ✅ |  |
| ASSESSMENT_WRITE | ✅ | ✅ |  |  |  |
| BILLING_READ | ✅ |  | ✅* |  | ✅ |
| INVOICE_WRITE | ✅ |  |  |  | ✅ |
| PAYMENT_RECORD | ✅ |  |  |  | ✅ |
| REFUND_APPROVE | ✅ |  |  |  | ✅ |
| REPORT_EXPORT | ✅ |  |  | ✅ | ✅ |
| AUDIT_READ | ✅ |  |  |  |  |

Catatan:
- `*` artinya akses dibatasi ownership/scope:
  - INSTRUCTOR hanya untuk class yang dia ajar.
  - GUARDIAN hanya untuk anak yang terhubung di `student_guardians`.
- Permission alias `SESSION_*` hanya untuk kompatibilitas transisi; assignment role baru wajib menggunakan `SCHEDULE_*`.

---

## Aturan Scope Enforcement (wajib)
1. **Controller-level auth**: endpoint protected wajib `@PreAuthorize`.
2. **Service-level guard**: validasi scope ownership (guardian->student, instructor->class_group).
3. **DB query scope**: jangan fetch data lintas tenant/scope lalu disaring di memory.
4. **Inactive role/permission**: tidak boleh memberi authority.
5. **Audit log**: perubahan enrollment, attendance, billing, refund harus tercatat.

---

## API Error Contract
- `401 Unauthorized`: token tidak valid / tidak ada.
- `403 Forbidden`: token valid tapi permission/scope tidak memenuhi.
- `422 Unprocessable Entity`: payload validasi bisnis gagal.

---

## Testing Policy (minimum)
1. Authorization matrix test per endpoint:
  - no token -> 401
  - wrong role -> 403
  - correct role -> 2xx
2. Scope test:
  - guardian akses anak lain -> 403
  - instructor akses kelas lain -> 403
3. Regression test untuk permission inactive.

### Authorization test matrix tambahan untuk migrasi naming
| Area | Guard canonical | Alias legacy diizinkan? | Ekspektasi test |
|---|---|---|---|
| Backend endpoint scheduling | `SCHEDULE_READ`, `SCHEDULE_WRITE`, `SCHEDULE_RESCHEDULE` | Tidak untuk kontrak baru | Request dengan authority `SCHEDULE_*` lolos; authority non-canonical ditolak jika endpoint tidak memerlukan alias |
| Frontend sidebar/dashboard | `SCHEDULE_*` (utama) | Ya, selama transisi | Menu scheduling tetap muncul jika user masih membawa `SESSION_*` dari token lama |
| Frontend page guard scheduling | `SCHEDULE_*` (utama) | Ya, selama transisi | Halaman scheduling dapat dibuka oleh user `SCHEDULE_*` maupun `SESSION_*`; release berikutnya hapus alias |

---

## Change Management
- Perubahan RBAC bersifat **additive-first**.
- Penghapusan permission wajib lewat fase deprecate.
- Setiap perubahan matrix wajib:
  1) update SQL seed/migration,
  2) update dokumen ini,
  3) update test authorization,
  4) masuk release note.

### Release documentation checklist (RBAC naming migration)
1. Cantumkan bahwa canonical scheduling permission adalah `SCHEDULE_*`.
2. Cantumkan status deprecated untuk `SESSION_*` beserta target removal version.
3. Lampirkan daftar migration/seed yang diperbarui (`RBACLearningV1Seed.sql` + changelog Liquibase).
4. Lampirkan hasil gate authorization matrix (`AuthorizationMatrixTest`) pada release notes.
5. Komunikasikan impact frontend: mapping sidebar/dashboard/guard menerima canonical + alias selama masa transisi.

### Catatan deprecate resmi
- Efektif mulai `2026-04-28`, `SESSION_*` dinyatakan deprecated.
- `SCHEDULE_*` menjadi satu-satunya naming canonical untuk modul scheduling.
- Alias `SESSION_*` dipertahankan sementara untuk backward compatibility dan akan dihapus setelah seluruh client bermigrasi.
