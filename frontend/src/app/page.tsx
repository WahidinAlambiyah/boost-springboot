"use client";

import NavigationSidebar from "@/app/components/navigation-sidebar";
import { can, canAll, canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

export default function Home() {
  const user = useAuthStore((state) => state.user);
  const authorities = useAuthStore((state) => state.authorities);

  return (
    <div className="flex min-h-screen bg-zinc-50">
      <NavigationSidebar />
      <main className="flex-1 p-6">
        <h1 className="text-2xl font-semibold text-zinc-900">Dashboard</h1>
        <p className="mt-1 text-zinc-600">Welcome, {user?.username ?? "-"}</p>

        <div className="mt-6 grid gap-4 md:grid-cols-3">
          <StatusCard title="can('users:read')" value={String(can(authorities, "users:read"))} />
          <StatusCard
            title="canAny(['audit:read', 'audit:export'])"
            value={String(canAny(authorities, ["audit:read", "audit:export"]))}
          />
          <StatusCard
            title="canAll(['settings:read', 'settings:update'])"
            value={String(canAll(authorities, ["settings:read", "settings:update"]))}
          />
        </div>
      </main>
    </div>
  );
}

interface StatusCardProps {
  title: string;
  value: string;
}

function StatusCard({ title, value }: StatusCardProps) {
  return (
    <article className="rounded-lg border border-zinc-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-zinc-500">{title}</p>
      <p className="mt-2 text-lg font-semibold text-zinc-900">{value}</p>
    </article>
  );
}
