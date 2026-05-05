import Link from "next/link";

export default function HeroSection() {
  return (
    <section className="layout-container section-space grid gap-10 md:grid-cols-2 md:items-center">
      <div>
        <p className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
          Program Aktivitas Anak
        </p>
        <h1 className="mt-5 text-3xl font-semibold leading-tight tracking-tight text-zinc-900 sm:text-4xl lg:text-5xl">
          Kelas Pushbike & Aktivitas Anak yang Aman, Terarah, dan Menyenangkan
        </h1>
        <p className="text-body mt-5 max-w-xl font-normal">
          Bantu anak melatih keseimbangan, keberanian, fokus, dan kepercayaan diri melalui latihan pushbike, kegiatan belajar, dan event anak yang dipantau langsung oleh coach.
        </p>
        <div className="mt-8 flex flex-wrap items-center gap-3">
          <Link href="/login" className="rounded-full bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-emerald-500">
            Daftar Trial Class
          </Link>
          <a href="#program" className="rounded-full bg-zinc-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-zinc-700">
            Lihat Jadwal Kegiatan
          </a>
          <a href="https://wa.me/6280000000000" className="rounded-full border border-zinc-300 bg-transparent px-5 py-2.5 text-sm font-semibold text-zinc-700 transition hover:bg-zinc-100">
            Konsultasi via WhatsApp
          </a>
        </div>
      </div>
      <div className="surface-card relative overflow-hidden rounded-3xl bg-gradient-to-br from-zinc-100 via-slate-100 to-emerald-100/80 p-7 sm:p-8">
        <div className="space-y-4">
          <div className="h-24 rounded-2xl bg-gradient-to-r from-emerald-200 to-emerald-100" />
          <div className="grid grid-cols-2 gap-4">
            <div className="h-20 rounded-2xl bg-gradient-to-r from-slate-200 to-zinc-100" />
            <div className="h-20 rounded-2xl bg-gradient-to-r from-amber-200 to-amber-100" />
          </div>
        </div>
      </div>
    </section>
  );
}
