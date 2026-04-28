"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { useAuthStore } from "@/store/auth";

interface ProtectedLayoutProps {
  children: React.ReactNode;
}

function ProtectedLayoutSkeleton() {
  return (
    <div className="min-h-screen bg-zinc-50 p-6">
      <div className="mx-auto w-full max-w-6xl animate-pulse space-y-4">
        <div className="h-10 w-full rounded-md bg-zinc-200" />
        <div className="grid gap-4 md:grid-cols-[16rem,1fr]">
          <div className="h-[70vh] rounded-md bg-zinc-200" />
          <div className="space-y-3">
            <div className="h-10 w-1/2 rounded-md bg-zinc-200" />
            <div className="h-48 rounded-md bg-zinc-200" />
            <div className="h-48 rounded-md bg-zinc-200" />
          </div>
        </div>
      </div>
    </div>
  );
}

export default function ProtectedLayout({ children }: ProtectedLayoutProps) {
  const router = useRouter();
  const status = useAuthStore((state) => state.status);
  const [bootstrapped, setBootstrapped] = useState(false);

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

    if (status === "unauthenticated") {
      router.replace("/login");
    }
  }, [bootstrapped, router, status]);

  if (!bootstrapped || status === "loading") {
    return <ProtectedLayoutSkeleton />;
  }

  if (status === "unauthenticated") {
    return null;
  }

  return <>{children}</>;
}
