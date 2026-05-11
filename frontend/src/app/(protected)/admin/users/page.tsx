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
import { AdminUser } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_USERS_KEY = ["admin-rbac", "users"] as const;
const ADMIN_RBAC_ROLES_KEY = ["admin-rbac", "roles"] as const;
type SortDirection = "asc" | "desc";
type PendingAction = { type: "delete" | "toggle"; user: AdminUser; nextActive?: boolean } | null;

export default function AdminUsersPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "USER_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "USER_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canToggle = useMemo(() => canWrite || can(authorities, "USER_DISABLE") || can(authorities, "USER_ENABLE"), [authorities, canWrite]);
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [passwordTarget, setPasswordTarget] = useState<AdminUser | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const usersQuery = useQuery({
    queryKey: [...ADMIN_RBAC_USERS_KEY, { search, activeParam, roleFilter, page, size, sort, direction }],
    queryFn: () => adminRbacService.listUsers({ search, active: activeParam, roleCode: roleFilter, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const rolesQuery = useQuery({
    queryKey: ADMIN_RBAC_ROLES_KEY,
    queryFn: () => adminRbacService.listRoles({ size: 100, sort: "code", direction: "asc" }),
    placeholderData: keepPreviousData,
  });

  const invalidateUsers = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_USERS_KEY });
  const toggleActiveMutation = useMutation({ mutationFn: ({ id, active }: { id: string; active: boolean }) => adminRbacService.updateUserActive(id, active), onSuccess: async () => { setPendingAction(null); await invalidateUsers(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => adminRbacService.deleteUser(id), onSuccess: async () => { setPendingAction(null); await invalidateUsers(); } });
  const passwordMutation = useMutation({ mutationFn: () => { if (!passwordTarget) throw new Error("Pengguna belum dipilih"); return adminRbacService.updateUserPassword(passwordTarget.id, newPassword); }, onSuccess: async () => { setPasswordTarget(null); setNewPassword(""); await invalidateUsers(); } });

  const users = usersQuery.data?.items ?? [];
  const availableRoles = rolesQuery.data?.items ?? [];
  const isRefetching = usersQuery.isFetching && !usersQuery.isLoading;
  const mutationError = toggleActiveMutation.error || deleteMutation.error || passwordMutation.error;
  const resetPage = () => setPage(0);
  const changeSort = (field: string) => { setPage(0); if (sort === field) { setDirection((current) => current === "asc" ? "desc" : "asc"); return; } setSort(field); setDirection("asc"); };
  const sortLabel = (field: string) => sort === field ? (direction === "asc" ? " ▲" : " ▼") : "";
  const confirmAction = () => { if (!pendingAction) return; if (pendingAction.type === "delete") deleteMutation.mutate(pendingAction.user.id); else toggleActiveMutation.mutate({ id: pendingAction.user.id, active: Boolean(pendingAction.nextActive) }); };

  return (
    <RequirePermission permissions={["USER_READ", "USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Manajemen Pengguna" description="Kelola akun pengguna, status akun, dan assignment role." actions={<TailAdminButton onClick={() => router.push("/admin/users/new")} disabled={!canWrite}>Tambah Pengguna</TailAdminButton>} />
        <TailAdminCard title="Daftar Pengguna" description="Data menggunakan server-side slice, pencarian, sorting, dan filtering.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_150px_170px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari pengguna..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={roleFilter} onChange={(event) => { setRoleFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Role</option>{availableRoles.map((role) => <option key={role.id} value={role.code}>{role.code}</option>)}</select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>

          {usersQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {usersQuery.isError ? <ErrorMessage message="Gagal memuat data pengguna." /> : null}
          {!usersQuery.isLoading && users.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah pengguna baru." /> : null}

          {users.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("username")}>Pengguna{sortLabel("username")}</button></th><th className="px-3 py-3">Role</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("createdAt")}>Dibuat{sortLabel("createdAt")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{users.map((user) => <tr key={user.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3"><p className="font-medium text-gray-900">{user.username}</p><p className="text-xs text-gray-500">{user.email}</p>{user.fullName ? <p className="text-xs text-gray-500">{user.fullName}</p> : null}</td><td className="px-3 py-3"><div className="flex flex-wrap gap-1.5">{user.roles.length > 0 ? user.roles.map((role) => <TailAdminBadge key={role} tone="info">{role}</TailAdminBadge>) : <TailAdminBadge>Belum Ada Role</TailAdminBadge>}</div></td><td className="px-3 py-3"><TailAdminBadge tone={user.active ? "success" : "warning"}>{user.active ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3 text-xs text-gray-500">{user.createdAt ?? "-"}</td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => router.push(`/admin/users/${user.id}/edit`)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="ghost" onClick={() => setPasswordTarget(user)} disabled={!canWrite}>Kata Sandi</TailAdminButton><TailAdminButton size="sm" variant="ghost" onClick={() => setPendingAction({ type: "toggle", user, nextActive: !user.active })} disabled={!canToggle || toggleActiveMutation.isPending}>{user.active ? "Nonaktifkan" : "Aktifkan"}</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingAction({ type: "delete", user })} disabled={!canDelete || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!usersQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!usersQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi pengguna gagal diproses. Periksa data atau permission admin." /> : null}
        </TailAdminCard>

        <ConfirmModal open={Boolean(pendingAction)} title={pendingAction?.type === "delete" ? "Hapus pengguna?" : pendingAction?.nextActive ? "Aktifkan pengguna?" : "Nonaktifkan pengguna?"} description={<span>Aksi ini akan diterapkan ke pengguna <strong>{pendingAction?.user.username}</strong>. Pastikan data dan permission admin sudah benar sebelum melanjutkan.</span>} confirmLabel={pendingAction?.type === "delete" ? "Ya, hapus" : pendingAction?.nextActive ? "Ya, aktifkan" : "Ya, nonaktifkan"} tone={pendingAction?.type === "delete" ? "danger" : "warning"} loading={deleteMutation.isPending || toggleActiveMutation.isPending} onCancel={() => setPendingAction(null)} onConfirm={confirmAction} />
        <ConfirmModal open={Boolean(passwordTarget)} title="Perbarui kata sandi?" description={<div className="space-y-3"><p>Kata sandi pengguna <strong>{passwordTarget?.username}</strong> akan diperbarui.</p><input required minLength={8} type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} placeholder="Kata sandi baru minimal 8 karakter" className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></div>} confirmLabel="Perbarui kata sandi" tone="warning" loading={passwordMutation.isPending} onCancel={() => { setPasswordTarget(null); setNewPassword(""); }} onConfirm={() => passwordMutation.mutate()} />
      </AppShell>
    </RequirePermission>
  );
}
