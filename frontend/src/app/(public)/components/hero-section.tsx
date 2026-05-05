import Link from "next/link";

export default function HeroSection() {
  return (
    <section className="mx-auto grid w-full max-w-6xl gap-10 px-6 pt-16 pb-14 md:grid-cols-2 md:items-center">
      <div>
        <p className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
          Friendly training platform
        </p>
        <h1 className="mt-5 text-4xl font-semibold leading-tight tracking-tight text-zinc-900 sm:text-5xl">
          Manajemen kelas lebih rapi, progres siswa lebih mudah dipantau.
        </h1>
        <p className="mt-5 max-w-xl text-base leading-7 text-zinc-600">
          Buat pengalaman belajar terasa ringan untuk tim dan orang tua dengan dashboard operasional yang clean dan terstruktur.
        </p>
        <div className="mt-8 flex flex-wrap items-center gap-3">
          <Link href="/login" className="rounded-full bg-zinc-900 px-5 py-2.5 text-sm font-semibold text-white hover:bg-zinc-700">
            Coba Sekarang
          </Link>
          <a href="#program" className="rounded-full border border-zinc-300 px-5 py-2.5 text-sm font-semibold text-zinc-700 hover:bg-zinc-100">
            Lihat Program
          </a>
        </div>
      </div>
      <div className="relative overflow-hidden rounded-3xl border border-zinc-200 bg-gradient-to-br from-zinc-200 via-slate-100 to-amber-100 p-8 shadow-sm">
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
