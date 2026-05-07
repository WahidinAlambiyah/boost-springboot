import AuthBootstrap from "@/app/components/auth-bootstrap";
import RequirePermission from "@/app/components/require-permission";

interface DevLayoutProps {
  children: React.ReactNode;
}

export default function DevLayout({ children }: DevLayoutProps) {
  return (
    <AuthBootstrap>
      {/* TODO: remove ROLE_READ/USER_READ fallback when DEV_TOOLS_READ is managed by backend. */}
      <RequirePermission permissions={["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"]} mode="any">
        {children}
      </RequirePermission>
    </AuthBootstrap>
  );
}
