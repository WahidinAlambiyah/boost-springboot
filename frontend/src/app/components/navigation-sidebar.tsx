"use client";

import Link from "next/link";

import { can, canAll, canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

interface NavigationItem {
  label: string;
  href: string;
  visible: (authorities: string[]) => boolean;
}

const navigationItems: NavigationItem[] = [
  {
    label: "Dashboard",
    href: "/",
    visible: () => true,
  },
  {
    label: "Users",
    href: "/users",
    visible: (authorities) => can(authorities, "users:read"),
  },
  {
    label: "Audit Logs",
    href: "/audit-logs",
    visible: (authorities) => canAny(authorities, ["audit:read", "audit:export"]),
  },
  {
    label: "Settings",
    href: "/settings",
    visible: (authorities) => canAll(authorities, ["settings:read", "settings:update"]),
  },
];

export default function NavigationSidebar() {
  const authorities = useAuthStore((state) => state.authorities);

  const visibleItems = navigationItems.filter((item) => item.visible(authorities));

  return (
    <aside className="w-64 border-r border-zinc-200 bg-white p-4">
      <p className="mb-3 text-sm font-semibold uppercase text-zinc-500">Navigation</p>
      <nav className="flex flex-col gap-2">
        {visibleItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className="rounded-md px-3 py-2 text-sm text-zinc-700 transition-colors hover:bg-zinc-100"
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
