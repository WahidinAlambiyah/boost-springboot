import Link from "next/link";

export default function LoginPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6">
      <div className="w-full max-w-sm rounded-lg border border-zinc-200 bg-white p-6 shadow-sm">
        <h1 className="text-xl font-semibold text-zinc-900">Login</h1>
        <p className="mt-2 text-sm text-zinc-600">
          Session expired atau Anda belum terautentikasi. Silakan login melalui form autentikasi backend.
        </p>
        <Link
          href="/dashboard"
          className="mt-4 inline-flex rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-700 hover:bg-zinc-100"
        >
          Coba ke Dashboard
        </Link>
      </div>
    </main>
  );
}
