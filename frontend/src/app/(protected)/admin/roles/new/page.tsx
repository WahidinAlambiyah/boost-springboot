"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { RoleForm } from "@/features/admin-rbac/role-form";

export default function AdminRoleCreatePage() {
  return (
    <RequirePermission permissions={["ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Tambah Role" description="Buat role baru dan tentukan permission yang dimiliki." />
        <RoleForm mode="create" />
      </AppShell>
    </RequirePermission>
  );
}
