"use client";

import { useRouter } from "next/navigation";

import {
  CloseIcon,
  HamburgerIcon,
  TailAdminNotificationDropdown,
  TailAdminUserDropdown,
} from "@/app/components/tailadmin";
import { logoutSession } from "@/lib/api";
import { useAuthStore } from "@/store/auth";

interface TopbarProps {
  isSidebarCollapsed?: boolean;
  onToggleSidebar?: () => void;
}

export default function Topbar({ isSidebarCollapsed = false, onToggleSidebar }: TopbarProps) {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const clearSession = useAuthStore((state) => state.clearSession);

  const handleLogout = async () => {
    await logoutSession();
    clearSession();
    router.replace("/login");
  };

  return (
    <header className="sticky top-0 z-40 flex w-full border-b border-gray-200 bg-white">
      <div className="flex grow items-center justify-between gap-3 px-3 py-3 sm:gap-4 lg:px-6 lg:py-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onToggleSidebar}
            aria-label={isSidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-expanded={!isSidebarCollapsed}
            className="z-40 inline-flex h-11 w-11 items-center justify-center rounded-lg border border-gray-200 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-300"
          >
            <span className="sr-only">{isSidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}</span>
            {isSidebarCollapsed ? <HamburgerIcon /> : <CloseIcon className="h-6 w-6" />}
          </button>

          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Boost</p>
            <p className="text-base font-semibold text-gray-900 sm:text-lg">Admin Console</p>
          </div>
        </div>

        <div className="flex items-center gap-2 sm:gap-3">
          <TailAdminNotificationDropdown />
          <TailAdminUserDropdown username={user?.username} email={user?.email} onLogout={handleLogout} />
        </div>
      </div>
    </header>
  );
}
