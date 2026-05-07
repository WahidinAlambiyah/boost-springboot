# Public Page Pattern

## Catatan Scope Dokumentasi

Panduan ini hanya menjelaskan pola implementasi untuk developer dan tidak mengubah runtime aplikasi.

## Cara membuat halaman publik baru

Untuk menambahkan halaman publik baru:

- Buat route di `frontend/src/app/{route}/page.tsx`, misalnya `frontend/src/app/about/page.tsx` atau `frontend/src/app/events/page.tsx`.
- Gunakan `PublicPageLayout` dari `frontend/src/app/(public)/components/public-page-layout.tsx` sebagai pembungkus halaman.
- Jangan memakai dependency auth, protected layout, dashboard shell, atau komponen yang bergantung pada state/session dashboard.

## Cara memakai navbar/footer

`PublicPageLayout` sudah merender `PublicNavbar` dan `PublicFooter`, sehingga halaman publik tidak perlu mengimpor atau merender navbar/footer secara manual.

Halaman cukup mengisi content utama di dalam `PublicPageLayout`. Fokuskan file `page.tsx` pada struktur halaman, copywriting, section, dan CTA yang relevan.

## Cara mengambil data dari `src/content/public`

Konten publik disimpan sebagai static TypeScript data di `frontend/src/content/public`. Gunakan import alias `@/content/public/...` agar konsisten dengan kode frontend.

Contoh import informasi situs:

```tsx
import { siteContent } from "@/content/public/site";
```

Contoh import konten publik lain:

```tsx
import { programs } from "@/content/public/programs";
import { events } from "@/content/public/events";
import { galleryItems } from "@/content/public/gallery";
import { faqGroups } from "@/content/public/faqs";
import { testimonials } from "@/content/public/testimonials";
```

Pilih data yang memang dibutuhkan oleh halaman. Jangan memindahkan static content ke komponen jika data tersebut bisa tetap berada di `src/content/public`.

## Membuat section reusable

Saat membuat section yang akan dipakai ulang:

- Buat komponen section yang menerima data via props, bukan membaca data langsung dari module content.
- Gunakan container, grid, gap, padding, dan spacing yang konsisten dengan section publik lain.
- Biarkan data tetap static TypeScript di `frontend/src/content/public`, lalu oper data tersebut dari page atau parent section.
- Pastikan komponen reusable tidak bergantung pada auth/protected dashboard dependency.

## Checklist SEO dasar

Sebelum merge halaman publik baru, cek hal berikut:

- Satu `h1` per halaman.
- `title` dan `description` masuk akal untuk halaman tersebut.
- Link internal jelas dan mengarah ke halaman yang relevan.
- Anchor/link punya accessible label yang mudah dipahami.
- CTA terlihat dan mudah diklik di mobile.
- Image atau placeholder punya label atau `alt` yang relevan.

## Contoh kode minimal halaman publik

```tsx
import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { programOverviews } from "@/content/public/programs";
import { siteBrand } from "@/content/public/site";

export default function ExamplePublicPage() {
  return (
    <PublicPageLayout>
      <div className="container mx-auto flex w-full max-w-6xl flex-col gap-12 px-6 py-16">
        <section className="max-w-3xl space-y-4">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">
            {siteBrand.name}
          </p>
          <h1 className="text-4xl font-bold tracking-tight text-foreground">
            Program publik untuk keluarga aktif
          </h1>
          <p className="text-lg text-muted-foreground">
            Pilih program yang sesuai untuk anak dan mulai perjalanan belajar
            dengan pengalaman yang aman, menyenangkan, dan terarah.
          </p>
        </section>

        <section aria-labelledby="program-list-title" className="space-y-6">
          <div className="space-y-2">
            <h2 id="program-list-title" className="text-2xl font-semibold">
              Program tersedia
            </h2>
            <p className="text-muted-foreground">
              Ringkasan program yang bisa ditampilkan sebagai card publik.
            </p>
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            {programOverviews.map((program) => (
              <article
                key={program.slug}
                className="rounded-2xl border bg-card p-6 shadow-sm"
              >
                <h3 className="text-xl font-semibold">{program.title}</h3>
                <p className="mt-3 text-sm text-muted-foreground">
                  {program.description}
                </p>
                <Link
                  href={`/program/${program.slug}`}
                  className="mt-5 inline-flex font-medium text-primary hover:underline"
                  aria-label={`Lihat detail program ${program.title}`}
                >
                  Lihat detail
                </Link>
              </article>
            ))}
          </div>
        </section>

        <section className="rounded-3xl bg-primary p-8 text-primary-foreground">
          <h2 className="text-2xl font-semibold">Siap mencoba?</h2>
          <p className="mt-2 max-w-2xl">
            Jadwalkan trial dan temukan kelas yang paling sesuai untuk anak.
          </p>
          <Link
            href="/trial"
            className="mt-6 inline-flex rounded-full bg-background px-5 py-3 font-semibold text-foreground"
          >
            Daftar trial
          </Link>
        </section>
      </div>
    </PublicPageLayout>
  );
}
```
