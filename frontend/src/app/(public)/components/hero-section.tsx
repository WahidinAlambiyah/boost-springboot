import Link from "next/link";

import { createWhatsAppLink } from "@/lib/whatsapp";

export default function HeroSection() {
  return (
    <section className="layout-container section-space grid gap-10 py-12 md:grid-cols-2 md:items-center md:py-16 lg:py-20">
      <div>
        <p className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
          Pushbike & Aktivitas Anak
        </p>
        <h1 className="mt-5 text-3xl font-semibold leading-tight tracking-tight text-zinc-900 sm:text-4xl lg:text-5xl">
          Tempat Anak Belajar Berani, Fokus, dan Percaya Diri Lewat Kelas Pushbike
        </h1>
        <p className="text-body mt-5 max-w-xl text-base">
          Program terstruktur untuk usia 2–7 tahun dengan coach berpengalaman, suasana fun learning, dan ringkasan progress yang mudah dipahami orang tua.
        </p>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center">
          <Link href="/trial" className="rounded-full bg-emerald-600 px-5 py-3 text-center text-sm font-semibold text-white transition hover:bg-emerald-500">
            Daftar Trial Class
          </Link>
          <a href={createWhatsAppLink()} className="rounded-full border border-zinc-300 bg-white px-5 py-3 text-center text-sm font-semibold text-zinc-700 transition hover:bg-zinc-100">
            Konsultasi via WhatsApp
          </a>
          <a href="#alur-trial" className="rounded-full bg-zinc-800 px-5 py-3 text-center text-sm font-semibold text-white transition hover:bg-zinc-700">
            Lihat Alur Trial
          </a>
        </div>
      </div>

      <div className="surface-card relative overflow-hidden rounded-3xl border border-zinc-200/70 bg-gradient-to-br from-emerald-100 via-white to-slate-100 p-7 shadow-sm sm:p-8">
        <div className="space-y-4">
          <div className="h-24 rounded-2xl bg-gradient-to-r from-emerald-300/80 to-emerald-100" />
          <div className="grid grid-cols-2 gap-4">
            <div className="h-20 rounded-2xl bg-gradient-to-r from-slate-200 to-zinc-100" />
            <div className="h-20 rounded-2xl bg-gradient-to-r from-amber-200 to-amber-100" />
          </div>
          <div className="rounded-xl bg-white/90 p-4 text-sm text-zinc-700 shadow-sm">
            <p className="font-semibold text-zinc-900">Ringkasan progress mingguan</p>
            <p className="mt-1">Coach mencatat kehadiran, skill, fokus, dan rekomendasi latihan selanjutnya.</p>
          </div>
        </div>
      </div>
    </section>
  );
}
