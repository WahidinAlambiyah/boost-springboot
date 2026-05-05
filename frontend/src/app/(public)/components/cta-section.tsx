import Link from "next/link";

export default function CtaSection() {
  return (
    <section className="layout-container section-space">
      <div className="overflow-hidden rounded-3xl border border-zinc-700/40 bg-gradient-to-r from-slate-900 via-zinc-900 to-emerald-900 p-7 sm:p-10">
        <h2 className="text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          Siap mulai aktivitas anak yang aman, terarah, dan menyenangkan?
        </h2>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-zinc-200">
          Pilih sesi trial, lihat jadwal kegiatan, dan diskusikan kebutuhan anak bersama coach kami.
        </p>
        <div className="mt-7 flex flex-wrap gap-3">
          <Link href="/login" className="rounded-full bg-emerald-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-emerald-400">
            Daftar Trial Class
          </Link>
          <a href="#program" className="rounded-full bg-zinc-700 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-zinc-600">
            Lihat Jadwal Kegiatan
          </a>
          <a href="https://wa.me/6280000000000" className="rounded-full border border-white/60 bg-transparent px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-white/10">
            Konsultasi via WhatsApp
          </a>
        </div>
      </div>
    </section>
  );
}
