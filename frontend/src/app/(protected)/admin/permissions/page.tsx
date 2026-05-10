"use client";

import { FormEvent, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminBadge, TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";
import { adminRbacService } from "@/features/admin-rbac/admin-rbac.service";
import { AdminPermission } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_PERMISSIONS_KEY = ["admin-rbac", "permissions"] as const;

interface PermissionFormState {
  code: string;
  name: string;
  description: string;
  module: string;
  active: boolean;
}

const emptyForm: PermissionFormState = {
  code: "",
  name: "",
  description: "",
  module: "Admin",
  active: true,
};

const buildFormFromPermission = (permission: AdminPermission): PermissionFormState => ({
  code: permission.code,
  name: permission.name,
  description: permission.description ?? "",
  module: permission.module ?? "",
  active: permission.active,
});

export default function AdminPermissionsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "PERMISSION_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "PERMISSION_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);

  const [search, setSearch] = useState("");
  const [selectedPermission, setSelectedPermission] = useState<AdminPermission | null>(null);
  const [form, setForm] = useState<PermissionFormState>(emptyForm);

  const permissionsQuery = useQuery({
    queryKey: [...ADMIN_RBAC_PERMISSIONS_KEY, search],
    queryFn: () => adminRbacService.listPermissions({ search }),
  });

  const invalidatePermissions = async () => {
    await queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_PERMISSIONS_KEY });
  };

  const createMutation = useMutation({
    mutationFn: () =>
      adminRbacService.createPermission({
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        module: form.module.trim() || null,
        active: form.active,
      }),
    onSuccess: async () => {
      setForm(emptyForm);
      await invalidatePermissions();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedPermission) throw new Error("No selected permission");
      return adminRbacService.updatePermission(selectedPermission.id, {
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        module: form.module.trim() || null,
        active: form.active,
      });
    },
    onSuccess: async () => {
      setSelectedPermission(null);
      setForm(emptyForm);
      await invalidatePermissions();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminRbacService.deletePermission(id),
    onSuccess: invalidatePermissions,
  });

  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  const groupedPermissions = useMemo(() => {
    const groups = new Map<string, AdminPermission[]>();
    (permissionsQuery.data ?? []).forEach((permission) => {
      const module = permission.module || "Other";
      groups.set(module, [...(groups.get(module) ?? []), permission]);
    });
    return Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [permissionsQuery.data]);

  const handleEdit = (permission: AdminPermission) => {
    setSelectedPermission(permission);
    setForm(buildFormFromPermission(permission));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canWrite) return;
    if (selectedPermission) {
      updateMutation.mutate();
      return;
    }
    createMutation.mutate();
  };

  return (
    <RequirePermission permissions={["PERMISSION_READ", "PERMISSION_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="Permission Management"
          description="Kelola permission yang dipakai untuk RBAC, menu visibility, dan method security backend."
          actions={
            <TailAdminButton
              variant="secondary"
              onClick={() => {
                setSelectedPermission(null);
                setForm(emptyForm);
              }}
            >
              Form Permission Baru
            </TailAdminButton>
          }
        />

        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
          <TailAdminCard
            title="Permissions"
            description="Data diambil dari backend /api/admin/rbac/permissions."
            actions={
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Cari code/nama/module"
                className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />
            }
          >
            {permissionsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
            {permissionsQuery.isError ? <ErrorMessage message="Gagal memuat data permission." /> : null}
            {permissionsQuery.data?.length === 0 ? <EmptyState title="Belum ada permission" description="Tambahkan permission baru dari form di samping." /> : null}

            {groupedPermissions.length > 0 ? (
              <div className="space-y-5">
                {groupedPermissions.map(([module, permissions]) => (
                  <section key={module}>
                    <div className="mb-2 flex items-center gap-2">
                      <TailAdminBadge tone="default">{module}</TailAdminBadge>
                      <span className="text-xs text-gray-500">{permissions.length} permissions</span>
                    </div>
                    <div className="overflow-x-auto rounded-xl border border-gray-100">
                      <table className="min-w-full text-left text-sm">
                        <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500">
                          <tr>
                            <th className="px-3 py-3">Code</th>
                            <th className="px-3 py-3">Name</th>
                            <th className="px-3 py-3">Status</th>
                            <th className="px-3 py-3 text-right">Actions</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                          {permissions.map((permission) => (
                            <tr key={permission.id}>
                              <td className="px-3 py-3 font-medium text-gray-900">{permission.code}</td>
                              <td className="px-3 py-3">
                                <p className="text-gray-700">{permission.name}</p>
                                {permission.description ? <p className="text-xs text-gray-500">{permission.description}</p> : null}
                              </td>
                              <td className="px-3 py-3">
                                <TailAdminBadge tone={permission.active ? "success" : "warning"}>{permission.active ? "Active" : "Inactive"}</TailAdminBadge>
                              </td>
                              <td className="px-3 py-3">
                                <div className="flex justify-end gap-2">
                                  <TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(permission)} disabled={!canWrite}>Edit</TailAdminButton>
                                  <TailAdminButton
                                    size="sm"
                                    variant="danger"
                                    onClick={() => window.confirm(`Hapus permission ${permission.code}?`) && deleteMutation.mutate(permission.id)}
                                    disabled={!canDelete || deleteMutation.isPending}
                                  >
                                    Delete
                                  </TailAdminButton>
                                </div>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                ))}
              </div>
            ) : null}
          </TailAdminCard>

          <TailAdminCard title={selectedPermission ? "Edit Permission" : "Tambah Permission"} description="Gunakan format CODE_ACTION, contoh USER_READ atau BILLING_WRITE.">
            <form onSubmit={handleSubmit} className="space-y-4">
              <label className="block text-sm font-medium text-gray-700">
                Code
                <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="USER_READ" />
              </label>
              <label className="block text-sm font-medium text-gray-700">
                Name
                <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Read Users" />
              </label>
              <label className="block text-sm font-medium text-gray-700">
                Module
                <input value={form.module} onChange={(event) => setForm({ ...form, module: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Admin" />
              </label>
              <label className="block text-sm font-medium text-gray-700">
                Description
                <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
              </label>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
                <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
                Active
              </label>
              <div className="flex gap-2">
                <TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedPermission ? "Update Permission" : "Create Permission"}</TailAdminButton>
                {selectedPermission ? <TailAdminButton variant="secondary" onClick={() => { setSelectedPermission(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}
              </div>
              {mutationError ? <ErrorMessage message="Aksi permission gagal diproses. Cek input, duplicate code, atau permission akun login." /> : null}
            </form>
          </TailAdminCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
