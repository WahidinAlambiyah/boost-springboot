"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminBadge, TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";

const roleRows = [
  { code: "ADMIN", name: "Administrator", permissions: 42, status: "Active" },
  { code: "COACH", name: "Coach", permissions: 14, status: "Active" },
  { code: "PARENT", name: "Parent", permissions: 5, status: "Active" },
];

export default function AdminRolesPage() {
  return (
    <RequirePermission permissions={["ROLE_READ", "ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="Role Management"
          description="Kelola role dan mapping permission untuk user."
          actions={<TailAdminButton disabled>Tambah Role</TailAdminButton>}
        />

        <TailAdminCard title="Roles" description="Placeholder TailAdmin-ready untuk CRUD role dan assign permissions.">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-3 py-3">Code</th>
                  <th className="px-3 py-3">Name</th>
                  <th className="px-3 py-3">Permissions</th>
                  <th className="px-3 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {roleRows.map((row) => (
                  <tr key={row.code}>
                    <td className="px-3 py-3 font-medium text-gray-900">{row.code}</td>
                    <td className="px-3 py-3 text-gray-600">{row.name}</td>
                    <td className="px-3 py-3"><TailAdminBadge tone="info">{row.permissions} permissions</TailAdminBadge></td>
                    <td className="px-3 py-3"><TailAdminBadge tone="success">{row.status}</TailAdminBadge></td>
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
