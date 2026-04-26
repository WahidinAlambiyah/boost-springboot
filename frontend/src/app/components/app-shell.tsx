import NavigationSidebar from "@/app/components/navigation-sidebar";
import Topbar from "@/app/components/topbar";

interface AppShellProps {
  children: React.ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  return (
    <div className="min-h-screen bg-zinc-50">
      <Topbar />
      <div className="flex min-h-[calc(100vh-4rem)]">
        <NavigationSidebar />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
