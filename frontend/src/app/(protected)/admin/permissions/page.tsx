"use client";

import { FormEvent, useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { ConfirmModal, TailAdminBadge, TailAdminButton, TailAdminCard } from "@/app/components/tailadmin";
import { adminRbacService } from "@/features/admin-rbac/admin-rbac.service";
import { AdminPermission } from "@/features/admin-rbac/admin-rbac.types";

const ADMIN_RBAC_PERMISSIONS_KEY = ["admin-rbac", "permissions"] as const;
type SortDirection = "asc" | "desc";

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
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [moduleFilter, setModuleFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("module");
  const [direction, setDirection] = useState<SortDirection>("asc");
  const [selectedPermission, setSelectedPermission] = useState<AdminPermission | null>(null);
  const [pendingDeletePermission, setPendingDeletePermission] = useState<AdminPermission | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<PermissionFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const permissionsQuery = useQuery({
    queryKey: [...ADMIN_RBAC_PERMISSIONS_KEY, { search, activeParam, moduleFilter, page, size, sort, direction }],
    queryFn: () => adminRbacService.listPermissions({ search, active: activeParam, module: moduleFilter, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const allModulesQuery = useQuery({
    queryKey: [...ADMIN_RBAC_PERMISSIONS_KEY, "modules"],
    queryFn: () => adminRbacService.listPermissions({ size: 100, sort: "module", direction: "asc" }),
    placeholderData: keepPreviousData,
  });

  const invalidatePermissions = async () => queryClient.invalidateQueries({ queryKey: ADMIN_RBAC_PERMISSIONS_KEY });

  const createMutation = useMutation({
    mutationFn: () => adminRbacService.createPermission({
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      description: form.description.trim() || null,
      module: form.module.trim() || null,
      active: form.active,
    }),
    onSuccess: async () => {
      setForm(emptyForm);
      setIsFormOpen(false);
      setPage(0);
      await invalidatePermissions();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedPermission) throw new Error("Permission belum dipilih");
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
      setIsFormOpen(false);
      await invalidatePermissions();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminRbacService.deletePermission(id),
    onSuccess: async () => {
      setPendingDeletePermission(null);
      await invalidatePermissions();
    },
  });

  const permissions = permissionsQuery.data?.items ?? [];
  const modules = Array.from(new Set((allModulesQuery.data?.items ?? []).map((item) => item.module).filter(Boolean) as string[])).sort();
  const isRefetching = permissionsQuery.isFetching && !permissionsQuery.isLoading;
  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;
  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  const resetPage = () => setPage(0);
  const changeSort = (field: string) => {
    setPage(0);
    if (sort === field) {
      setDirection((current) => (current === "asc" ? "desc" : "asc"));
      return;
    }
    setSort(field);
    setDirection("asc");
  };
  const sortLabel = (field: string) => (sort === field ? (direction === "asc" ? " ▲" : " ▼") : "");

  const openCreateModal = () => {
    setSelectedPermission(null);
    setForm(emptyForm);
    setIsFormOpen(true);
  };

  const openEditModal = (permission: AdminPermission) => {
    setSelectedPermission(permission);
    setForm(buildFormFromPermission(permission));
    setIsFormOpen(true);
  };

  const closeFormModal = () => {
    setSelectedPermission(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedPermission ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions={["PERMISSION_READ", "PERMISSION_WRITE"]} mode="any">
      <AppShell>
        <PageHeader
          title="Manajemen Permission"
          description="Kelola permission yang dipakai untuk RBAC dan akses menu."
          actions={<TailAdminButton onClick={openCreateModal}>Tambah Permission</TailAdminButton>}
        />

        <TailAdminCard title="Daftar Permission" description="Data menggunakan server-side slice, pencarian, sorting, dan filtering.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_150px_170px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari permission..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={moduleFilter} onChange={(event) => { setModuleFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Modul</option>{modules.map((module) => <option key={module} value={module}>{module}</option>)}</select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>

          {permissionsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {permissionsQuery.isError ? <ErrorMessage message="Gagal memuat data permission." /> : null}
          {!permissionsQuery.isLoading && permissions.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah permission baru." /> : null}

          {permissions.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Kode{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Nama{sortLabel("name")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("module")}>Modul{sortLabel("module")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{permissions.map((permission) => <tr key={permission.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{permission.code}</td><td className="px-3 py-3"><p className="text-gray-700">{permission.name}</p>{permission.description ? <p className="text-xs text-gray-500">{permission.description}</p> : null}</td><td className="px-3 py-3"><TailAdminBadge tone="default">{permission.module || "Lainnya"}</TailAdminBadge></td><td className="px-3 py-3"><TailAdminBadge tone={permission.active ? "success" : "warning"}>{permission.active ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEditModal(permission)}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDeletePermission(permission)} disabled={deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}

          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!permissionsQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!permissionsQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi permission gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? (
          <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm">
            <button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeFormModal} />
            <div className="relative w-full max-w-xl rounded-2xl border border-gray-200 bg-white p-6 shadow-xl">
              <h3 className="text-lg font-semibold text-gray-900">{selectedPermission ? "Edit Permission" : "Tambah Permission"}</h3>
              <p className="mt-1 text-sm text-gray-500">Form permission cukup ringan, sehingga ditampilkan sebagai popup modal.</p>
              <form onSubmit={handleSubmit} className="mt-5 space-y-4">
                <label className="block text-sm font-medium text-gray-700">Kode<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="USER_READ" /></label>
                <label className="block text-sm font-medium text-gray-700">Nama<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Baca Data Pengguna" /></label>
                <label className="block text-sm font-medium text-gray-700">Modul<input value={form.module} onChange={(event) => setForm({ ...form, module: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Admin" /></label>
                <label className="block text-sm font-medium text-gray-700">Deskripsi<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label>
                <label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Aktif</label>
                <div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeFormModal} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedPermission ? "Perbarui Permission" : "Simpan Permission"}</TailAdminButton></div>
              </form>
            </div>
          </div>
        ) : null}

        <ConfirmModal open={Boolean(pendingDeletePermission)} title="Hapus permission?" description={<span>Permission <strong>{pendingDeletePermission?.code}</strong> akan dihapus. Role yang memakai permission ini bisa kehilangan akses terkait.</span>} confirmLabel="Ya, hapus permission" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDeletePermission(null)} onConfirm={() => pendingDeletePermission && deleteMutation.mutate(pendingDeletePermission.id)} />
      </AppShell>
    </RequirePermission>
  );
}
