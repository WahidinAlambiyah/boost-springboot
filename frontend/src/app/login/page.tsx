export default function LoginPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6">
      <div className="w-full max-w-sm rounded-lg border border-zinc-200 bg-white p-6 shadow-sm">
        <h1 className="text-xl font-semibold text-zinc-900">Login</h1>
        <p className="mt-2 text-sm text-zinc-600">
          Session expired or you are not authenticated. Please login from your backend-integrated form.
        </p>
      </div>
    </main>
  );
}
