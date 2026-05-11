"use client";

import { useParams } from "next/navigation";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { RoleForm } from "@/features/admin-rbac/role-form";

export default function AdminRoleEditPage() {
  const params = useParams<{ id: string }>();

  return (
    <RequirePermission permissions={["ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Edit Role" description="Perbarui role dan mapping permission." />
        <RoleForm mode="edit" roleId={params.id} />
      </AppShell>
    </RequirePermission>
  );
}
