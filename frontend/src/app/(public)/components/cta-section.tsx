import Link from "next/link";

export default function CtaSection() {
  return (
    <section className="mx-auto w-full max-w-6xl px-6 py-16">
      <div className="overflow-hidden rounded-3xl border border-zinc-200 bg-gradient-to-r from-slate-900 via-zinc-900 to-emerald-900 p-8 sm:p-10">
        <h2 className="text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          Siap bikin latihan jadi lebih terukur?
        </h2>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-zinc-200">
          Mulai dari jadwal, kehadiran, sampai progres. Semua dalam satu sistem yang ramah untuk tim dan orang tua.
        </p>
        <div className="mt-7 flex flex-wrap gap-3">
          <Link href="/login" className="rounded-full bg-white px-5 py-2.5 text-sm font-semibold text-zinc-900 hover:bg-zinc-100">
            Mulai Gratis
          </Link>
          <a href="#faq" className="rounded-full border border-white/50 px-5 py-2.5 text-sm font-semibold text-white hover:bg-white/10">
            Tanya Jawab
          </a>
        </div>
      </div>
    </section>
  );
}
