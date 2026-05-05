import Link from "next/link";

export default function CtaSection() {
  return (
    <section className="border-t border-zinc-200 bg-white">
      <div className="mx-auto flex w-full max-w-6xl flex-col items-start gap-4 px-6 py-12 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-semibold text-zinc-900">Siap mulai operasional lebih rapi?</h2>
          <p className="mt-1 text-sm text-zinc-600">
            Masuk ke akun Anda untuk lanjut ke dashboard dan modul internal.
          </p>
        </div>
        <Link
          href="/login"
          className="inline-flex rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Masuk Sekarang
        </Link>
      </div>
    </section>
  );
}
