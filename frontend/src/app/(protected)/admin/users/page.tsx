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
import { AdminUser } from "@/features/admin-rbac/admin-rbac.types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const ADMIN_RBAC_USERS_KEY = ["admin-rbac", "users"] as const;
const ADMIN_RBAC_ROLES_KEY = ["admin-rbac", "roles"] as const;

interface UserFormState {
  username: string;
  email: string;
  password: string;
  fullName: string;
  active: boolean;
  roleCodes: string[];
}

const emptyForm: UserFormState = {
  username: "",
  email: "",
  password: "",
  fullName: "",
  active: true,
  roleCodes: [],
};

const buildFormFromUser = (user: AdminUser): UserFormState => ({
  username: user.username,
  email: user.email,
  password: "",
  fullName: user.fullName ?? "",
  active: user.active,
  roleCodes: user.roles ?? [],
});

export default function AdminUsersPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "USER_WRITE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canDelete = useMemo(() => can(authorities, "USER_DELETE") || can(authorities, "ROLE_ADMIN"), [authorities]);
  const canToggle = useMemo(
    () => canWrite || can(authorities, "USER_DISABLE") || can(authorities, "USER_ENABLE"),
    [authorities, canWrite],
  );

  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState<AdminUser | null>(null);
  const [form, setForm] = useState<UserFormState>(emptyForm);
  const [passwordTarget, setPasswordTarget] = useState<AdminUser | null>(null);
  const [newPassword, setNewPassword] = useState("");

  const usersQuery = useQuery({
    queryKey: [...ADMIN_RBAC_USERS_KEY, search],
    queryFn: () => adminRbacService.listUsers({ search }),
  });

  const rolesQuery = useQuery({
    queryKey: ADMIN_RBAC_ROLES_KEY,
    queryFn: () => adminRbacService.listRoles(),
  });

  const invalidateUsers = async () => {
    await queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_USERS_KEY });
  };

  const createMutation = useMutation({
    mutationFn: () =>
      adminRbacService.createUser({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        fullName: form.fullName.trim() || null,
        active: form.active,
        roleCodes: form.roleCodes,
      }),
    onSuccess: async () => {
      setForm(emptyForm);
      await invalidateUsers();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedUser) throw new Error("No selected user");
      return adminRbacService.updateUser(selectedUser.id, {
        username: form.username.trim(),
        email: form.email.trim(),
        fullName: form.fullName.trim() || null,
        active: form.active,
        roleCodes: form.roleCodes,
      });
    },
    onSuccess: async () => {
      setSelectedUser(null);
      setForm(emptyForm);
      await invalidateUsers();
    },
  });

  const toggleActiveMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => adminRbacService.updateUserActive(id, active),
    onSuccess: invalidateUsers,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminRbacService.deleteUser(id),
    onSuccess: invalidateUsers,
  });

  const passwordMutation = useMutation({
    mutationFn: () => {
      if (!passwordTarget) throw new Error("No selected user");
      return adminRbacService.updateUserPassword(passwordTarget.id, newPassword);
    },
    onSuccess: async () => {
      setPasswordTarget(null);
      setNewPassword("");
      await invalidateUsers();
    },
  });

  const mutationError = createMutation.error || updateMutation.error || toggleActiveMutation.error || deleteMutation.error || passwordMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const availableRoles = rolesQuery.data ?? [];

  const handleEdit = (user: AdminUser) => {
    setSelectedUser(user);
    setForm(buildFormFromUser(user));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canWrite) return;
    if (selectedUser) {
      updateMutation.mutate();
      return;
    }
    createMutation.mutate();
  };

  const toggleRole = (roleCode: string) => {
    setForm((current) => ({
      ...current,
      roleCodes: current.roleCodes.includes(roleCode)
        ? current.roleCodes.filter((item) => item !== roleCode)
        : [...current.roleCodes, roleCode],
    }));
  };

  return (
    <RequirePermission permissions={["USER_READ", "USER_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="User Management"
          description="Kelola user, status aktif, reset password, dan role assignment."
          actions={
            <TailAdminButton
              variant="secondary"
              onClick={() => {
                setSelectedUser(null);
                setForm(emptyForm);
              }}
            >
              Form User Baru
            </TailAdminButton>
          }
        />

        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
          <TailAdminCard
            title="Users"
            description="Data diambil dari backend /api/admin/rbac/users."
            actions={
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Cari username/email/nama"
                className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />
            }
          >
            {usersQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
            {usersQuery.isError ? <ErrorMessage message="Gagal memuat data user." /> : null}
            {usersQuery.data?.length === 0 ? <EmptyState title="Belum ada user" description="Tambahkan user admin/coach/operator dari form di samping." /> : null}

            {usersQuery.data && usersQuery.data.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full text-left text-sm">
                  <thead className="border-b border-gray-200 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-3 py-3">User</th>
                      <th className="px-3 py-3">Roles</th>
                      <th className="px-3 py-3">Status</th>
                      <th className="px-3 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {usersQuery.data.map((user) => (
                      <tr key={user.id}>
                        <td className="px-3 py-3">
                          <p className="font-medium text-gray-900">{user.username}</p>
                          <p className="text-xs text-gray-500">{user.email}</p>
                          {user.fullName ? <p className="text-xs text-gray-500">{user.fullName}</p> : null}
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex flex-wrap gap-1.5">
                            {user.roles.length > 0 ? user.roles.map((role) => <TailAdminBadge key={role} tone="info">{role}</TailAdminBadge>) : <TailAdminBadge>NO ROLE</TailAdminBadge>}
                          </div>
                        </td>
                        <td className="px-3 py-3">
                          <TailAdminBadge tone={user.active ? "success" : "warning"}>{user.active ? "Active" : "Inactive"}</TailAdminBadge>
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex justify-end gap-2">
                            <TailAdminButton size="sm" variant="secondary" onClick={() => handleEdit(user)} disabled={!canWrite}>Edit</TailAdminButton>
                            <TailAdminButton size="sm" variant="ghost" onClick={() => setPasswordTarget(user)} disabled={!canWrite}>Password</TailAdminButton>
                            <TailAdminButton
                              size="sm"
                              variant="ghost"
                              onClick={() => toggleActiveMutation.mutate({ id: user.id, active: !user.active })}
                              disabled={!canToggle || toggleActiveMutation.isPending}
                            >
                              {user.active ? "Disable" : "Enable"}
                            </TailAdminButton>
                            <TailAdminButton
                              size="sm"
                              variant="danger"
                              onClick={() => window.confirm(`Hapus user ${user.username}?`) && deleteMutation.mutate(user.id)}
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

          <div className="space-y-5">
            <TailAdminCard title={selectedUser ? "Edit User" : "Tambah User"} description="Password hanya wajib saat tambah user baru.">
              <form onSubmit={handleSubmit} className="space-y-4">
                <label className="block text-sm font-medium text-gray-700">
                  Username
                  <input required value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
                </label>
                <label className="block text-sm font-medium text-gray-700">
                  Email
                  <input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
                </label>
                <label className="block text-sm font-medium text-gray-700">
                  Full Name
                  <input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
                </label>
                {!selectedUser ? (
                  <label className="block text-sm font-medium text-gray-700">
                    Password
                    <input required minLength={8} type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
                  </label>
                ) : null}
                <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
                  Active
                </label>
                <div>
                  <p className="mb-2 text-sm font-medium text-gray-700">Roles</p>
                  {rolesQuery.isLoading ? <p className="text-sm text-gray-500">Loading roles...</p> : null}
                  <div className="flex flex-wrap gap-2">
                    {availableRoles.map((role) => (
                      <label key={role.id} className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-700">
                        <input type="checkbox" checked={form.roleCodes.includes(role.code)} onChange={() => toggleRole(role.code)} />
                        {role.code}
                      </label>
                    ))}
                  </div>
                </div>
                <div className="flex gap-2">
                  <TailAdminButton type="submit" disabled={!canWrite || isSubmitting}>{selectedUser ? "Update User" : "Create User"}</TailAdminButton>
                  {selectedUser ? <TailAdminButton variant="secondary" onClick={() => { setSelectedUser(null); setForm(emptyForm); }}>Cancel</TailAdminButton> : null}
                </div>
              </form>
            </TailAdminCard>

            {passwordTarget ? (
              <TailAdminCard title={`Reset Password: ${passwordTarget.username}`}>
                <form
                  className="space-y-3"
                  onSubmit={(event) => {
                    event.preventDefault();
                    passwordMutation.mutate();
                  }}
                >
                  <input required minLength={8} type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} placeholder="Password baru minimal 8 karakter" className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
                  <div className="flex gap-2">
                    <TailAdminButton type="submit" disabled={passwordMutation.isPending}>Update Password</TailAdminButton>
                    <TailAdminButton variant="secondary" onClick={() => { setPasswordTarget(null); setNewPassword(""); }}>Cancel</TailAdminButton>
                  </div>
                </form>
              </TailAdminCard>
            ) : null}

            {mutationError ? <ErrorMessage message="Aksi user gagal diproses. Cek data input atau permission akun login." /> : null}
          </div>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
