"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";

import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";

import { adminRbacService } from "./admin-rbac.service";
import { AdminRole } from "./admin-rbac.types";

interface RoleFormProps {
  mode: "create" | "edit";
  roleId?: string;
}

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

const toForm = (role: AdminRole): RoleFormState => ({
  code: role.code,
  name: role.name,
  description: role.description ?? "",
  active: role.active,
  permissionCodes: role.permissions ?? [],
});

export function RoleForm({ mode, roleId }: RoleFormProps) {
  const router = useRouter();
  const [form, setForm] = useState<RoleFormState>(emptyForm);

  const roleQuery = useQuery({
    queryKey: ["admin-rbac", "roles", roleId],
    queryFn: () => adminRbacService.getRole(roleId ?? ""),
    enabled: mode === "edit" && Boolean(roleId),
  });

  const permissionsQuery = useQuery({
    queryKey: ["admin-rbac", "permissions", "form-options"],
    queryFn: () => adminRbacService.listPermissions({ size: 100, sort: "module", direction: "asc", active: true }),
  });

  useEffect(() => {
    if (roleQuery.data) setForm(toForm(roleQuery.data));
  }, [roleQuery.data]);

  const createMutation = useMutation({
    mutationFn: () =>
      adminRbacService.createRole({
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        active: form.active,
        permissionCodes: form.permissionCodes,
      }),
    onSuccess: () => router.push("/admin/roles"),
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!roleId) throw new Error("ID role tidak tersedia");
      return adminRbacService.updateRole(roleId, {
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        description: form.description.trim() || null,
        active: form.active,
        permissionCodes: form.permissionCodes,
      });
    },
    onSuccess: () => router.push("/admin/roles"),
  });

  const permissionsByModule = useMemo(() => {
    const groups = new Map<string, NonNullable<typeof permissionsQuery.data>["items"]>();
    (permissionsQuery.data?.items ?? []).forEach((permission) => {
      const module = permission.module || "Lainnya";
      groups.set(module, [...(groups.get(module) ?? []), permission]);
    });
    return Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [permissionsQuery.data]);

  const togglePermission = (permissionCode: string) => {
    setForm((current) => ({
      ...current,
      permissionCodes: current.permissionCodes.includes(permissionCode)
        ? current.permissionCodes.filter((item) => item !== permissionCode)
        : [...current.permissionCodes, permissionCode],
    }));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    mode === "create" ? createMutation.mutate() : updateMutation.mutate();
  };

  const isLoading = roleQuery.isLoading || permissionsQuery.isLoading;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const error = roleQuery.error || permissionsQuery.error || createMutation.error || updateMutation.error;

  if (isLoading) return <LoadingSkeleton rows={8} />;

  return (
    <TailAdminCard title={mode === "create" ? "Tambah Role" : "Edit Role"} description="Kelola role dan daftar permission yang dimiliki role tersebut.">
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid gap-4 md:grid-cols-2">
          <label className="block text-sm font-medium text-gray-700">
            Kode Role
            <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="ADMIN" />
          </label>
          <label className="block text-sm font-medium text-gray-700">
            Nama Role
            <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Administrator" />
          </label>
          <label className="block text-sm font-medium text-gray-700 md:col-span-2">
            Deskripsi
            <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-24 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" />
          </label>
        </div>

        <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
          <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
          Aktif
        </label>

        <div>
          <p className="mb-2 text-sm font-medium text-gray-700">Permission</p>
          <div className="max-h-[520px] space-y-4 overflow-y-auto rounded-lg border border-gray-200 p-4">
            {permissionsByModule.map(([module, permissions]) => (
              <div key={module}>
                <p className="mb-2 text-xs font-semibold uppercase text-gray-500">{module}</p>
                <div className="flex flex-wrap gap-2">
                  {permissions.map((permission) => (
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

        {error ? <ErrorMessage message="Gagal menyimpan data role. Periksa input dan permission admin." /> : null}

        <div className="flex gap-2">
          <TailAdminButton type="submit" disabled={isSubmitting}>{mode === "create" ? "Simpan Role" : "Perbarui Role"}</TailAdminButton>
          <TailAdminButton variant="secondary" onClick={() => router.push("/admin/roles")}>Batal</TailAdminButton>
        </div>
      </form>
    </TailAdminCard>
  );
}
