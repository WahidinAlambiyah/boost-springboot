"use client";

import { FormEvent, useMemo, useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

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
type SortDirection = "asc" | "desc";

interface PermissionFormState { code: string; name: string; description: string; module: string; active: boolean; }
const emptyForm: PermissionFormState = { code: "", name: "", description: "", module: "Admin", active: true };
const buildFormFromPermission = (permission: AdminPermission): PermissionFormState => ({ code: permission.code, name: permission.name, description: permission.description ?? "", module: permission.module ?? "", active: permission.active });

export default function AdminPermissionsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "PERMISSION_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "PERMISSION_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [moduleFilter, setModuleFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("module");
  const [direction, setDirection] = useState<SortDirection>("asc");
  const [selectedPermission, setSelectedPermission] = useState<AdminPermission | null>(null);
  const [form, setForm] = useState<PermissionFormState>(emptyForm);
  const activeParam = activeFilter === "active" ? true : activeFilter === "inactive" ? false : null;

  const permissionsQuery = useQuery({ queryKey: [...ADMIN_RBAC_PERMISSIONS_KEY, { search, activeParam, moduleFilter, page, size, sort, direction }], queryFn: () => adminRbacService.listPermissions({ search, active: activeParam, module: moduleFilter, page, size, sort, direction }), placeholderData: keepPreviousData });
  const allModulesQuery = useQuery({ queryKey: [...ADMIN_RBAC_PERMISSIONS_KEY, "modules"], queryFn: () => adminRbacService.listPermissions({ size: 100, sort: "module", direction: "asc" }), placeholderData: keepPreviousData });
  const invalidatePermissions = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_PERMISSIONS_KEY });
  const createMutation = useMutation({ mutationFn: () => adminRbacService.createPermission({ code: form.code.trim().toUpperCase(), name: form.name.trim(), description: form.description.trim() || null, module: form.module.trim() || null, active: form.active }), onSuccess: async () => { setForm(emptyForm); setPage(0); await invalidatePermissions(); } });
  const updateMutation = useMutation({ mutationFn: () => { if (!selectedPermission) throw new Error("No selected permission"); return adminRbacService.updatePermission(selectedPermission.id, { code: form.code.trim().toUpperCase(), name: form.name.trim(), description: form.description.trim() || null, module: form.module.trim() || null, active: form.active }); }, onSuccess: async () => { setSelectedPermission(null); setForm(emptyForm); await invalidatePermissions(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => adminRbacService.deletePermission(id), onSuccess: invalidatePermissions });
  const permissions = permissionsQuery.data?.items ?? [];
  const isRefetching = permissionsQuery.isFetching && !permissionsQuery.isLoading;
  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const modules = Array.from(new Set((allModulesQuery.data?.items ?? []).map((item) => item.module).filter(Boolean) as string[])).sort();
  const resetPage = () => setPage(0);
  const changeSort = (field: string) => { setPage(0); if (sort === field) { setDirection((current) => current === "asc" ? "desc" : "asc"); return; } setSort(field); setDirection("asc"); };
  const sortLabel = (field: string) => sort === field ? (direction === "asc" ? " ▲" : " ▼") : "";
  const handleEdit = (permission: AdminPermission) => { setSelectedPermission(permission); setForm(buildFormFromPermission(permission)); };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!canWrite) return; selectedPermission ? updateMutation.mutate() : createMutation.mutate(); };

  return (
    <RequirePermission permissions={["PERMISSION_READ", "PERMISSION_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Permission Management" description="DataTables-style server-side: slice paging, search, sorting, dan filtering." actions={<TailAdminButton variant="secondary" onClick={() => { setSelectedPermission(null); setForm(emptyForm); }}>Form Permission Baru</TailAdminButton>} />
        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
          <TailAdminCard title="Permissions" description="Backend mengambil size + 1 rows tanpa count query agar ringan.">
            <div className="mb-4 grid gap-3 md:grid-cols-[1fr_140px_160px_110px]"><input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Search permissions..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" /><select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Status</option><option value="active">Active</option><option value="inactive">Inactive</option></select><select value={moduleFilter} onChange={(event) => { setModuleFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Modules</option>{modules.map((module) => <option key={module} value={module}>{module}</option>)}</select><select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 rows</option><option value={25}>25 rows</option><option value={50}>50 rows</option></select></div>
            {permissionsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}{permissionsQuery.isError ? <ErrorMessage message="Gagal memuat data permission." /> : null}{!permissionsQuery.isLoading && permissions.length === 0 ? <EmptyState title="Tidak ada data" description="Coba ubah search/filter atau tambah permission baru." /> : null}
            {permissions.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Code{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Name{sortLabel("name")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("module")}>Module{sortLabel("module")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Actions</th></tr></thead><tbody className="divide-y divide-gray-100">{permissions.map((permission) => <tr key={permission.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{permission.code}</td><td className="px-3 py-3"><p className="text-gray-700">{permission.name}</p>{permission.description ? <p className="text-xs text-gray-500">{permission.description}</p> : null}</td><td className="px-3 py-3"><TailAdminBadge tone="default">{permission.module || "Other"}</TailAdminBadge></td><td className="px-3 py-3"><TailAdminBadge tone={permission.active ? "success" : "warning"}>{permission.active ? "Active" : "Inactive"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(permission)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => window.confirm(`Hapus permission ${permission.code}?`) && deleteMutation.mutate(permission.id)} disabled={!canDelete || deleteMutation.isPending}>Delete</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
            <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Page {page + 1} · Showing up to {size} rows {isRefetching ? "· Updating..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!permissionsQuery.data?.hasPrevious}>Previous</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!permissionsQuery.data?.hasNext}>Next</TailAdminButton></div></div>
          </TailAdminCard>
          <TailAdminCard title={selectedPermission ? "Edit Permission" : "Tambah Permission"} description="Gunakan format CODE_ACTION, contoh USER_READ atau BILLING_WRITE."><form onSubmit={handleSubmit} className="space-y-4"><label className="block text-sm font-medium text-gray-700">Code<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="USER_READ" /></label><label className="block text-sm font-medium text-gray-700">Name<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Read Users" /></label><label className="block text-sm font-medium text-gray-700">Module<input value={form.module} onChange={(event) => setForm({ ...form, module: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Admin" /></label><label className="block text-sm font-medium text-gray-700">Description<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Active</label><div className="flex gap-2"><TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedPermission ? "Update Permission" : "Create Permission"}</TailAdminButton>{selectedPermission ? <TailAdminButton variant="secondary" onClick={() => { setSelectedPermission(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}</div>{mutationError ? <ErrorMessage message="Aksi permission gagal diproses. Cek input, duplicate code, atau permission akun login." /> : null}</form></TailAdminCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
