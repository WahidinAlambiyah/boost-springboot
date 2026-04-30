# Panduan Development Modular

Dokumen ini menjelaskan aturan praktis saat menambah fitur baru di arsitektur modular monolith agar batas modul tetap terjaga, perubahan mudah direview, dan onboarding developer baru lebih cepat.

## 1) Cara menambah fitur di modul yang benar

Gunakan urutan berikut setiap kali menambah fitur:

1. **Petakan domain fitur**
   - Tentukan fitur ini milik modul mana berdasarkan ownership data dan business rule utama.
   - Contoh: fitur terkait enrollment siswa masuk ke modul `enrollment`, bukan `attendance` atau `billing`.

2. **Definisikan use case di dalam modul pemilik**
   - Tambahkan command/query service di modul tersebut (mis. `EnrollmentCommandService`, `EnrollmentQueryService`).
   - Letakkan validasi business rule utama tetap di modul pemilik.

3. **Batasi akses data ke repository modul sendiri**
   - Hanya modul pemilik yang boleh menyentuh entity/repository internalnya.
   - Modul lain harus konsumsi lewat internal service API (interface/service contract), bukan akses tabel atau repository langsung.

4. **Expose endpoint dari modul pemilik**
   - Controller endpoint ditaruh di package modul pemilik fitur.
   - DTO request/response mengikuti naming convention modul.

5. **Tambahkan migration bila ada perubahan schema**
   - Perubahan tabel domain hanya oleh modul pemilik.
   - Nama migration harus deskriptif dan bisa ditelusuri ke fitur.

6. **Lengkapi test berlapis**
   - Unit test untuk service/use case.
   - Integration test untuk alur endpoint + persistence + security (jika relevan).

7. **Update dokumentasi API (Swagger/OpenAPI)**
   - Pastikan endpoint, request body, response, dan error case tercermin di dokumentasi.

---

## 2) Larangan akses lintas modul langsung

Untuk menjaga encapsulation dan mencegah cyclic dependency, hal berikut **dilarang**:

- Import `Entity`, `Repository`, atau `Service` internal dari modul lain tanpa kontrak API yang disepakati.
- Query langsung ke tabel modul lain dari modul yang bukan pemilik data.
- Menaruh business rule modul A di modul B hanya karena endpoint berada di modul B.
- Menambah dependency baru antar modul tanpa alasan domain yang kuat dan review arsitektur.

### Pola yang benar

- Jika modul A butuh data dari modul B, modul A memanggil **public/internal service API** yang disediakan modul B.
- Jika integrasi bersifat asynchronous, gunakan event/outbox pattern sesuai aturan proyek.

---

## 3) Pola naming package

Gunakan pola package konsisten berikut:

```text
id.yourorg.boost.<module>.<layer>
```

Contoh:

- `id.yourorg.boost.enrollment.controller`
- `id.yourorg.boost.enrollment.service`
- `id.yourorg.boost.enrollment.repository`
- `id.yourorg.boost.enrollment.model`
- `id.yourorg.boost.enrollment.dto`

### Aturan naming tambahan

- Nama modul memakai lowercase singular: `iam`, `catalog`, `scheduling`, `enrollment`, `attendance`, `billing`, `notification`, `common`.
- Nama class use case eksplisit:
  - Command: `CreateEnrollmentCommandService`, `ChangeEnrollmentStatusService`
  - Query: `GetEnrollmentDetailQueryService`, `ListActiveEnrollmentQueryService`
- DTO:
  - Request: `CreateEnrollmentRequest`
  - Response: `EnrollmentDetailResponse`

---

## 4) PR checklist wajib

Sebelum merge, pastikan checklist ini lengkap di deskripsi PR:

- [ ] **Compile success**: build backend sukses tanpa error kompilasi.
- [ ] **Liquibase on empty DB**: migration sukses di database kosong.
- [ ] **Liquibase on legacy DB**: migration sukses di database berisi data lama, termasuk backfill `academy_id` berjalan sesuai ekspektasi.
- [ ] **Default academy seed**: data default academy ter-seed setelah migration.
- [ ] **New permission seed**: permission baru ter-seed dan dapat dipakai pada role assignment.
- [ ] **Swagger update**: endpoint baru muncul di OpenAPI/Swagger dengan request/response/status code yang benar.
- [ ] **Regression safety**: endpoint existing tetap lulus test regression.

### Backend command gate (ikuti pipeline repo)

Gunakan satu command gate backend berikut sebelum merge (subset test + OpenAPI compliance):

```bash
mvn -Dtest=AuthorizationLookupRepositoryTest,OpenApiDocumentationComplianceTest test
```

---

## 5) Contoh end-to-end: fitur baru `Enroll Student to Class Group`

Tujuan: developer baru punya gambaran implementasi dari request API sampai data tersimpan.

### A. Scope & module ownership

- Fitur: mendaftarkan siswa ke class group.
- Modul pemilik: `enrollment`.
- Dependensi yang diizinkan:
  - `catalog` untuk validasi class group masih aktif/terbuka.
  - `iam` untuk otorisasi actor.

### B. Desain API

- Endpoint: `POST /api/enrollments`
- Request:

```json
{
  "studentId": "std_001",
  "classGroupId": "cg_english_a",
  "startDate": "2026-04-01"
}
```

- Response (201):

```json
{
  "enrollmentId": "enr_123",
  "studentId": "std_001",
  "classGroupId": "cg_english_a",
  "status": "ACTIVE"
}
```

### C. Struktur package yang ditambah

```text
id.yourorg.boost.enrollment.controller.EnrollmentController
id.yourorg.boost.enrollment.dto.CreateEnrollmentRequest
id.yourorg.boost.enrollment.dto.EnrollmentResponse
id.yourorg.boost.enrollment.service.CreateEnrollmentCommandService
id.yourorg.boost.enrollment.repository.EnrollmentRepository
id.yourorg.boost.enrollment.model.Enrollment
```

### D. Alur eksekusi fitur

1. `EnrollmentController` menerima request `POST /api/enrollments`.
2. `CreateEnrollmentCommandService` validasi permission actor melalui `iam`.
3. Service memanggil `CatalogPolicyService.ensureClassGroupOpenForEnrollment(classGroupId)`.
4. Service cek idempotency/duplikasi enrollment aktif.
5. Service simpan entity `Enrollment` ke repository modul `enrollment`.
6. Service mengembalikan response DTO.
7. (Opsional) publish event `EnrollmentCreated` untuk notifikasi/billing secara asynchronous.

### E. Testing minimum

- Unit test:
  - sukses create enrollment.
  - gagal jika class group ditutup.
  - gagal jika duplicate active enrollment.
- Integration test:
  - `POST /api/enrollments` mengembalikan `201` pada skenario valid.
  - validasi security/permission berjalan.

### F. Artefak PR untuk fitur ini

- Kode controller/service/repository/model di modul `enrollment`.
- Migration tabel atau index baru (jika diperlukan).
- Update Swagger/OpenAPI.
- Checklist PR terisi penuh.

Dengan alur ini, developer baru bisa mengikuti pola implementasi yang aman terhadap module boundary dan konsisten untuk fitur berikutnya.
