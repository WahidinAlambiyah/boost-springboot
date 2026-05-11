"use client";

import { useMemo, useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";

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

export default function AdminRolesPage() {
  const router = useRouter();
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
  const [pendingDeleteRole, setPendingDeleteRole] = useState<AdminRole | null>(null);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const rolesQuery = useQuery({
    queryKey: [...ADMIN_RBAC_ROLES_KEY, { search, activeParam, permissionFilter, page, size, sort, direction }],
    queryFn: () => adminRbacService.listRoles({ search, active: activeParam, permissionCode: permissionFilter, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });
  const permissionsQuery = useQuery({ queryKey: ADMIN_RBAC_PERMISSIONS_KEY, queryFn: () => adminRbacService.listPermissions({ size: 100, sort: "module", direction: "asc" }), placeholderData: keepPreviousData });
  const invalidateRoles = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_ROLES_KEY });
  const deleteMutation = useMutation({ mutationFn: (id: string) => adminRbacService.deleteRole(id), onSuccess: async () => { setPendingDeleteRole(null); await invalidateRoles(); } });
  const roles = rolesQuery.data?.items ?? [];
  const permissions = permissionsQuery.data?.items ?? [];
  const isRefetching = rolesQuery.isFetching && !rolesQuery.isLoading;
  const mutationError = deleteMutation.error;
  const resetPage = () => setPage(0);
  const changeSort = (field: string) => { setPage(0); if (sort === field) { setDirection((current) => current === "asc" ? "desc" : "asc"); return; } setSort(field); setDirection("asc"); };
  const sortLabel = (field: string) => sort === field ? (direction === "asc" ? " ▲" : " ▼") : "";

  return (
    <RequirePermission permissions={["ROLE_READ", "ROLE_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Manajemen Role" description="Kelola role dan permission yang dimiliki setiap role." actions={<TailAdminButton onClick={() => router.push("/admin/roles/new")} disabled={!canWrite}>Tambah Role</TailAdminButton>} />
        <TailAdminCard title="Daftar Role" description="Data menggunakan server-side slice, pencarian, sorting, dan filtering.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_150px_200px_120px]"><input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari role..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" /><select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select><select value={permissionFilter} onChange={(event) => { setPermissionFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Permission</option>{permissions.map((permission) => <option key={permission.id} value={permission.code}>{permission.code}</option>)}</select><select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select></div>
          {rolesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}{rolesQuery.isError ? <ErrorMessage message="Gagal memuat data role." /> : null}{!rolesQuery.isLoading && roles.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah role baru." /> : null}
          {roles.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Role{sortLabel("code")}</button></th><th className="px-3 py-3">Permission</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("createdAt")}>Dibuat{sortLabel("createdAt")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{roles.map((role) => <tr key={role.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3"><p className="font-medium text-gray-900">{role.code}</p><p className="text-xs text-gray-500">{role.name}</p>{role.description ? <p className="text-xs text-gray-500">{role.description}</p> : null}</td><td className="px-3 py-3"><TailAdminBadge tone="info">{role.permissions.length} permission</TailAdminBadge></td><td className="px-3 py-3"><TailAdminBadge tone={role.active ? "success" : "warning"}>{role.active ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3 text-xs text-gray-500">{role.createdAt ?? "-"}</td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => router.push(`/admin/roles/${role.id}/edit`)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDeleteRole(role)} disabled={!canDelete || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!rolesQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!rolesQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi role gagal diproses. Periksa data atau permission admin." /> : null}
        </TailAdminCard>
        <ConfirmModal open={Boolean(pendingDeleteRole)} title="Hapus role?" description={<span>Role <strong>{pendingDeleteRole?.code}</strong> akan dihapus. Pastikan role ini tidak sedang digunakan oleh pengguna penting sebelum melanjutkan.</span>} confirmLabel="Ya, hapus role" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDeleteRole(null)} onConfirm={() => pendingDeleteRole && deleteMutation.mutate(pendingDeleteRole.id)} />
      </AppShell>
    </RequirePermission>
  );
}
