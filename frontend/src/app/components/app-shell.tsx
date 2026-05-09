"use client";

import { useState, type ReactNode } from "react";

import Sidebar from "@/app/components/sidebar";
import Topbar from "@/app/components/topbar";
import HttpDebugDrawer from "@/app/components/http-debug-drawer";

interface AppShellProps {
  children: ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

  return (
    <div className="min-h-screen bg-zinc-50">
      <HttpDebugDrawer />
      <Topbar
        isSidebarCollapsed={isSidebarCollapsed}
        onToggleSidebar={() => setIsSidebarCollapsed((currentValue) => !currentValue)}
      />
      <div className="flex min-h-[calc(100vh-4rem)]">
        <Sidebar collapsed={isSidebarCollapsed} />
        <main className="min-w-0 flex-1 p-6 transition-all duration-200">{children}</main>
      </div>
    </div>
  );
}
