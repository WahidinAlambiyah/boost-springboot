"use client";

import { FormEvent, useMemo, useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { ConfirmModal, TailAdminBadge, TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";
import { adminRbacService } from "@/features/admin-rbac/admin-rbac.service";
import { AdminRole } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_ROLES_KEY = ["admin-rbac", "roles"] as const;
const ADMIN_RBAC_PERMISSIONS_KEY = ["admin-rbac", "permissions"] as const;
type SortDirection = "asc" | "desc";

interface RoleFormState { code: string; name: string; description: string; active: boolean; permissionCodes: string[]; }
const emptyForm: RoleFormState = { code: "", name: "", description: "", active: true, permissionCodes: [] };
const buildFormFromRole = (role: AdminRole): RoleFormState => ({ code: role.code, name: role.name, description: role.description ?? "", active: role.active, permissionCodes: role.permissions ?? [] });

export default function AdminRolesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "ROLE_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "ROLE_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [permissionFilter, setPermissionFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("code");
  const [direction, setDirection] = useState<SortDirection>("asc");
  const [selectedRole, setSelectedRole] = useState<AdminRole | null>(null);
  const [pendingDeleteRole, setPendingDeleteRole] = useState<AdminRole | null>(null);
  const [form, setForm] = useState<RoleFormState>(emptyForm);
  const activeParam = activeFilter === "active" ? true : activeFilter === "inactive" ? false : null;

  const rolesQuery = useQuery({ queryKey: [...ADMIN_RBAC_ROLES_KEY, { search, activeParam, permissionFilter, page, size, sort, direction }], queryFn: () => adminRbacService.listRoles({ search, active: activeParam, permissionCode: permissionFilter, page, size, sort, direction }), placeholderData: keepPreviousData });
  const permissionsQuery = useQuery({ queryKey: ADMIN_RBAC_PERMISSIONS_KEY, queryFn: () => adminRbacService.listPermissions({ size: 100, sort: "module", direction: "asc" }), placeholderData: keepPreviousData });
  const invalidateRoles = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_ROLES_KEY });
  const createMutation = useMutation({ mutationFn: () => adminRbacService.createRole({ code: form.code.trim().toUpperCase(), name: form.name.trim(), description: form.description.trim() || null, active: form.active, permissionCodes: form.permissionCodes }), onSuccess: async () => { setForm(emptyForm); setPage(0); await invalidateRoles(); } });
  const updateMutation = useMutation({ mutationFn: () => { if (!selectedRole) throw new Error("No selected role"); return adminRbacService.updateRole(selectedRole.id, { code: form.code.trim().toUpperCase(), name: form.name.trim(), description: form.description.trim() || null, active: form.active, permissionCodes: form.permissionCodes }); }, onSuccess: async () => { setSelectedRole(null); setForm(emptyForm); await invalidateRoles(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => adminRbacService.deleteRole(id), onSuccess: async () => { setPendingDeleteRole(null); await invalidateRoles(); } });
  const roles = rolesQuery.data?.items ?? [];
  const permissions = permissionsQuery.data?.items ?? [];
  const isRefetching = rolesQuery.isFetching && !rolesQuery.isLoading;
  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const resetPage = () => setPage(0);
  const changeSort = (field: string) => { setPage(0); if (sort === field) { setDirection((current) => current === "asc" ? "desc" : "asc"); return; } setSort(field); setDirection("asc"); };
  const sortLabel = (field: string) => sort === field ? (direction === "asc" ? " ▲" : " ▼") : "";
  const permissionsByModule = useMemo(() => { const groups = new Map<string, typeof permissions>(); permissions.forEach((permission) => { const module = permission.module || "Other"; groups.set(module, [...(groups.get(module) ?? []), permission]); }); return Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b)); }, [permissions]);
  const handleEdit = (role: AdminRole) => { setSelectedRole(role); setForm(buildFormFromRole(role)); };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!canWrite) return; selectedRole ? updateMutation.mutate() : createMutation.mutate(); };
  const togglePermission = (permissionCode: string) => setForm((current) => ({ ...current, permissionCodes: current.permissionCodes.includes(permissionCode) ? current.permissionCodes.filter((item) => item !== permissionCode) : [...current.permissionCodes, permissionCode] }));

  return (
    <RequirePermission permissions={["ROLE_READ", "ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Role Management" description="DataTables-style server-side: slice paging, search, sorting, dan filtering." actions={<TailAdminButton variant="secondary" onClick={() => { setSelectedRole(null); setForm(emptyForm); }}>Form Role Baru</TailAdminButton>} />
        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_460px]">
          <TailAdminCard title="Roles" description="Backend mengambil size + 1 rows tanpa count query agar ringan.">
            <div className="mb-4 grid gap-3 md:grid-cols-[1fr_140px_190px_110px]"><input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Search roles..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" /><select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Status</option><option value="active">Active</option><option value="inactive">Inactive</option></select><select value={permissionFilter} onChange={(event) => { setPermissionFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Permissions</option>{permissions.map((permission) => <option key={permission.id} value={permission.code}>{permission.code}</option>)}</select><select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 rows</option><option value={25}>25 rows</option><option value={50}>50 rows</option></select></div>
            {rolesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}{rolesQuery.isError ? <ErrorMessage message="Gagal memuat data role." /> : null}{!rolesQuery.isLoading && roles.length === 0 ? <EmptyState title="Tidak ada data" description="Coba ubah search/filter atau tambah role baru." /> : null}
            {roles.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Role{sortLabel("code")}</button></th><th className="px-3 py-3">Permissions</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("createdAt")}>Created{sortLabel("createdAt")}</button></th><th className="px-3 py-3 text-right">Actions</th></tr></thead><tbody className="divide-y divide-gray-100">{roles.map((role) => <tr key={role.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3"><p className="font-medium text-gray-900">{role.code}</p><p className="text-xs text-gray-500">{role.name}</p>{role.description ? <p className="text-xs text-gray-500">{role.description}</p> : null}</td><td className="px-3 py-3"><TailAdminBadge tone="info">{role.permissions.length} permissions</TailAdminBadge></td><td className="px-3 py-3"><TailAdminBadge tone={role.active ? "success" : "warning"}>{role.active ? "Active" : "Inactive"}</TailAdminBadge></td><td className="px-3 py-3 text-xs text-gray-500">{role.createdAt ?? "-"}</td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(role)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDeleteRole(role)} disabled={!canDelete || deleteMutation.isPending}>Delete</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
            <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Page {page + 1} · Showing up to {size} rows {isRefetching ? "· Updating..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!rolesQuery.data?.hasPrevious}>Previous</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!rolesQuery.data?.hasNext}>Next</TailAdminButton></div></div>
          </TailAdminCard>
          <TailAdminCard title={selectedRole ? "Edit Role" : "Tambah Role"} description="Mapping permission menentukan menu dan akses API user."><form onSubmit={handleSubmit} className="space-y-4"><label className="block text-sm font-medium text-gray-700">Code<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="ADMIN" /></label><label className="block text-sm font-medium text-gray-700">Name<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Administrator" /></label><label className="block text-sm font-medium text-gray-700">Description<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Active</label><div><p className="mb-2 text-sm font-medium text-gray-700">Permissions</p><div className="max-h-[420px] space-y-3 overflow-y-auto rounded-lg border border-gray-200 p-3">{permissionsByModule.map(([module, modulePermissions]) => <div key={module}><p className="mb-2 text-xs font-semibold uppercase text-gray-500">{module}</p><div className="flex flex-wrap gap-2">{modulePermissions.map((permission) => <label key={permission.id} className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-700"><input type="checkbox" checked={form.permissionCodes.includes(permission.code)} onChange={() => togglePermission(permission.code)} />{permission.code}</label>)}</div></div>)}</div></div><div className="flex gap-2"><TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedRole ? "Update Role" : "Create Role"}</TailAdminButton>{selectedRole ? <TailAdminButton variant="secondary" onClick={() => { setSelectedRole(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}</div>{mutationError ? <ErrorMessage message="Aksi role gagal diproses. Cek input, duplicate code, atau permission akun login." /> : null}</form></TailAdminCard>
        </div>
        <ConfirmModal open={Boolean(pendingDeleteRole)} title="Hapus role?" description={<span>Role <strong>{pendingDeleteRole?.code}</strong> akan dihapus. Pastikan role ini tidak sedang digunakan oleh user penting sebelum melanjutkan.</span>} confirmLabel="Ya, hapus role" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDeleteRole(null)} onConfirm={() => pendingDeleteRole && deleteMutation.mutate(pendingDeleteRole.id)} />
      </AppShell>
    </RequirePermission>
  );
}
