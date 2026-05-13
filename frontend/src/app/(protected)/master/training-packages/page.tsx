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
import { trainingPackageService } from "@/features/training-packages/training-package.service";
import { TrainingPackage } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";
type PackageType = "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";

interface PackageFormState {
  academyId: string;
  code: string;
  name: string;
  packageType: PackageType;
  price: string;
  sessionQuota: string;
  validityDays: string;
  description: string;
  active: boolean;
}

const emptyForm: PackageFormState = {
  academyId: "",
  code: "",
  name: "",
  packageType: "MONTHLY",
  price: "0",
  sessionQuota: "0",
  validityDays: "30",
  description: "",
  active: true,
};

const packageTypeLabel: Record<PackageType, string> = {
  TRIAL: "Trial",
  PER_SESSION: "Per Sesi",
  MONTHLY: "Bulanan",
  SESSION_BUNDLE: "Paket Sesi",
};

const buildForm = (item: TrainingPackage): PackageFormState => ({
  academyId: item.academyId,
  code: item.code,
  name: item.name,
  packageType: item.packageType,
  price: String(item.price ?? 0),
  sessionQuota: String(item.sessionQuota ?? 0),
  validityDays: String(item.validityDays ?? 30),
  description: item.description ?? "",
  active: item.isActive !== false,
});

export default function MasterTrainingPackagesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "PACKAGE_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [academyFilter, setAcademyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [selectedPackage, setSelectedPackage] = useState<TrainingPackage | null>(null);
  const [pendingDelete, setPendingDelete] = useState<TrainingPackage | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<PackageFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", "package-options"],
    queryFn: () => academyService.slice({ size: 100, sort: "name", direction: "asc", active: true }),
    placeholderData: keepPreviousData,
  });

  const packagesQuery = useQuery({
    queryKey: ["master", "training-packages", { search, academyFilter, activeParam, typeFilter, page, size, sort, direction }],
    queryFn: () => trainingPackageService.slice({ search, academyId: academyFilter, active: activeParam, packageType: typeFilter, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "training-packages"] });

  const createMutation = useMutation({
    mutationFn: () => trainingPackageService.create(toPayload(form)),
    onSuccess: async () => {
      setIsFormOpen(false);
      setForm(emptyForm);
      setPage(0);
      await invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedPackage) throw new Error("Paket latihan belum dipilih");
      return trainingPackageService.update(selectedPackage.id, toPayload(form));
    },
    onSuccess: async () => {
      setSelectedPackage(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => trainingPackageService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const packages = packagesQuery.data?.items ?? [];
  const academies = academiesQuery.data?.items ?? [];
  const academyNameById = useMemo(() => new Map(academies.map((academy) => [academy.id, academy.name])), [academies]);
  const isRefetching = packagesQuery.isFetching && !packagesQuery.isLoading;
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
    setSelectedPackage(null);
    setForm({ ...emptyForm, academyId: academyFilter || academies[0]?.id || "" });
    setIsFormOpen(true);
  };
  const openEdit = (item: TrainingPackage) => {
    setSelectedPackage(item);
    setForm(buildForm(item));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedPackage(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedPackage ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="PACKAGE_READ">
      <AppShell>
        <PageHeader
          title="Paket Latihan"
          description="Kelola paket latihan yang digunakan untuk enrollment, tagihan, dan kuota sesi siswa."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Paket</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Paket Latihan" description="Data menggunakan server-side slice, pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_210px_160px_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari kode / nama paket..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={academyFilter} onChange={(event) => { setAcademyFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
            <select value={typeFilter} onChange={(event) => { setTypeFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Tipe</option>{Object.entries(packageTypeLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {packagesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {packagesQuery.isError ? <ErrorMessage message="Gagal memuat data paket latihan." /> : null}
          {!packagesQuery.isLoading && packages.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah paket latihan baru." /> : null}
          {packages.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Kode{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Nama Paket{sortLabel("name")}</button></th><th className="px-3 py-3">Akademi</th><th className="px-3 py-3"><button onClick={() => changeSort("packageType")}>Tipe{sortLabel("packageType")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("price")}>Harga{sortLabel("price")}</button></th><th className="px-3 py-3">Kuota</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{packages.map((item) => <tr key={item.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{item.code}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{item.name}</p>{item.description ? <p className="text-xs text-gray-500">{item.description}</p> : null}</td><td className="px-3 py-3 text-sm text-gray-600">{academyNameById.get(item.academyId) ?? item.academyId}</td><td className="px-3 py-3"><TailAdminBadge tone="info">{packageTypeLabel[item.packageType]}</TailAdminBadge></td><td className="px-3 py-3 text-sm text-gray-700">Rp {Number(item.price ?? 0).toLocaleString("id-ID")}</td><td className="px-3 py-3 text-sm text-gray-600">{item.sessionQuota ?? 0} sesi · {item.validityDays ?? "-"} hari</td><td className="px-3 py-3"><TailAdminBadge tone={item.isActive !== false ? "success" : "warning"}>{item.isActive !== false ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(item)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(item)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!packagesQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!packagesQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi paket latihan gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedPackage ? "Edit Paket Latihan" : "Tambah Paket Latihan"}</h3><p className="mt-1 text-sm text-gray-500">Lengkapi detail paket untuk kebutuhan penjualan dan enrollment.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Akademi<select required value={form.academyId} onChange={(event) => setForm({ ...form, academyId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Pilih Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Kode<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="MONTHLY-4" /></label><label className="block text-sm font-medium text-gray-700">Nama Paket<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Tipe Paket<select value={form.packageType} onChange={(event) => setForm({ ...form, packageType: event.target.value as PackageType })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm">{Object.entries(packageTypeLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label><label className="block text-sm font-medium text-gray-700">Harga<input required type="number" min={0} value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Kuota Sesi<input required type="number" min={0} value={form.sessionQuota} onChange={(event) => setForm({ ...form, sessionQuota: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Masa Berlaku Hari<input type="number" min={0} value={form.validityDays} onChange={(event) => setForm({ ...form, validityDays: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="block text-sm font-medium text-gray-700">Deskripsi<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedPackage ? "Perbarui Paket" : "Simpan Paket"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus paket latihan?" description={<span>Paket <strong>{pendingDelete?.name}</strong> akan dihapus. Pastikan tidak ada enrollment/tagihan aktif yang masih bergantung pada paket ini.</span>} confirmLabel="Ya, hapus paket" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}

function toPayload(form: PackageFormState) {
  return {
    academyId: form.academyId,
    code: form.code.trim().toUpperCase(),
    name: form.name.trim(),
    packageType: form.packageType,
    price: Number(form.price || 0),
    sessionQuota: Number(form.sessionQuota || 0),
    validityDays: form.validityDays ? Number(form.validityDays) : undefined,
    description: form.description.trim() || undefined,
    active: form.active,
  };
}
