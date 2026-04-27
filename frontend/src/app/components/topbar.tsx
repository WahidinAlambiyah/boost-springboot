"use client";

import { useRouter } from "next/navigation";

import { logoutSession } from "@/lib/api";
import { useAuthStore } from "@/store/auth";

export default function Topbar() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const clearSession = useAuthStore((state) => state.clearSession);

  const handleLogout = async () => {
    await logoutSession();
    clearSession();
    router.replace("/login");
  };

  return (
    <header className="flex h-16 items-center justify-between border-b border-zinc-200 bg-white px-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Boost</p>
        <p className="text-lg font-semibold text-zinc-900">Admin Console</p>
      </div>

      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-medium text-zinc-900">{user?.username ?? "Unknown User"}</p>
          <p className="text-xs text-zinc-500">{user?.email ?? "No email"}</p>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-700 transition-colors hover:bg-zinc-100"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
