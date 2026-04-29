import Sidebar from "@/app/components/sidebar";
import Topbar from "@/app/components/topbar";
import HttpDebugDrawer from "@/app/components/http-debug-drawer";

interface AppShellProps {
  children: React.ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  return (
    <div className="min-h-screen bg-zinc-50">
      <HttpDebugDrawer />
      <Topbar />
      <div className="flex min-h-[calc(100vh-4rem)]">
        <Sidebar />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
