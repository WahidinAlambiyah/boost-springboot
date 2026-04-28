"use client";

import { useRouter } from "next/navigation";
import { useEffect, useMemo } from "react";

import { can, canAll, canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

interface RequirePermissionProps {
  children: React.ReactNode;
  permissions: string | string[];
  mode?: "single" | "any" | "all";
}

export default function RequirePermission({
  children,
  permissions,
  mode = "single",
}: RequirePermissionProps) {
  const router = useRouter();
  const status = useAuthStore((state) => state.status);
  const authorities = useAuthStore((state) => state.authorities);

  const normalizedPermissions = useMemo(
    () => (Array.isArray(permissions) ? permissions : [permissions]),
    [permissions],
  );

  const isAllowed = useMemo(() => {
    if (normalizedPermissions.length === 0) {
      return true;
    }

    if (mode === "all") {
      return canAll(authorities, normalizedPermissions);
    }

    if (mode === "any") {
      return canAny(authorities, normalizedPermissions);
    }

    return can(authorities, normalizedPermissions[0]);
  }, [authorities, mode, normalizedPermissions]);

  useEffect(() => {
    if (status !== "authenticated") {
      return;
    }

    if (!isAllowed) {
      router.replace("/forbidden");
    }
  }, [isAllowed, router, status]);

  if (status !== "authenticated") {
    return null;
  }

  if (!isAllowed) {
    return null;
  }

  return <>{children}</>;
}
