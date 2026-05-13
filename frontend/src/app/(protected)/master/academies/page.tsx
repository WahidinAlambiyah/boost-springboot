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
import { academyService } from "@/features/academies/academy.service";
import { Academy } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";

interface AcademyFormState {
  code: string;
  name: string;
  description: string;
  phone: string;
  email: string;
  isActive: boolean;
}

const emptyForm: AcademyFormState = {
  code: "",
  name: "",
  description: "",
  phone: "",
  email: "",
  isActive: true,
};

const buildForm = (academy: Academy): AcademyFormState => ({
  code: academy.code,
  name: academy.name,
  description: academy.description ?? "",
  phone: academy.phone ?? "",
  email: academy.email ?? "",
  isActive: academy.isActive,
});

export default function MasterAcademiesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "ACADEMY_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [selectedAcademy, setSelectedAcademy] = useState<Academy | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Academy | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<AcademyFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", { search, activeParam, page, size, sort, direction }],
    queryFn: () => academyService.slice({ search, active: activeParam, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "academies"] });

  const createMutation = useMutation({
    mutationFn: () => academyService.create({
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      phone: form.phone.trim() || undefined,
      email: form.email.trim() || undefined,
      isActive: form.isActive,
    }),
    onSuccess: async () => {
      setIsFormOpen(false);
      setForm(emptyForm);
      setPage(0);
      await invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedAcademy) throw new Error("Akademi belum dipilih");
      return academyService.update(selectedAcademy.id, {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        isActive: form.isActive,
      });
    },
    onSuccess: async () => {
      setSelectedAcademy(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => academyService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const rows = academiesQuery.data?.items ?? [];
  const isRefetching = academiesQuery.isFetching && !academiesQuery.isLoading;
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
  const openCreate = () => {
    setSelectedAcademy(null);
    setForm(emptyForm);
    setIsFormOpen(true);
  };
  const openEdit = (academy: Academy) => {
    setSelectedAcademy(academy);
    setForm(buildForm(academy));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedAcademy(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedAcademy ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="ACADEMY_READ">
      <AppShell>
        <PageHeader
          title="Master Akademi"
          description="Kelola data akademi/cabang utama yang digunakan di seluruh operasional latihan."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Akademi</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Akademi" description="Data menggunakan pola seperti Manajemen Pengguna: pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari kode / nama akademi..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {academiesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {academiesQuery.isError ? <ErrorMessage message="Gagal memuat data akademi." /> : null}
          {!academiesQuery.isLoading && rows.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah akademi baru." /> : null}
          {rows.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Kode{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Nama{sortLabel("name")}</button></th><th className="px-3 py-3">Kontak</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{rows.map((academy) => <tr key={academy.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{academy.code}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{academy.name}</p>{academy.description ? <p className="text-xs text-gray-500">{academy.description}</p> : null}</td><td className="px-3 py-3 text-xs text-gray-500"><p>{academy.phone || "-"}</p><p>{academy.email || "-"}</p></td><td className="px-3 py-3"><TailAdminBadge tone={academy.isActive ? "success" : "warning"}>{academy.isActive ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(academy)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(academy)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!academiesQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!academiesQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi akademi gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative w-full max-w-xl rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedAcademy ? "Edit Akademi" : "Tambah Akademi"}</h3><p className="mt-1 text-sm text-gray-500">Form akademi cukup ringan, sehingga ditampilkan sebagai popup modal.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Kode<input required disabled={Boolean(selectedAcademy)} value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase disabled:bg-gray-100" placeholder="BINTARO" /></label><label className="block text-sm font-medium text-gray-700">Nama<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Pushbike Academy Bintaro" /></label><label className="block text-sm font-medium text-gray-700">Deskripsi<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Telepon<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Email<input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.isActive} onChange={(event) => setForm({ ...form, isActive: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedAcademy ? "Perbarui Akademi" : "Simpan Akademi"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus akademi?" description={<span>Akademi <strong>{pendingDelete?.name}</strong> akan dihapus. Pastikan tidak ada data operasional aktif yang masih bergantung pada akademi ini.</span>} confirmLabel="Ya, hapus akademi" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}
