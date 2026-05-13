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
import { coachProfileService } from "@/features/coach-profiles/coach-profile.service";
import { CoachProfile } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";

type EmploymentType = "FULL_TIME" | "PART_TIME" | "CONTRACT";
type PayType = "SALARY" | "HOURLY" | "PER_SESSION";

interface CoachFormState {
  academyId: string;
  userId: string;
  coachNo: string;
  fullName: string;
  phone: string;
  specialties: string;
  bio: string;
  employmentType: EmploymentType;
  payType: PayType;
  active: boolean;
}

const emptyForm: CoachFormState = {
  academyId: "",
  userId: "",
  coachNo: "",
  fullName: "",
  phone: "",
  specialties: "",
  bio: "",
  employmentType: "PART_TIME",
  payType: "PER_SESSION",
  active: true,
};

const buildForm = (coach: CoachProfile): CoachFormState => ({
  academyId: coach.academyId,
  userId: coach.userId,
  coachNo: coach.coachNo ?? "",
  fullName: coach.fullName,
  phone: coach.phone ?? "",
  specialties: coach.specialties ?? "",
  bio: coach.bio ?? "",
  employmentType: coach.employmentType,
  payType: coach.payType,
  active: coach.isActive,
});

export default function MasterCoachesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "COACH_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [academyFilter, setAcademyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [selectedCoach, setSelectedCoach] = useState<CoachProfile | null>(null);
  const [pendingDelete, setPendingDelete] = useState<CoachProfile | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<CoachFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", "coach-options"],
    queryFn: () => academyService.slice({ size: 100, sort: "name", direction: "asc", active: true }),
    placeholderData: keepPreviousData,
  });

  const usersQuery = useQuery({
    queryKey: ["master", "coach-user-options"],
    queryFn: () => coachProfileService.listUsers(),
    placeholderData: keepPreviousData,
  });

  const coachesQuery = useQuery({
    queryKey: ["master", "coaches", { search, academyFilter, activeParam, page, size, sort, direction }],
    queryFn: () => coachProfileService.slice({ search, academyId: academyFilter, active: activeParam, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "coaches"] });

  const createMutation = useMutation({
    mutationFn: () => coachProfileService.create(toPayload(form)),
    onSuccess: async () => {
      setIsFormOpen(false);
      setForm(emptyForm);
      setPage(0);
      await invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedCoach) throw new Error("Coach belum dipilih");
      return coachProfileService.update(selectedCoach.id, toPayload(form));
    },
    onSuccess: async () => {
      setSelectedCoach(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => coachProfileService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const coaches = coachesQuery.data?.items ?? [];
  const academies = academiesQuery.data?.items ?? [];
  const users = usersQuery.data ?? [];
  const academyNameById = useMemo(() => new Map(academies.map((academy) => [academy.id, academy.name])), [academies]);
  const userLabelById = useMemo(() => new Map(users.map((user) => [user.id, user.fullName || user.username || user.email || user.id])), [users]);
  const isRefetching = coachesQuery.isFetching && !coachesQuery.isLoading;
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
    setSelectedCoach(null);
    setForm({ ...emptyForm, academyId: academyFilter || academies[0]?.id || "", userId: users[0]?.id || "" });
    setIsFormOpen(true);
  };
  const openEdit = (coach: CoachProfile) => {
    setSelectedCoach(coach);
    setForm(buildForm(coach));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedCoach(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedCoach ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="COACH_READ">
      <AppShell>
        <PageHeader
          title="Master Coach"
          description="Kelola data coach yang digunakan untuk jadwal, absensi coach, payroll, dan penilaian siswa."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Coach</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Coach" description="Data menggunakan server-side slice, pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_220px_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari nomor / nama / spesialisasi coach..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={academyFilter} onChange={(event) => { setAcademyFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {coachesQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {coachesQuery.isError ? <ErrorMessage message="Gagal memuat data coach." /> : null}
          {!coachesQuery.isLoading && coaches.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah coach baru." /> : null}
          {coaches.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("coachNo")}>No Coach{sortLabel("coachNo")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("fullName")}>Nama{sortLabel("fullName")}</button></th><th className="px-3 py-3">Akademi</th><th className="px-3 py-3">Spesialisasi</th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{coaches.map((coach) => <tr key={coach.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{coach.coachNo || "-"}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{coach.fullName}</p><p className="text-xs text-gray-500">{coach.phone || "-"}</p></td><td className="px-3 py-3 text-sm text-gray-600">{academyNameById.get(coach.academyId) ?? coach.academyId}</td><td className="px-3 py-3 text-sm text-gray-600">{coach.specialties || "-"}</td><td className="px-3 py-3"><TailAdminBadge tone={coach.isActive ? "success" : "warning"}>{coach.isActive ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(coach)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(coach)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!coachesQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!coachesQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi coach gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedCoach ? "Edit Coach" : "Tambah Coach"}</h3><p className="mt-1 text-sm text-gray-500">Lengkapi data coach untuk kebutuhan operasional latihan dan payroll.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Akademi<select required value={form.academyId} onChange={(event) => setForm({ ...form, academyId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Pilih Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label><label className="block text-sm font-medium text-gray-700">Akun Pengguna<select required value={form.userId} onChange={(event) => setForm({ ...form, userId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Pilih Akun</option>{users.map((user) => <option key={user.id} value={user.id}>{userLabelById.get(user.id)}</option>)}</select></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Nomor Coach<input value={form.coachNo} onChange={(event) => setForm({ ...form, coachNo: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="COACH-001" /></label><label className="block text-sm font-medium text-gray-700">Nama Coach<input required value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="block text-sm font-medium text-gray-700">Telepon<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Spesialisasi<input value={form.specialties} onChange={(event) => setForm({ ...form, specialties: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Balance bike, beginner class" /></label><label className="block text-sm font-medium text-gray-700">Bio<textarea value={form.bio} onChange={(event) => setForm({ ...form, bio: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Tipe Kerja<select value={form.employmentType} onChange={(event) => setForm({ ...form, employmentType: event.target.value as EmploymentType })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="FULL_TIME">Full Time</option><option value="PART_TIME">Part Time</option><option value="CONTRACT">Kontrak</option></select></label><label className="block text-sm font-medium text-gray-700">Tipe Bayar<select value={form.payType} onChange={(event) => setForm({ ...form, payType: event.target.value as PayType })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="SALARY">Gaji Bulanan</option><option value="HOURLY">Per Jam</option><option value="PER_SESSION">Per Sesi</option></select></label></div><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedCoach ? "Perbarui Coach" : "Simpan Coach"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus coach?" description={<span>Coach <strong>{pendingDelete?.fullName}</strong> akan dihapus. Pastikan tidak ada jadwal aktif yang masih bergantung pada coach ini.</span>} confirmLabel="Ya, hapus coach" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}

function toPayload(form: CoachFormState) {
  return {
    academyId: form.academyId,
    userId: form.userId,
    coachNo: form.coachNo.trim() || undefined,
    fullName: form.fullName.trim(),
    phone: form.phone.trim() || undefined,
    specialties: form.specialties.trim() || undefined,
    bio: form.bio.trim() || undefined,
    employmentType: form.employmentType,
    payType: form.payType,
    active: form.active,
  };
}
