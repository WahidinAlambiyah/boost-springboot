"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminBadge, TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";

const userRows = [
  { username: "admin", email: "admin@example.com", role: "ADMIN", status: "Active" },
  { username: "coach.demo", email: "coach@example.com", role: "COACH", status: "Active" },
  { username: "parent.demo", email: "parent@example.com", role: "PARENT", status: "Inactive" },
];

export default function AdminUsersPage() {
  return (
    <RequirePermission permissions={["USER_READ", "USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="User Management"
          description="Kelola akun user, status aktif, dan role assignment."
          actions={<TailAdminButton disabled>Tambah User</TailAdminButton>}
        />

        <TailAdminCard title="Users" description="Placeholder TailAdmin-ready untuk CRUD user. Sambungkan ke backend /api/users saat endpoint tersedia.">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-3 py-3">Username</th>
                  <th className="px-3 py-3">Email</th>
                  <th className="px-3 py-3">Role</th>
                  <th className="px-3 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {userRows.map((row) => (
                  <tr key={row.username}>
                    <td className="px-3 py-3 font-medium text-gray-900">{row.username}</td>
                    <td className="px-3 py-3 text-gray-600">{row.email}</td>
                    <td className="px-3 py-3"><TailAdminBadge tone="info">{row.role}</TailAdminBadge></td>
                    <td className="px-3 py-3"><TailAdminBadge tone={row.status === "Active" ? "success" : "warning"}>{row.status}</TailAdminBadge></td>
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
