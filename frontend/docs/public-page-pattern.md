# Public Page Pattern

Panduan ini adalah standar membuat halaman publik baru di frontend. Halaman publik adalah halaman marketing/informasi yang tidak membutuhkan session, auth store, protected layout, atau dashboard shell.

## Cara membuat halaman public baru

1. Tentukan URL publik yang akan dibuat, misalnya `/about`, `/events`, atau `/program/pushbike`.
2. Buat file route di `src/app/{route}/page.tsx`.
3. Import dan gunakan `PublicPageLayout` sebagai wrapper halaman.
4. Ambil copy/static data dari `src/content/public` jika data sudah tersedia atau cocok dipakai ulang.
5. Pecah UI menjadi section kecil bila section akan dipakai ulang di halaman lain.
6. Tambahkan metadata halaman jika route membutuhkan title/description yang spesifik.
7. Pastikan halaman tidak mengimpor komponen khusus protected/admin seperti `AppShell`, `RequirePermission`, atau store auth.

Contoh struktur sederhana:

```text
src/app/events/page.tsx
src/app/(public)/components/event-card-section.tsx
src/content/public/events.ts
```

Aturan route:

- Route publik berada langsung di `src/app/{route}` atau segment publik yang memang tidak protected.
- Jangan tempatkan halaman publik di `src/app/(protected)`.
- Jika route bersifat nested, gunakan folder sesuai URL, misalnya `src/app/program/pushbike/page.tsx` untuk `/program/pushbike`.

## Cara memakai `PublicPageLayout`

`PublicPageLayout` sudah merender navbar dan footer publik. Page cukup mengisi konten utama sebagai `children`.

```tsx
import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function EventsPage() {
  return (
    <PublicPageLayout>
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-12 px-6 py-16">
        <section className="max-w-3xl space-y-4">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">Events</p>
          <h1 className="text-4xl font-bold tracking-tight text-foreground">
            Kegiatan dan agenda terbaru
          </h1>
          <p className="text-lg text-muted-foreground">
            Temukan agenda publik yang bisa diikuti keluarga dan komunitas.
          </p>
        </section>
      </div>
    </PublicPageLayout>
  );
}
```

Aturan layout:

- Jangan render navbar/footer manual di page karena sudah ditangani `PublicPageLayout`.
- Jaga spacing page dengan container konsisten seperti `mx-auto`, `max-w-6xl`, `px-6`, dan `py-16`.
- Hindari dependency client-only jika halaman bisa tetap server component.
- Tambahkan `"use client"` hanya jika section membutuhkan state, event handler kompleks, atau browser API.

## Cara mengambil data dari `src/content/public`

Konten publik disimpan sebagai static TypeScript data di `src/content/public`. Import data dengan alias `@/content/public/...`.

Contoh import:

```tsx
import { contactFormContent, contactPageContent } from "@/content/public/contact";
import { events } from "@/content/public/events";
import { faqGroups } from "@/content/public/faqs";
import { galleryItems } from "@/content/public/gallery";
import { heroContent } from "@/content/public/hero";
import { navItems, navigationCtas } from "@/content/public/navigation";
import { pricingPackages, pricingPageContent } from "@/content/public/pricing";
import { programOverviews } from "@/content/public/programs";
import { siteBrand, siteContent } from "@/content/public/site";
import { testimonials } from "@/content/public/testimonials";
```

Contoh pemakaian di page:

```tsx
import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { events } from "@/content/public/events";
import { siteBrand } from "@/content/public/site";

export default function EventsPage() {
  const featuredEvents = events.slice(0, 3);

  return (
    <PublicPageLayout>
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-12 px-6 py-16">
        <section className="space-y-4">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">
            {siteBrand.name}
          </p>
          <h1 className="text-4xl font-bold tracking-tight">Agenda publik</h1>
        </section>

        <section aria-labelledby="featured-events-title" className="space-y-6">
          <h2 id="featured-events-title" className="text-2xl font-semibold">
            Event pilihan
          </h2>
          <div className="grid gap-4 md:grid-cols-3">
            {featuredEvents.map((event) => (
              <article key={event.title} className="rounded-2xl border bg-card p-6 shadow-sm">
                <h3 className="text-xl font-semibold">{event.title}</h3>
                <p className="mt-3 text-sm text-muted-foreground">{event.description}</p>
                <Link className="mt-5 inline-flex font-medium text-primary hover:underline" href="/contact">
                  Tanya event ini
                </Link>
              </article>
            ))}
          </div>
        </section>
      </div>
    </PublicPageLayout>
  );
}
```

Aturan content:

- Simpan copy, list card, FAQ, CTA, testimonial, dan data statis publik di `src/content/public`.
- Jangan hardcode data berulang di banyak page jika bisa dipakai ulang dari content module.
- Page boleh melakukan filtering/slicing ringan terhadap static data.
- Jika data butuh transform besar, buat helper kecil yang tetap mudah dites dan tidak mencampur UI.

## Cara membuat reusable section

Buat reusable section ketika pola UI dipakai di lebih dari satu halaman, misalnya testimonial, FAQ, pricing card, event card, atau CTA.

Contoh section yang menerima data lewat props:

```tsx
import Link from "next/link";

type PublicEvent = {
  title: string;
  description: string;
  href?: string;
};

type EventCardSectionProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  events: PublicEvent[];
};

export default function EventCardSection({
  eyebrow,
  title,
  description,
  events,
}: EventCardSectionProps) {
  return (
    <section aria-labelledby="event-card-section-title" className="space-y-6">
      <div className="max-w-3xl space-y-2">
        {eyebrow ? (
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">{eyebrow}</p>
        ) : null}
        <h2 id="event-card-section-title" className="text-2xl font-semibold tracking-tight">
          {title}
        </h2>
        {description ? <p className="text-muted-foreground">{description}</p> : null}
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {events.map((event) => (
          <article key={event.title} className="rounded-2xl border bg-card p-6 shadow-sm">
            <h3 className="text-xl font-semibold">{event.title}</h3>
            <p className="mt-3 text-sm text-muted-foreground">{event.description}</p>
            {event.href ? (
              <Link className="mt-5 inline-flex font-medium text-primary hover:underline" href={event.href}>
                Lihat detail
              </Link>
            ) : null}
          </article>
        ))}
      </div>
    </section>
  );
}
```

Contoh pemakaian section dari page:

```tsx
import EventCardSection from "@/app/(public)/components/event-card-section";
import { events } from "@/content/public/events";

<EventCardSection
  eyebrow="Agenda"
  title="Event untuk keluarga"
  description="Ikuti kegiatan publik yang sesuai untuk anak dan orang tua."
  events={events.map((event) => ({
    title: event.title,
    description: event.description,
    href: "/contact",
  }))}
/>;
```

Aturan reusable section:

- Section menerima data lewat props, bukan import content langsung, agar mudah dipakai ulang dan dites.
- Gunakan tipe props yang kecil dan sesuai kebutuhan section.
- Jaga heading hierarchy: page punya satu `h1`, reusable section biasanya memakai `h2` dan card memakai `h3`.
- Hindari dependency auth, React Query admin, atau store protected.
- Jika section butuh interaksi client-side, jadikan hanya section itu client component, bukan seluruh page.

## Checklist SEO dasar

Sebelum merge halaman publik baru, pastikan:

- [ ] Halaman memiliki tepat satu `h1` yang menjelaskan topik utama.
- [ ] Metadata `title` dan `description` relevan, unik, dan tidak terlalu generik.
- [ ] Struktur heading berurutan (`h1` lalu `h2`, card/detail memakai `h3` bila perlu).
- [ ] Copy utama menjawab intent pengunjung dalam paragraf awal.
- [ ] Link internal mengarah ke halaman relevan dan memakai anchor text yang deskriptif.
- [ ] CTA utama terlihat tanpa harus mencari terlalu jauh, terutama di mobile.
- [ ] Gambar memakai `alt` yang mendeskripsikan isi gambar; decorative image boleh memakai `alt=""`.
- [ ] Placeholder visual memiliki label atau konteks yang accessible.
- [ ] Section penting memakai landmark/`aria-labelledby` bila membantu navigasi screen reader.
- [ ] Tidak ada dependency auth/protected/admin di halaman publik.
- [ ] Konten statis yang reusable disimpan di `src/content/public`.
- [ ] Halaman sudah dicek responsive untuk mobile, tablet, dan desktop.
