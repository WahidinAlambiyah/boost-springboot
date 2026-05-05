# Frontend Public Website dan Admin Dashboard Plan

## 1. Ringkasan struktur saat ini

Struktur frontend saat ini sudah terorganisasi dengan pemisahan concern yang cukup jelas:

- `src/app`
  - Root App Router untuk seluruh halaman, layout global, dan entrypoint route.
  - Saat ini route utama berada di route group `(auth)` dan `(protected)`.
- `src/app/(auth)`
  - Menampung halaman autentikasi (contoh: `login`) beserta layout khusus auth.
  - Digunakan untuk flow akses sebelum user terautentikasi.
- `src/app/(protected)`
  - Menampung halaman dashboard/admin yang membutuhkan sesi login dan otorisasi.
  - Sudah menggunakan pola layout terproteksi sebagai fondasi area internal.
- `src/app/components`
  - Komponen UI/shared yang dipakai lintas halaman (shell, navigasi, feedback, table, dsb).
- `src/features`
  - Domain modules per fitur (students, scheduling, attendance, payroll, assessment, dll), biasanya berisi service, schema, dan komponen spesifik domain.
- `src/lib`
  - Utilitas lintas aplikasi: API client, permission mapping, formatter, guard/helper, query provider, dan helper teknis lain.
- `src/store`
  - State management global (khususnya auth/session store) yang dipakai untuk kontrol akses dan bootstrap sesi.
- `src/types`
  - Definisi type/interface bersama untuk kontrak data agar konsisten antar fitur.

## 2. Route existing

### Public/Auth-oriented routes (existing)

- `/login` (di route group `(auth)`)
- `/forbidden`

### Protected/Admin routes (existing, di `(protected)`)

Contoh route yang saat ini sudah aktif:

- `/dashboard`
- `/admin`
- `/academies`
- `/academy-locations`
- `/students`
- `/students/[studentId]/packages`
- `/class-sessions`
- `/class-sessions/[id]`
- `/scheduling`
- `/attendance`
- `/attendance/sessions/[classSessionId]`
- `/assessments`
- `/assessments/new`
- `/assessments/[id]/edit`
- `/assessment-skills`
- `/catalog`
- `/training-packages`
- `/enrollment`
- `/coach-profiles`
- `/payroll/coach`
- `/billing`
- `/reports/student-progress`
- `/notification`

### Root route saat ini

- `/` saat ini melakukan redirect ke `/dashboard`.

## 3. Rencana route public website

Rencana penambahan route untuk website publik (marketing/informational):

- `/` → landing page public
- `/programs`
- `/coaches`
- `/locations`
- `/contact`

Catatan penting:

- Route public di atas **harus tidak tergantung auth store**.
- Rendering, data fetching, dan interaksi pada route public tidak boleh mewajibkan user login.
- Route public harus bisa diakses anonymous tanpa trigger redirect ke area admin.

## 4. Rencana route admin dashboard

Area admin tetap dipertahankan di route group `(protected)` dengan pola yang sama:

- Tetap menggunakan `AppShell` untuk struktur layout admin.
- Tetap menggunakan `RequirePermission` untuk enforcement authorization per halaman/aksi.

Pengelompokan menu admin per domain (high-level):

- **Master data**
  - academies, academy locations, coach profiles, students, training packages, assessment skills
- **Scheduling**
  - class sessions, scheduling
- **Attendance**
  - attendance dan attendance session detail
- **Assessment**
  - assessments dan alur scoring/edit
- **Payroll**
  - payroll coach (+ dapat diperluas ke billing/finance terkait)

## 5. Komponen reusable

Komponen/utilitas reusable yang dipertahankan dan menjadi fondasi rollout:

- Layout & akses:
  - `AppShell`
  - `Sidebar`
  - `Topbar`
  - `RequirePermission`
- UI primitives/pattern:
  - `page-header`
  - `data-table`
  - `form-field`
  - `status-badge`
  - `error-message`
  - `confirm-dialog`
- Data/util layer:
  - `query-provider`
  - API client
  - `formatters`

## 6. Risiko perubahan

Risiko utama yang perlu diawasi saat memperkenalkan public website:

- Perubahan behavior root redirect dari `/ -> /dashboard`.
- Perbedaan sumber menu navigasi (fallback menu lokal vs menu dari backend).
- Potensi regresi auth redirect (anonymous/user login) akibat perubahan segment route.
- Metadata/SEO default berpotensi tidak sesuai untuk halaman public bila masih berorientasi dashboard.

## 7. Mitigasi dan rollout

Strategi mitigasi dan rollout bertahap:

- Lakukan perubahan root secara bertahap (incremental), hindari perubahan big-bang.
- Pertahankan semua route existing agar kompatibilitas internal tidak terganggu.
- Lakukan smoke test minimal untuk alur:
  - login
  - dashboard
  - catalog

Catatan penutup:

- Dokumen ini bersifat **planning**.
- Perubahan yang direncanakan **tidak mengubah auth flow** saat ini.
- Perubahan yang direncanakan **tidak menghapus route existing**.
