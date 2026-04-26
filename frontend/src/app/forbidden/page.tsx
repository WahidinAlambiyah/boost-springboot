export default function ForbiddenPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6 text-center">
      <p className="text-6xl font-bold text-zinc-900">403</p>
      <h1 className="mt-3 text-2xl font-semibold text-zinc-900">Forbidden</h1>
      <p className="mt-2 max-w-md text-zinc-600">
        You do not have permission to access this page.
      </p>
    </main>
  );
}
