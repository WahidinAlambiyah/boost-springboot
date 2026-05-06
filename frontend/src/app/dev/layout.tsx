import AuthBootstrap from "@/app/components/auth-bootstrap";

interface DevLayoutProps {
  children: React.ReactNode;
}

export default function DevLayout({ children }: DevLayoutProps) {
  return <AuthBootstrap>{children}</AuthBootstrap>;
}
