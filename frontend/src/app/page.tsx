import Link from "next/link";

export default function HomePage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6 text-center">
      <p className="text-sm font-medium uppercase tracking-wide text-zinc-500">Boost</p>
      <h1 className="mt-3 text-4xl font-semibold text-zinc-900">Platform Manajemen Kelas</h1>
      <p className="mt-4 max-w-2xl text-zinc-600">
        Selamat datang di aplikasi operasional Boost. Silakan login untuk mengakses dashboard,
        atau langsung buka portal admin jika Anda memiliki hak akses.
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
        <Link
          href="/admin"
          className="inline-flex rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
        >
          Portal Admin
        </Link>
      </div>
    </main>
  );
}
