import Link from "next/link";

export default function HeroSection() {
  return (
    <section className="mx-auto w-full max-w-6xl px-6 pt-20 pb-14 text-center">
      <p className="text-sm font-medium uppercase tracking-wide text-zinc-500">Boost</p>
      <h1 className="mt-3 text-4xl font-semibold text-zinc-900 sm:text-5xl">
        Platform Manajemen Kelas
      </h1>
      <p className="mx-auto mt-4 max-w-2xl text-zinc-600">
        Kelola administrasi akademi, pembelajaran, kehadiran, hingga pelaporan dalam satu aplikasi
        operasional yang terintegrasi.
      </p>
      <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
        <Link
          href="/login"
          className="inline-flex rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Login
        </Link>
        <Link
          href="/dashboard"
          className="inline-flex rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
        >
          Buka Dashboard
        </Link>
      </div>
    </section>
  );
}
