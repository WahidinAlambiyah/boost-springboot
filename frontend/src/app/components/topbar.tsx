"use client";

import { useRouter } from "next/navigation";

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
      <div className="flex grow items-center justify-between gap-3 px-4 py-3 lg:px-6 lg:py-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onToggleSidebar}
            aria-label={isSidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-expanded={!isSidebarCollapsed}
            className="inline-flex h-11 w-11 items-center justify-center rounded-lg border border-gray-200 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-300"
          >
            <span className="sr-only">{isSidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}</span>
            {isSidebarCollapsed ? (
              <svg width="16" height="12" viewBox="0 0 16 12" fill="none" aria-hidden="true">
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M0.58 1C0.58 0.59 0.92 0.25 1.33 0.25H14.67C15.08 0.25 15.42 0.59 15.42 1C15.42 1.41 15.08 1.75 14.67 1.75H1.33C0.92 1.75 0.58 1.41 0.58 1ZM0.58 11C0.58 10.59 0.92 10.25 1.33 10.25H14.67C15.08 10.25 15.42 10.59 15.42 11C15.42 11.41 15.08 11.75 14.67 11.75H1.33C0.92 11.75 0.58 11.41 0.58 11ZM1.33 5.25C0.92 5.25 0.58 5.59 0.58 6C0.58 6.41 0.92 6.75 1.33 6.75H8C8.41 6.75 8.75 6.41 8.75 6C8.75 5.59 8.41 5.25 8 5.25H1.33Z"
                  fill="currentColor"
                />
              </svg>
            ) : (
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M6.22 7.28C5.93 6.99 5.93 6.51 6.22 6.22C6.51 5.93 6.99 5.93 7.28 6.22L12 10.94L16.72 6.22C17.01 5.93 17.49 5.93 17.78 6.22C18.07 6.51 18.07 6.99 17.78 7.28L13.06 12L17.78 16.72C18.07 17.01 18.07 17.49 17.78 17.78C17.49 18.07 17.01 18.07 16.72 17.78L12 13.06L7.28 17.78C6.99 18.07 6.51 18.07 6.22 17.78C5.93 17.49 5.93 17.01 6.22 16.72L10.94 12L6.22 7.28Z"
                  fill="currentColor"
                />
              </svg>
            )}
          </button>

          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Boost</p>
            <p className="text-base font-semibold text-gray-900 sm:text-lg">Admin Console</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-medium text-gray-900">{user?.username ?? "Unknown User"}</p>
            <p className="text-xs text-gray-500">{user?.email ?? "No email"}</p>
          </div>
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-50 text-sm font-semibold uppercase text-blue-700">
            {(user?.username ?? "U").slice(0, 1)}
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}
