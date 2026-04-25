# RBAC Baseline v1

Dokumen ini menjadi baseline RBAC versi awal (`v1`) untuk aplikasi Boost Spring Boot.

## 1) Daftar Role

Role default yang tersedia dari seed RBAC:

1. `ADMIN`
2. `USER`
3. `SUPPORT`
4. `FINANCE`
5. `USER_MANAGER`

## 2) Daftar Permission

Permission baseline dari changelog saat ini:

- `USER_READ`
- `USER_WRITE`
- `USER_DELETE`
- `USER_DISABLE`
- `USER_ENABLE`
- `USER_UNLOCK`
- `ROLE_READ`
- `ROLE_WRITE`
- `ROLE_DELETE`
- `PERMISSION_READ`
- `PERMISSION_WRITE`
- `PERMISSION_DELETE`
- `AUDIT_READ`
- `REPORT_EXPORT`


### Tambahan permission (add-only, kompatibel baseline)

Permission berikut ditambahkan tanpa mengubah permission lama:

- `CLASS_READ`
- `CLASS_WRITE`
- `SCHEDULE_READ`
- `SCHEDULE_WRITE`
- `ENROLLMENT_READ`
- `ENROLLMENT_WRITE`
- `ATTENDANCE_READ`
- `ATTENDANCE_MARK`
- `BILLING_READ`
- `BILLING_WRITE`
- `NOTIFICATION_READ`
- `NOTIFICATION_WRITE`

## 3) Mapping Role -> Permission (Baseline)

### `ADMIN`
Semua permission baseline di atas.

### `USER`
Tidak ada permission langsung (empty direct permission set).

### `SUPPORT`
Tidak ada permission langsung (empty direct permission set).

### `FINANCE`
Tidak ada permission langsung (empty direct permission set).

### `USER_MANAGER`
Tidak ada permission langsung (empty direct permission set).

> Catatan: pada baseline ini, assignment `role_permissions` eksplisit hanya dilakukan untuk `ADMIN` melalui changelog seed RBAC.

## 4) Aturan Self-Registration dan Admin-Only Flows

### Self-Registration

- Endpoint auth dipublikasikan (`/api/auth/**` permitAll) sehingga registrasi dapat dilakukan tanpa login.
- Registrasi (`register`) selalu memberi role default `USER`.
- Self-registration **tidak** boleh meng-assign role lain selain default `USER`.

### Admin-Only Flows

Alur berikut wajib memenuhi kombinasi permission + role authority `ROLE_ADMIN`:

- Buat user: `USER_WRITE` + `ROLE_ADMIN`
- Update user: `USER_WRITE` + `ROLE_ADMIN`
- Assign role ke user: `USER_WRITE` + `ROLE_ADMIN`
- Hapus user: `USER_DELETE` + `ROLE_ADMIN`
- Update password user lain: `USER_WRITE` + `ROLE_ADMIN`

Alur user lifecycle yang guarded by permission:

- Disable user: `USER_DISABLE`
- Enable user: `USER_ENABLE`
- Unlock user: `USER_UNLOCK`

## 5) Aturan Perubahan RBAC ke Depan

Agar perubahan RBAC aman dan tidak merusak kompatibilitas:

1. **Add-only default policy**
   - Penambahan role/permission baru diperbolehkan.
   - Role/permission yang sudah ada tidak langsung dihapus dari baseline aktif.

2. **Deprecate dulu, jangan hard-delete**
   - Jika role/permission ingin dihentikan, tandai sebagai deprecated terlebih dahulu di dokumentasi baseline dan changelog.
   - Berikan masa transisi sebelum benar-benar dihapus.

3. **Tidak breaking untuk API security**
   - Jangan mengubah requirement permission endpoint existing secara mendadak.
   - Jika perlu tightening policy, lakukan bertahap, dokumentasikan impact, dan sertakan jalur migrasi.

4. **Versioning dokumen baseline**
   - Setiap perubahan role/permission wajib menaikkan versi dokumen baseline (`v1.x`, `v2.x`, dst.) dan menyertakan changelog RBAC.

## 6) Rilis: Tag, Hardening Summary, Checksum Migrasi, Kompatibilitas API

### Gate wajib sebelum release/tag RBAC

Sebelum membuat tag RBAC berikutnya, command gate security berikut **wajib hijau** (mengikuti suite terbaru):

```bash
# hard gate RBAC (wajib lulus)
mvn -Dtest=AuthorizationMatrixTest test

# gate regression tambahan untuk cakupan security + kontrak API
mvn -Dtest=AuthorizationLookupRepositoryTest,OpenApiDocumentationComplianceTest test
```

`AuthorizationMatrixTest` adalah proteksi utama untuk mencegah regression RBAC ketika frontend mulai konsumsi endpoint domain baru. Karena itu, kegagalan test ini harus memperlakukan build sebagai gagal.

### Gate CI untuk blok merge

Pipeline CI menjalankan `AuthorizationMatrixTest` sebagai **job gate terpisah** sebelum full test suite. Jika job ini gagal, job berikutnya tidak dijalankan dan status PR menjadi gagal, sehingga merge harus diblokir sampai matrix otorisasi kembali hijau.

### Tag rilis setelah merge

Setelah PR merge, buat annotated tag pada commit final RBAC:

```bash
git tag -a rbac-baseline-v1.0.0 -m "RBAC baseline v1.0.0"
git push origin rbac-baseline-v1.0.0
```

### Lampiran GitHub Release

Di halaman GitHub Release untuk tag `rbac-baseline-v1.0.0`, lampirkan:

1. **Ringkasan hardening selesai**
   - JWT-based auth + stateless security chain
   - Method-level authorization berbasis permission
   - Login throttling + account lock lifecycle
   - Audit log read permission guard

2. **Checksum migrasi DB terkait RBAC (SHA-256)**

   ```text
   003-create-roles.yaml: 2c05225e4eab82edd799930b9ca3cb2ed5baf2f51b0ac0a3cf22d78f540386b6
   004-create-permissions.yaml: f66eadacfd6ca8136857ca4bdd62214d710a82b363e6d916d5b8eab3b1d7b1f8
   005-create-user_roles.yaml: 6dc01dfa6f204c9d764d30cf96d1517e6529bb3cd79cf3946eb25fbb0eb53f4c
   006-create-role_permissions.yaml: f2604784e1eae9ec6478f021f53e18a269807e5d7f1a944c1e918569c0a324c6
   007-seed-default-rbac.yaml: 3c02fa6195d0ce7412cee97e76d091168ad7b03d352d373519a5eb7c4b747925
   008-create-audit-log.yaml: 2d6e87a5ce906572ead9d1a4a84f859dcacb56c3e96bcd99e3a650ade9250881
   009-user-lifecycle-security.yaml: dffe072fc7843edc39961b98fe008c5d517be8345d46e0712a243b002a0b41d1
   ```

3. **Kompatibilitas API endpoint security**
   - Kompatibel untuk endpoint public existing (`/api/auth/**`, actuator health/info/metrics/prometheus, OpenAPI/Swagger).
   - Endpoint selain public tetap `authenticated`.
   - Guard permission existing dipertahankan untuk controller/service yang sudah berjalan.
