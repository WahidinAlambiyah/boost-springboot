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
import { AdminUser } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_USERS_KEY = ["admin-rbac", "users"] as const;
const ADMIN_RBAC_ROLES_KEY = ["admin-rbac", "roles"] as const;
type SortDirection = "asc" | "desc";
type PendingAction = { type: "delete" | "toggle"; user: AdminUser; nextActive?: boolean } | null;

interface UserFormState { username: string; email: string; password: string; fullName: string; active: boolean; roleCodes: string[]; }
const emptyForm: UserFormState = { username: "", email: "", password: "", fullName: "", active: true, roleCodes: [] };
const buildFormFromUser = (user: AdminUser): UserFormState => ({ username: user.username, email: user.email, password: "", fullName: user.fullName ?? "", active: user.active, roleCodes: user.roles ?? [] });

export default function AdminUsersPage() {
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
  const [selectedUser, setSelectedUser] = useState<AdminUser | null>(null);
  const [form, setForm] = useState<UserFormState>(emptyForm);
  const [passwordTarget, setPasswordTarget] = useState<AdminUser | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const activeParam = activeFilter === "active" ? true : activeFilter === "inactive" ? false : null;

  const usersQuery = useQuery({ queryKey: [...ADMIN_RBAC_USERS_KEY, { search, activeParam, roleFilter, page, size, sort, direction }], queryFn: () => adminRbacService.listUsers({ search, active: activeParam, roleCode: roleFilter, page, size, sort, direction }), placeholderData: keepPreviousData });
  const rolesQuery = useQuery({ queryKey: ADMIN_RBAC_ROLES_KEY, queryFn: () => adminRbacService.listRoles({ size: 100, sort: "code", direction: "asc" }), placeholderData: keepPreviousData });
  const invalidateUsers = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_USERS_KEY });
  const createMutation = useMutation({ mutationFn: () => adminRbacService.createUser({ username: form.username.trim(), email: form.email.trim(), password: form.password, fullName: form.fullName.trim() || null, active: form.active, roleCodes: form.roleCodes }), onSuccess: async () => { setForm(emptyForm); setPage(0); await invalidateUsers(); } });
  const updateMutation = useMutation({ mutationFn: () => { if (!selectedUser) throw new Error("No selected user"); return adminRbacService.updateUser(selectedUser.id, { username: form.username.trim(), email: form.email.trim(), fullName: form.fullName.trim() || null, active: form.active, roleCodes: form.roleCodes }); }, onSuccess: async () => { setSelectedUser(null); setForm(emptyForm); await invalidateUsers(); } });
  const toggleActiveMutation = useMutation({ mutationFn: ({ id, active }: { id: string; active: boolean }) => adminRbacService.updateUserActive(id, active), onSuccess: async () => { setPendingAction(null); await invalidateUsers(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => adminRbacService.deleteUser(id), onSuccess: async () => { setPendingAction(null); await invalidateUsers(); } });
  const passwordMutation = useMutation({ mutationFn: () => { if (!passwordTarget) throw new Error("No selected user"); return adminRbacService.updateUserPassword(passwordTarget.id, newPassword); }, onSuccess: async () => { setPasswordTarget(null); setNewPassword(""); await invalidateUsers(); } });
  const users = usersQuery.data?.items ?? [];
  const availableRoles = rolesQuery.data?.items ?? [];
  const isRefetching = usersQuery.isFetching && !usersQuery.isLoading;
  const mutationError = createMutation.error || updateMutation.error || toggleActiveMutation.error || deleteMutation.error || passwordMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const resetPage = () => setPage(0);
  const changeSort = (field: string) => { setPage(0); if (sort === field) { setDirection((current) => current === "asc" ? "desc" : "asc"); return; } setSort(field); setDirection("asc"); };
  const sortLabel = (field: string) => sort === field ? (direction === "asc" ? " ▲" : " ▼") : "";
  const handleEdit = (user: AdminUser) => { setSelectedUser(user); setForm(buildFormFromUser(user)); };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!canWrite) return; selectedUser ? updateMutation.mutate() : createMutation.mutate(); };
  const toggleRole = (roleCode: string) => setForm((current) => ({ ...current, roleCodes: current.roleCodes.includes(roleCode) ? current.roleCodes.filter((item) => item !== roleCode) : [...current.roleCodes, roleCode] }));
  const confirmAction = () => { if (!pendingAction) return; if (pendingAction.type === "delete") deleteMutation.mutate(pendingAction.user.id); else toggleActiveMutation.mutate({ id: pendingAction.user.id, active: Boolean(pendingAction.nextActive) }); };

  return (
    <RequirePermission permissions={["USER_READ", "USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="User Management" description="DataTables-style server-side: slice paging, search, sorting, dan filtering." actions={<TailAdminButton variant="secondary" onClick={() => { setSelectedUser(null); setForm(emptyForm); }}>Form User Baru</TailAdminButton>} />
        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
          <TailAdminCard title="Users" description="Backend mengambil size + 1 rows tanpa count query agar ringan.">
            <div className="mb-4 grid gap-3 md:grid-cols-[1fr_140px_160px_110px]">
              <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Search users..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
              <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Status</option><option value="active">Active</option><option value="inactive">Inactive</option></select>
              <select value={roleFilter} onChange={(event) => { setRoleFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">All Roles</option>{availableRoles.map((role) => <option key={role.id} value={role.code}>{role.code}</option>)}</select>
              <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 rows</option><option value={25}>25 rows</option><option value={50}>50 rows</option></select>
            </div>
            {usersQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
            {usersQuery.isError ? <ErrorMessage message="Gagal memuat data user." /> : null}
            {!usersQuery.isLoading && users.length === 0 ? <EmptyState title="Tidak ada data" description="Coba ubah search/filter atau tambah user baru." /> : null}
            {users.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("username")}>User{sortLabel("username")}</button></th><th className="px-3 py-3">Roles</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("createdAt")}>Created{sortLabel("createdAt")}</button></th><th className="px-3 py-3 text-right">Actions</th></tr></thead><tbody className="divide-y divide-gray-100">{users.map((user) => <tr key={user.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3"><p className="font-medium text-gray-900">{user.username}</p><p className="text-xs text-gray-500">{user.email}</p>{user.fullName ? <p className="text-xs text-gray-500">{user.fullName}</p> : null}</td><td className="px-3 py-3"><div className="flex flex-wrap gap-1.5">{user.roles.length > 0 ? user.roles.map((role) => <TailAdminBadge key={role} tone="info">{role}</TailAdminBadge>) : <TailAdminBadge>NO ROLE</TailAdminBadge>}</div></td><td className="px-3 py-3"><TailAdminBadge tone={user.active ? "success" : "warning"}>{user.active ? "Active" : "Inactive"}</TailAdminBadge></td><td className="px-3 py-3 text-xs text-gray-500">{user.createdAt ?? "-"}</td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(user)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="ghost" onClick={() => setPasswordTarget(user)} disabled={!canWrite}>Password</TailAdminButton><TailAdminButton size="sm" variant="ghost" onClick={() => setPendingAction({ type: "toggle", user, nextActive: !user.active })} disabled={!canToggle || toggleActiveMutation.isPending}>{user.active ? "Disable" : "Enable"}</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingAction({ type: "delete", user })} disabled={!canDelete || deleteMutation.isPending}>Delete</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
            <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Page {page + 1} · Showing up to {size} rows {isRefetching ? "· Updating..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!usersQuery.data?.hasPrevious}>Previous</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!usersQuery.data?.hasNext}>Next</TailAdminButton></div></div>
          </TailAdminCard>
          <div className="space-y-5"><TailAdminCard title={selectedUser ? "Edit User" : "Tambah User"} description="Password hanya wajib saat tambah user baru."><form onSubmit={handleSubmit} className="space-y-4"><label className="block text-sm font-medium text-gray-700">Username<input required value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Email<input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Full Name<input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label>{!selectedUser ? <label className="block text-sm font-medium text-gray-700">Password<input required minLength={8} type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label> : null}<label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Active</label><div><p className="mb-2 text-sm font-medium text-gray-700">Roles</p><div className="flex flex-wrap gap-2">{availableRoles.map((role) => <label key={role.id} className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-700"><input type="checkbox" checked={form.roleCodes.includes(role.code)} onChange={() => toggleRole(role.code)} />{role.code}</label>)}</div></div><div className="flex gap-2"><TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedUser ? "Update User" : "Create User"}</TailAdminButton>{selectedUser ? <TailAdminButton variant="secondary" onClick={() => { setSelectedUser(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}</div></form></TailAdminCard>{passwordTarget ? <TailAdminCard title={`Reset Password: ${passwordTarget.username}`}><form className="space-y-3" onSubmit={(event) => { event.preventDefault(); passwordMutation.mutate(); }}><input required minLength={8} type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} placeholder="Password baru minimal 8 karakter" className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /><div className="flex gap-2"><TailAdminButton type="submit" disabled={passwordMutation.isPending}>Update Password</TailAdminButton><TailAdminButton variant="secondary" onClick={() => { setPasswordTarget(null); setNewPassword(""); }}>Cancel</TailAdminButton></div></form></TailAdminCard> : null}{mutationError ? <ErrorMessage message="Aksi user gagal diproses. Cek data input atau permission akun login." /> : null}</div>
        </div>
        <ConfirmModal open={Boolean(pendingAction)} title={pendingAction?.type === "delete" ? "Hapus user?" : pendingAction?.nextActive ? "Aktifkan user?" : "Nonaktifkan user?"} description={<span>Aksi ini akan diterapkan ke user <strong>{pendingAction?.user.username}</strong>. Pastikan data dan permission admin sudah benar sebelum melanjutkan.</span>} confirmLabel={pendingAction?.type === "delete" ? "Ya, hapus" : pendingAction?.nextActive ? "Ya, aktifkan" : "Ya, nonaktifkan"} tone={pendingAction?.type === "delete" ? "danger" : "warning"} loading={deleteMutation.isPending || toggleActiveMutation.isPending} onCancel={() => setPendingAction(null)} onConfirm={confirmAction} />
      </AppShell>
    </RequirePermission>
  );
}
