# Protected Pages Guardrails

Dokumen ini menetapkan guardrails untuk perubahan halaman protected agar tetap backward-compatible.

## Prinsip utama

- Tetap gunakan `api` dari `src/lib/api.ts` untuk seluruh akses API pada flow frontend.
- Jangan mengubah flow auth store yang sudah ada, kecuali penyesuaian minor yang dibutuhkan untuk compatibility type.
- Jangan menghapus atau merusak halaman existing: `login`, `dashboard`, dan `catalog`.
- Tetap gunakan `RequirePermission` dan `AppShell` sebagai pola utama untuk protected pages.

## Aturan perubahan

Jika diperlukan penyesuaian minor import/type:

- lakukan secara additive,
- jaga backward-compatibility,
- hindari perubahan perilaku runtime pada flow autentikasi.
