"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { ADMIN_PERMISSION_BUNDLE, hasAdminAccess } from "@/lib/admin-guard";
import { useAuthStore } from "@/store/auth";

export default function AdminPage() {
  const router = useRouter();
  const status = useAuthStore((state) => state.status);
  const authorities = useAuthStore((state) => state.authorities);

  const isAllowed = hasAdminAccess(authorities);

  useEffect(() => {
    if (status !== "authenticated") {
      return;
    }

    if (!isAllowed) {
      router.replace("/forbidden");
    }
  }, [isAllowed, router, status]);

  if (status !== "authenticated" || !isAllowed) {
    return null;
  }

  return (
    <main className="flex-1 p-6">
      <h1 className="text-2xl font-semibold">Admin Area</h1>
      <p className="mt-2 text-zinc-600">
        Halaman ini hanya untuk pengguna dengan ROLE_ADMIN, atau kombinasi permission admin: {ADMIN_PERMISSION_BUNDLE.join(", ")}.
      </p>
    </main>
  );
}
