"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminBadge, TailAdminCard } from "@/app/components/tailadmin";

const auditRows = [
  { actor: "admin", action: "LOGIN_SUCCESS", target: "Auth", createdAt: "2026-05-10 09:00" },
  { actor: "admin", action: "USER_UPDATED", target: "coach.demo", createdAt: "2026-05-10 09:15" },
  { actor: "system", action: "ROLE_SYNC", target: "ADMIN", createdAt: "2026-05-10 09:30" },
];

export default function AdminAuditLogPage() {
  return (
    <RequirePermission permissions={["AUDIT_READ", "USER_READ", "ROLE_READ"]} mode="any">
      <AppShell>
        <PageHeader
          title="Audit Log"
          description="Pantau aktivitas penting seperti login, perubahan user, role, permission, dan menu."
        />

        <TailAdminCard title="Recent Audit Events" description="Placeholder TailAdmin-ready untuk audit trail. Sambungkan ke backend audit endpoint saat tersedia.">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-3 py-3">Time</th>
                  <th className="px-3 py-3">Actor</th>
                  <th className="px-3 py-3">Action</th>
                  <th className="px-3 py-3">Target</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {auditRows.map((row) => (
                  <tr key={`${row.actor}-${row.action}-${row.createdAt}`}>
                    <td className="px-3 py-3 text-gray-600">{row.createdAt}</td>
                    <td className="px-3 py-3 font-medium text-gray-900">{row.actor}</td>
                    <td className="px-3 py-3"><TailAdminBadge tone="info">{row.action}</TailAdminBadge></td>
                    <td className="px-3 py-3 text-gray-600">{row.target}</td>
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
