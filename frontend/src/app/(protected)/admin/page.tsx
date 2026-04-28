import RequirePermission from "@/app/components/require-permission";

export default function AdminPage() {
  return (
    <RequirePermission permissions="admin:access">
      <main className="flex-1 p-6">
        <h1 className="text-2xl font-semibold">Admin Area</h1>
        <p className="mt-2 text-zinc-600">Only users with admin:access permission can stay on this page.</p>
      </main>
    </RequirePermission>
  );
}
