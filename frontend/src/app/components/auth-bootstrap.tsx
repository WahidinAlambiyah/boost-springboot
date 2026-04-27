"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

interface AuthBootstrapProps {
  children: React.ReactNode;
}

const publicRoutes = new Set(["/login", "/forbidden"]);

export default function AuthBootstrap({ children }: AuthBootstrapProps) {
  const router = useRouter();
  const pathname = usePathname();
  const status = useAuthStore((state) => state.status);
  const authorities = useAuthStore((state) => state.authorities);
  const [bootstrapped, setBootstrapped] = useState(false);

  const isPublicRoute = useMemo(() => publicRoutes.has(pathname), [pathname]);

  useEffect(() => {
    let isMounted = true;

    const bootstrap = async () => {
      await bootstrapSession();

      if (!isMounted) {
        return;
      }

      setBootstrapped(true);
    };

    void bootstrap();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!bootstrapped) {
      return;
    }

    if (status === "unauthenticated" && !isPublicRoute) {
      router.replace("/login");
    }

    if (status === "authenticated" && pathname === "/login") {
      router.replace("/dashboard");
    }
  }, [bootstrapped, isPublicRoute, pathname, router, status]);

  useEffect(() => {
    if (!bootstrapped || status !== "authenticated") {
      return;
    }

    if (pathname.startsWith("/admin") && !can(authorities, "admin:access")) {
      router.replace("/forbidden");
    }
  }, [authorities, bootstrapped, pathname, router, status]);

  if (isPublicRoute) {
    return <>{children}</>;
  }

  if (!bootstrapped || status === "loading") {
    return <div className="flex min-h-screen items-center justify-center">Loading session...</div>;
  }

  if (status === "unauthenticated") {
    return null;
  }

  return <>{children}</>;
}
