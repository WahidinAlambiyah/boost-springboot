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
import { AdminRole } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_ROLES_KEY = ["admin-rbac", "roles"] as const;
const ADMIN_RBAC_PERMISSIONS_KEY = ["admin-rbac", "permissions"] as const;

interface RoleFormState {
  code: string;
  name: string;
  description: string;
  active: boolean;
  permissionCodes: string[];
}

const emptyForm: RoleFormState = {
  code: "",
  name: "",
  description: "",
  active: true,
  permissionCodes: [],
};

const buildFormFromRole = (role: AdminRole): RoleFormState => ({
  code: role.code,
  name: role.name,
  description: role.description ?? "",
  active: role.active,
  permissionCodes: role.permissions ?? [],
});

export default function AdminRolesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "ROLE_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "ROLE_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);

  const [search, setSearch] = useState("");
  const [selectedRole, setSelectedRole] = useState<AdminRole | null>(null);
  const [form, setForm] = useState<RoleFormState>(emptyForm);

  const rolesQuery = useQuery({
    queryKey: [...ADMIN_RBAC_ROLES_KEY, search],
    queryFn: () => adminRbacService.listRoles({ search }),
  });

  const permissionsQuery = useQuery({
    queryKey: ADMIN_RBAC_PERMISSIONS_KEY,
    queryFn: () => adminRbacService.listPermissions(),
  });

  const invalidateRoles = async () => {
    await queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_ROLES_KEY });
  };

  const createMutation = useMutation({
    mutationFn: () =>
      adminRbacService.createRole({
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        active: form.active,
        permissionCodes: form.permissionCodes,
      }),
    onSuccess: async () => {
      setForm(emptyForm);
      await invalidateRoles();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedRole) throw new Error("No selected role");
      return adminRbacService.updateRole(selectedRole.id, {
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        active: form.active,
        permissionCodes: form.permissionCodes,
      });
    },
    onSuccess: async () => {
      setSelectedRole(null);
      setForm(emptyForm);
      await invalidateRoles();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminRbacService.deleteRole(id),
    onSuccess: invalidateRoles,
  });

  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const permissionsByModule = useMemo(() => {
    const groups = new Map<string, typeof permissionsQuery.data>();
    (permissionsQuery.data ?? []).forEach((permission) => {
      const module = permission.module || "Other";
      groups.set(module, [...(groups.get(module) ?? []), permission]);
    });
    return Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [permissionsQuery.data]);

  const handleEdit = (role: AdminRole) => {
    setSelectedRole(role);
    setForm(buildFormFromRole(role));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canWrite) return;
    if (selectedRole) {
      updateMutation.mutate();
      return;
    }
    createMutation.mutate();
  };

  const togglePermission = (permissionCode: string) => {
    setForm((current) => ({
      ...current,
      permissionCodes: current.permissionCodes.includes(permissionCode)
        ? current.permissionCodes.filter((item) => item !== permissionCode)
        : [...current.permissionCodes, permissionCode],
    }));
  };

  return (
    <RequirePermission permissions={["ROLE_READ", "ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="Role Management"
          description="Kelola role dan mapping permission untuk user."
          actions={
            <TailAdminButton
              variant="secondary"
              onClick={() => {
                setSelectedRole(null);
                setForm(emptyForm);
              }}
            >
              Form Role Baru
            </TailAdminButton>
          }
        />

        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_460px]">
          <TailAdminCard
            title="Roles"
            description="Data diambil dari backend /api/admin/rbac/roles."
            actions={
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Cari code/nama/deskripsi"
                className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />
            }
          >
            {rolesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
            {rolesQuery.isError ? <ErrorMessage message="Gagal memuat data role." /> : null}
            {rolesQuery.data?.length === 0 ? <EmptyState title="Belum ada role" description="Tambahkan role dan mapping permission dari form di samping." /> : null}

            {rolesQuery.data && rolesQuery.data.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full text-left text-sm">
                  <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-3 py-3">Role</th>
                      <th className="px-3 py-3">Permissions</th>
                      <th className="px-3 py-3">Status</th>
                      <th className="px-3 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {rolesQuery.data.map((role) => (
                      <tr key={role.id}>
                        <td className="px-3 py-3">
                          <p className="font-medium text-gray-900">{role.code}</p>
                          <p className="text-xs text-gray-500">{role.name}</p>
                          {role.description ? <p className="text-xs text-gray-500">{role.description}</p> : null}
                        </td>
                        <td className="px-3 py-3">
                          <TailAdminBadge tone="info">{role.permissions.length} permissions</TailAdminBadge>
                        </td>
                        <td className="px-3 py-3">
                          <TailAdminBadge tone={role.active ? "success" : "warning"}>{role.active ? "Active" : "Inactive"}</TailAdminBadge>
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex justify-end gap-2">
                            <TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(role)} disabled={!canWrite}>Edit</TailAdminButton>
                            <TailAdminButton
                              size="sm"
                              variant="danger"
                              onClick={() => window.confirm(`Hapus role ${role.code}?`) && deleteMutation.mutate(role.id)}
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
            ) : null}
          </TailAdminCard>

          <TailAdminCard title={selectedRole ? "Edit Role" : "Tambah Role"} description="Mapping permission menentukan menu dan akses API user.">
            <form onSubmit={handleSubmit} className="space-y-4">
              <label className="block text-sm font-medium text-gray-700">
                Code
                <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="ADMIN" />
              </label>
              <label className="block text-sm font-medium text-gray-700">
                Name
                <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Administrator" />
              </label>
              <label className="block text-sm font-medium text-gray-700">
                Description
                <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
              </label>
              <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
                <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
                Active
              </label>

              <div>
                <p className="mb-2 text-sm font-medium text-gray-700">Permissions</p>
                {permissionsQuery.isLoading ? <p className="text-sm text-gray-500">Loading permissions...</p> : null}
                <div className="max-h-[420px] space-y-3 overflow-y-auto rounded-lg border border-gray-200 p-3">
                  {permissionsByModule.map(([module, permissions]) => (
                    <div key={module}>
                      <p className="mb-2 text-xs font-semibold uppercase text-gray-500">{module}</p>
                      <div className="flex flex-wrap gap-2">
                        {(permissions ?? []).map((permission) => (
                          <label key={permission.id} className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-700">
                            <input type="checkbox" checked={form.permissionCodes.includes(permission.code)} onChange={() => togglePermission(permission.code)} />
                            {permission.code}
                          </label>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex gap-2">
                <TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedRole ? "Update Role" : "Create Role"}</TailAdminButton>
                {selectedRole ? <TailAdminButton variant="secondary" onClick={() => { setSelectedRole(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}
              </div>
              {mutationError ? <ErrorMessage message="Aksi role gagal diproses. Cek input, duplicate code, atau permission akun login." /> : null}
            </form>
          </TailAdminCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
