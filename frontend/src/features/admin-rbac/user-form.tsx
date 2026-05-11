"use client";

import { FormEvent, useEffect, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";

import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";

import { adminRbacService } from "./admin-rbac.service";
import { AdminUser } from "./admin-rbac.types";

interface UserFormProps {
  mode: "create" | "edit";
  userId?: string;
}

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

const toForm = (user: AdminUser): UserFormState => ({
  username: user.username,
  email: user.email,
  password: "",
  fullName: user.fullName ?? "",
  active: user.active,
  roleCodes: user.roles ?? [],
});

export function UserForm({ mode, userId }: UserFormProps) {
  const router = useRouter();
  const [form, setForm] = useState<UserFormState>(emptyForm);

  const userQuery = useQuery({
    queryKey: ["admin-rbac", "users", userId],
    queryFn: () => adminRbacService.getUser(userId ?? ""),
    enabled: mode === "edit" && Boolean(userId),
  });

  const rolesQuery = useQuery({
    queryKey: ["admin-rbac", "roles", "form-options"],
    queryFn: () => adminRbacService.listRoles({ size: 100, sort: "code", direction: "asc", active: true }),
  });

  useEffect(() => {
    if (userQuery.data) setForm(toForm(userQuery.data));
  }, [userQuery.data]);

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
    onSuccess: () => router.push("/admin/users"),
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!userId) throw new Error("User ID tidak tersedia");
      return adminRbacService.updateUser(userId, {
        username: form.username.trim(),
        email: form.email.trim(),
        fullName: form.fullName.trim() || null,
        active: form.active,
        roleCodes: form.roleCodes,
      });
    },
    onSuccess: () => router.push("/admin/users"),
  });

  const toggleRole = (roleCode: string) => {
    setForm((current) => ({
      ...current,
      roleCodes: current.roleCodes.includes(roleCode)
        ? current.roleCodes.filter((item) => item !== roleCode)
        : [...current.roleCodes, roleCode],
    }));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    mode === "create" ? createMutation.mutate() : updateMutation.mutate();
  };

  const isLoading = userQuery.isLoading || rolesQuery.isLoading;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const error = userQuery.error || rolesQuery.error || createMutation.error || updateMutation.error;

  if (isLoading) return <LoadingSkeleton rows={8} />;

  return (
    <TailAdminCard
      title={mode === "create" ? "Tambah Pengguna" : "Edit Pengguna"}
      description="Kelola identitas pengguna, status akun, dan assignment role."
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid gap-4 md:grid-cols-2">
          <label className="block text-sm font-medium text-gray-700">
            Username
            <input required value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
          </label>
          <label className="block text-sm font-medium text-gray-700">
            Email
            <input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
          </label>
          <label className="block text-sm font-medium text-gray-700 md:col-span-2">
            Nama Lengkap
            <input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
          </label>
          {mode === "create" ? (
            <label className="block text-sm font-medium text-gray-700 md:col-span-2">
              Kata Sandi
              <input required minLength={8} type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
            </label>
          ) : null}
        </div>

        <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
          <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
          Aktif
        </label>

        <div>
          <p className="mb-2 text-sm font-medium text-gray-700">Role</p>
          <div className="flex flex-wrap gap-2 rounded-lg border border-gray-200 p-3">
            {(rolesQuery.data?.items ?? []).map((role) => (
              <label key={role.id} className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-700">
                <input type="checkbox" checked={form.roleCodes.includes(role.code)} onChange={() => toggleRole(role.code)} />
                {role.code}
              </label>
            ))}
          </div>
        </div>

        {error ? <ErrorMessage message="Gagal menyimpan data pengguna. Periksa input dan permission admin." /> : null}

        <div className="flex gap-2">
          <TailAdminButton type="submit" disabled={isSubmitting}>{mode === "create" ? "Simpan Pengguna" : "Perbarui Pengguna"}</TailAdminButton>
          <TailAdminButton variant="secondary" onClick={() => router.push("/admin/users")}>Batal</TailAdminButton>
        </div>
      </form>
    </TailAdminCard>
  );
}
