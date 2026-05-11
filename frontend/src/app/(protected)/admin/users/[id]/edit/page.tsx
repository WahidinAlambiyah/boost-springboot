"use client";

import { useParams } from "next/navigation";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { UserForm } from "@/features/admin-rbac/user-form";

export default function AdminUserEditPage() {
  const params = useParams<{ id: string }>();

  return (
    <RequirePermission permissions={["USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Edit Pengguna" description="Perbarui akun pengguna dan role aksesnya." />
        <UserForm mode="edit" userId={params.id} />
      </AppShell>
    </RequirePermission>
  );
}
