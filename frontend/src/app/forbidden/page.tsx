import Link from "next/link";

export default function ForbiddenPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6 text-center">
      <p className="text-6xl font-bold text-zinc-900">403</p>
      <h1 className="mt-3 text-2xl font-semibold text-zinc-900">Forbidden</h1>
      <p className="mt-2 max-w-md text-zinc-600">
        Anda tidak memiliki role atau permission yang dibutuhkan untuk mengakses halaman ini.
      </p>
      <Link
        href="/dashboard"
        className="mt-4 inline-flex rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-700 hover:bg-zinc-100"
      >
        Kembali ke Dashboard
      </Link>
    </main>
  );
}
