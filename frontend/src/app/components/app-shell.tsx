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
  const mainContentMargin = isSidebarCollapsed ? "lg:ml-[90px]" : "lg:ml-[290px]";

  return (
    <div className="min-h-screen bg-gray-50 xl:flex">
      <HttpDebugDrawer />
      <Sidebar collapsed={isSidebarCollapsed} />
      <div className={`flex-1 transition-all duration-300 ease-in-out ${mainContentMargin}`}>
        <Topbar
          isSidebarCollapsed={isSidebarCollapsed}
          onToggleSidebar={() => setIsSidebarCollapsed((currentValue) => !currentValue)}
        />
        <main className="mx-auto min-w-0 max-w-screen-2xl p-4 md:p-6">{children}</main>
      </div>
    </div>
  );
}
