"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { UserForm } from "@/features/admin-rbac/user-form";

export default function AdminUserCreatePage() {
  return (
    <RequirePermission permissions={["USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Tambah Pengguna" description="Buat akun pengguna baru dan tentukan role aksesnya." />
        <UserForm mode="create" />
      </AppShell>
    </RequirePermission>
  );
}
