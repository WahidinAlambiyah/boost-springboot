"use client";

import Link from "next/link";

import AppShell from "@/app/components/app-shell";
import { ENABLE_DEV_TOOLS } from "@/lib/dev-tools";
import { canAny } from "@/lib/permissions";
import { SCHEDULING_SIDEBAR_PERMISSIONS } from "@/lib/permission-mapping";
import { useAuthStore } from "@/store/auth";

interface ModuleLink {
  label: string;
  href: string;
  permissions: string[];
}

const moduleLinks: ModuleLink[] = [
  { label: "Catalog", href: "/catalog", permissions: ["CLASS_READ", "CLASS_WRITE"] },
  { label: "Scheduling", href: "/scheduling", permissions: [...SCHEDULING_SIDEBAR_PERMISSIONS] },
  { label: "Enrollment", href: "/enrollment", permissions: ["ENROLLMENT_READ", "ENROLLMENT_WRITE"] },
  { label: "Attendance", href: "/attendance", permissions: ["ATTENDANCE_READ", "ATTENDANCE_MARK"] },
  { label: "Billing", href: "/billing", permissions: ["BILLING_READ", "BILLING_WRITE"] },
  { label: "Notification", href: "/notification", permissions: ["NOTIFICATION_READ", "NOTIFICATION_WRITE"] },
  ...(ENABLE_DEV_TOOLS
    ? [
        {
          label: "Developer Tools",
          href: "/dev",
          // TODO: remove ROLE_READ/USER_READ fallback when DEV_TOOLS_READ is available from backend.
          permissions: ["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"],
        },
      ]
    : []),
];

export default function DashboardPage() {
  const user = useAuthStore((state) => state.user);
  const authorities = useAuthStore((state) => state.authorities);

  const allowedModules = moduleLinks.filter((module) => canAny(authorities, module.permissions));

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Dashboard</h1>

      <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Identitas User</h2>
        <dl className="mt-3 grid gap-3 sm:grid-cols-2">
          <div><dt className="text-xs text-zinc-500">Username</dt><dd className="text-sm font-medium text-zinc-900">{user?.username ?? "-"}</dd></div>
          <div><dt className="text-xs text-zinc-500">Email</dt><dd className="text-sm font-medium text-zinc-900">{user?.email ?? "-"}</dd></div>
          <div><dt className="text-xs text-zinc-500">User ID</dt><dd className="text-sm font-medium text-zinc-900">{user?.id ?? "-"}</dd></div>
          <div><dt className="text-xs text-zinc-500">Status</dt><dd className="text-sm font-medium text-zinc-900">{user?.isActive ? "Active" : "Inactive"}</dd></div>
        </dl>
      </section>

      <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Permission Aktif</h2>
        <div className="mt-3 flex flex-wrap gap-2">
          {authorities.length > 0 ? authorities.map((permission) => (
            <span key={permission} className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-700">{permission}</span>
          )) : <p className="text-sm text-zinc-600">Belum ada permission aktif.</p>}
        </div>
      </section>

      <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Quick Links</h2>
        <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {allowedModules.length > 0 ? allowedModules.map((module) => (
            <Link key={module.href} href={module.href} className="rounded-lg border border-zinc-200 bg-zinc-50 px-4 py-3 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100">{module.label}</Link>
          )) : <p className="text-sm text-zinc-600">Tidak ada modul yang dapat diakses.</p>}
        </div>
      </section>
    </AppShell>
  );
}
