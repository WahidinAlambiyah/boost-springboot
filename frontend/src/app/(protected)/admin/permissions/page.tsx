"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminBadge, TailAdminCard } from "@/app/components/tailadmin";

const permissionRows = [
  { code: "USER_READ", module: "Admin", action: "Read" },
  { code: "USER_WRITE", module: "Admin", action: "Write" },
  { code: "ROLE_READ", module: "Admin", action: "Read" },
  { code: "PERMISSION_READ", module: "Admin", action: "Read" },
  { code: "MENU_READ", module: "Admin", action: "Read" },
];

export default function AdminPermissionsPage() {
  return (
    <RequirePermission permissions={["PERMISSION_READ", "PERMISSION_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="Permission Management"
          description="Lihat daftar permission yang dipakai untuk RBAC dan menu visibility."
        />

        <TailAdminCard title="Permissions" description="Permission sebaiknya di-seed dari backend. UI ini disiapkan untuk read-only management terlebih dahulu.">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-3 py-3">Code</th>
                  <th className="px-3 py-3">Module</th>
                  <th className="px-3 py-3">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {permissionRows.map((row) => (
                  <tr key={row.code}>
                    <td className="px-3 py-3 font-medium text-gray-900">{row.code}</td>
                    <td className="px-3 py-3"><TailAdminBadge tone="default">{row.module}</TailAdminBadge></td>
                    <td className="px-3 py-3 text-gray-600">{row.action}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </TailAdminCard>
      </AppShell>
    </RequirePermission>
  );
}
