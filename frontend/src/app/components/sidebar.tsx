"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

interface SidebarItem {
  label: string;
  href: string;
  permissions?: string[];
}

const sidebarItems: SidebarItem[] = [
  { label: "Dashboard", href: "/dashboard" },
  { label: "Catalog", href: "/catalog", permissions: ["CLASS_READ", "CLASS_WRITE"] },
  { label: "Scheduling", href: "/scheduling", permissions: ["SCHEDULE_READ", "SCHEDULE_WRITE"] },
  { label: "Enrollment", href: "/enrollment", permissions: ["ENROLLMENT_READ", "ENROLLMENT_WRITE"] },
  { label: "Attendance", href: "/attendance", permissions: ["ATTENDANCE_READ", "ATTENDANCE_MARK"] },
  { label: "Billing", href: "/billing", permissions: ["BILLING_READ", "BILLING_WRITE"] },
  { label: "Notification", href: "/notification", permissions: ["NOTIFICATION_READ", "NOTIFICATION_WRITE"] },
];

export default function Sidebar() {
  const pathname = usePathname();
  const authorities = useAuthStore((state) => state.authorities);

  const visibleItems = sidebarItems.filter((item) => {
    if (!item.permissions) {
      return true;
    }

    return canAny(authorities, item.permissions);
  });

  return (
    <aside className="w-64 border-r border-zinc-200 bg-white p-4">
      <p className="mb-3 text-sm font-semibold uppercase text-zinc-500">Navigation</p>
      <nav className="flex flex-col gap-2">
        {visibleItems.map((item) => {
          const isActive = pathname === item.href;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`rounded-md px-3 py-2 text-sm transition-colors ${
                isActive
                  ? "bg-zinc-900 text-white"
                  : "text-zinc-700 hover:bg-zinc-100"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
