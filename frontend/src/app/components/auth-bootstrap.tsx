"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { useAuthStore } from "@/store/auth";

interface AuthBootstrapProps {
  children: React.ReactNode;
}

export default function AuthBootstrap({ children }: AuthBootstrapProps) {
  const router = useRouter();
  const pathname = usePathname();
  const status = useAuthStore((state) => state.status);
  const initializeSession = useAuthStore((state) => state.initializeSession);
  const [bootstrapped, setBootstrapped] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const bootstrap = async () => {
      const success = await initializeSession();

      if (!isMounted) {
        return;
      }

      if (!success && pathname !== "/login") {
        router.replace("/login");
      }

      setBootstrapped(true);
    };

    void bootstrap();

    return () => {
      isMounted = false;
    };
  }, [initializeSession, pathname, router]);

  if (pathname === "/login") {
    return <>{children}</>;
  }

  if (!bootstrapped || status === "loading") {
    return <div className="flex min-h-screen items-center justify-center">Loading session...</div>;
  }

  return <>{children}</>;
}
