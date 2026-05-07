import { notFound } from "next/navigation";

import AuthBootstrap from "@/app/components/auth-bootstrap";
import RequirePermission from "@/app/components/require-permission";
import { ENABLE_DEV_TOOLS } from "@/lib/dev-tools";

interface DevLayoutProps {
  children: React.ReactNode;
}

export default function DevLayout({ children }: DevLayoutProps) {
  if (!ENABLE_DEV_TOOLS) {
    notFound();
  }

  return (
    <AuthBootstrap>
      {/* TODO: remove ROLE_READ/USER_READ fallback when DEV_TOOLS_READ is managed by backend. */}
      <RequirePermission permissions={["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"]} mode="any">
        {children}
      </RequirePermission>
    </AuthBootstrap>
  );
}
