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
import { academyLocationService } from "@/features/academy-locations/academy-location.service";
import { academyService } from "@/features/academies/academy.service";
import { AcademyLocation } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";

interface LocationFormState {
  academyId: string;
  code: string;
  name: string;
  address: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  timezone: string;
  phone: string;
  googleMapsUrl: string;
  isActive: boolean;
}

const emptyForm: LocationFormState = {
  academyId: "",
  code: "",
  name: "",
  address: "",
  city: "",
  state: "",
  postalCode: "",
  country: "Indonesia",
  timezone: "Asia/Jakarta",
  phone: "",
  googleMapsUrl: "",
  isActive: true,
};

const buildForm = (location: AcademyLocation): LocationFormState => ({
  academyId: location.academyId,
  code: location.code,
  name: location.name,
  address: location.address ?? "",
  city: location.city ?? "",
  state: location.state ?? "",
  postalCode: location.postalCode ?? "",
  country: location.country ?? "Indonesia",
  timezone: location.timezone ?? "Asia/Jakarta",
  phone: location.phone ?? "",
  googleMapsUrl: "",
  isActive: location.isActive,
});

export default function MasterAcademyLocationsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "LOCATION_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [academyFilter, setAcademyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [selectedLocation, setSelectedLocation] = useState<AcademyLocation | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AcademyLocation | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<LocationFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", "location-options"],
    queryFn: () => academyService.slice({ size: 100, sort: "name", direction: "asc", active: true }),
    placeholderData: keepPreviousData,
  });

  const locationsQuery = useQuery({
    queryKey: ["master", "academy-locations", { search, academyFilter, activeParam, page, size, sort, direction }],
    queryFn: () => academyLocationService.slice({ search, academyId: academyFilter, active: activeParam, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "academy-locations"] });

  const createMutation = useMutation({
    mutationFn: () => academyLocationService.create({
      academyId: form.academyId,
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      address: form.address.trim() || undefined,
      city: form.city.trim() || undefined,
      state: form.state.trim() || undefined,
      postalCode: form.postalCode.trim() || undefined,
      country: form.country.trim() || undefined,
      timezone: form.timezone.trim() || undefined,
      phone: form.phone.trim() || undefined,
      googleMapsUrl: form.googleMapsUrl.trim() || undefined,
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
      if (!selectedLocation) throw new Error("Lokasi akademi belum dipilih");
      return academyLocationService.update(selectedLocation.id, {
        academyId: form.academyId,
        name: form.name.trim(),
        address: form.address.trim() || undefined,
        city: form.city.trim() || undefined,
        state: form.state.trim() || undefined,
        postalCode: form.postalCode.trim() || undefined,
        country: form.country.trim() || undefined,
        timezone: form.timezone.trim() || undefined,
        phone: form.phone.trim() || undefined,
        googleMapsUrl: form.googleMapsUrl.trim() || undefined,
        isActive: form.isActive,
      });
    },
    onSuccess: async () => {
      setSelectedLocation(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => academyLocationService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const rows = locationsQuery.data?.items ?? [];
  const academies = academiesQuery.data?.items ?? [];
  const academyNameById = useMemo(() => new Map(academies.map((academy) => [academy.id, academy.name])), [academies]);
  const isRefetching = locationsQuery.isFetching && !locationsQuery.isLoading;
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
    setSelectedLocation(null);
    setForm({ ...emptyForm, academyId: academyFilter || academies[0]?.id || "" });
    setIsFormOpen(true);
  };
  const openEdit = (location: AcademyLocation) => {
    setSelectedLocation(location);
    setForm(buildForm(location));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedLocation(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedLocation ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="LOCATION_READ">
      <AppShell>
        <PageHeader
          title="Lokasi Akademi"
          description="Kelola lokasi/cabang latihan untuk setiap akademi."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Lokasi</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Lokasi Akademi" description="Data menggunakan pola seperti Manajemen Pengguna: pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_220px_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari kode / nama / kota lokasi..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={academyFilter} onChange={(event) => { setAcademyFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {locationsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {locationsQuery.isError ? <ErrorMessage message="Gagal memuat data lokasi akademi." /> : null}
          {!locationsQuery.isLoading && rows.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah lokasi baru." /> : null}
          {rows.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Kode{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Nama Lokasi{sortLabel("name")}</button></th><th className="px-3 py-3">Akademi</th><th className="px-3 py-3">Alamat</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{rows.map((location) => <tr key={location.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{location.code}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{location.name}</p>{location.phone ? <p className="text-xs text-gray-500">{location.phone}</p> : null}</td><td className="px-3 py-3 text-sm text-gray-600">{academyNameById.get(location.academyId) ?? location.academyId}</td><td className="px-3 py-3 text-xs text-gray-500"><p>{location.address || "-"}</p><p>{[location.city, location.state].filter(Boolean).join(", ") || "-"}</p></td><td className="px-3 py-3"><TailAdminBadge tone={location.isActive ? "success" : "warning"}>{location.isActive ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(location)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(location)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!locationsQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!locationsQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi lokasi akademi gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedLocation ? "Edit Lokasi Akademi" : "Tambah Lokasi Akademi"}</h3><p className="mt-1 text-sm text-gray-500">Gunakan form ini untuk mengatur lokasi latihan per akademi.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Akademi<select required value={form.academyId} onChange={(event) => setForm({ ...form, academyId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Pilih Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Kode<input required disabled={Boolean(selectedLocation)} value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase disabled:bg-gray-100" placeholder="BTR" /></label><label className="block text-sm font-medium text-gray-700">Nama Lokasi<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Lapangan Bintaro" /></label></div><label className="block text-sm font-medium text-gray-700">Alamat<textarea value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><div className="grid gap-4 md:grid-cols-3"><label className="block text-sm font-medium text-gray-700">Kota<input value={form.city} onChange={(event) => setForm({ ...form, city: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Provinsi<input value={form.state} onChange={(event) => setForm({ ...form, state: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Kode Pos<input value={form.postalCode} onChange={(event) => setForm({ ...form, postalCode: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><div className="grid gap-4 md:grid-cols-3"><label className="block text-sm font-medium text-gray-700">Negara<input value={form.country} onChange={(event) => setForm({ ...form, country: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Zona Waktu<input value={form.timezone} onChange={(event) => setForm({ ...form, timezone: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Telepon<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="block text-sm font-medium text-gray-700">Google Maps URL<input value={form.googleMapsUrl} onChange={(event) => setForm({ ...form, googleMapsUrl: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="https://maps.google.com/..." /></label><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.isActive} onChange={(event) => setForm({ ...form, isActive: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedLocation ? "Perbarui Lokasi" : "Simpan Lokasi"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus lokasi akademi?" description={<span>Lokasi <strong>{pendingDelete?.name}</strong> akan dihapus. Pastikan tidak ada jadwal aktif yang masih memakai lokasi ini.</span>} confirmLabel="Ya, hapus lokasi" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}
