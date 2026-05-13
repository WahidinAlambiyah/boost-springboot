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
import { studentService, StudentRequest } from "@/features/students/student.service";
import { Student } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";

interface StudentFormState {
  academyId: string;
  studentNo: string;
  fullName: string;
  nickname: string;
  currentLevel: string;
  active: boolean;
}

const emptyForm: StudentFormState = {
  academyId: "",
  studentNo: "",
  fullName: "",
  nickname: "",
  currentLevel: "",
  active: true,
};

const buildForm = (student: Student): StudentFormState => ({
  academyId: student.academyId,
  studentNo: student.studentNo ?? "",
  fullName: student.fullName,
  nickname: student.nickname ?? "",
  currentLevel: student.currentLevel ?? "",
  active: student.status !== "INACTIVE",
});

export default function MasterStudentsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "STUDENT_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [academyFilter, setAcademyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("createdAt");
  const [direction, setDirection] = useState<SortDirection>("desc");
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Student | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<StudentFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", "student-options"],
    queryFn: () => academyService.slice({ size: 100, sort: "name", direction: "asc", active: true }),
    placeholderData: keepPreviousData,
  });

  const studentsQuery = useQuery({
    queryKey: ["master", "students", { search, academyFilter, activeParam, page, size, sort, direction }],
    queryFn: () => studentService.slice({ search, academyId: academyFilter, active: activeParam, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "students"] });

  const createMutation = useMutation({
    mutationFn: () => studentService.create(toPayload(form)),
    onSuccess: async () => {
      setIsFormOpen(false);
      setForm(emptyForm);
      setPage(0);
      await invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedStudent) throw new Error("Siswa belum dipilih");
      return studentService.update(selectedStudent.id, toPayload(form));
    },
    onSuccess: async () => {
      setSelectedStudent(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => studentService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const students = studentsQuery.data?.items ?? [];
  const academies = academiesQuery.data?.items ?? [];
  const academyNameById = useMemo(() => new Map(academies.map((academy) => [academy.id, academy.name])), [academies]);
  const isRefetching = studentsQuery.isFetching && !studentsQuery.isLoading;
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
    setSelectedStudent(null);
    setForm({ ...emptyForm, academyId: academyFilter || academies[0]?.id || "" });
    setIsFormOpen(true);
  };
  const openEdit = (student: Student) => {
    setSelectedStudent(student);
    setForm(buildForm(student));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedStudent(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedStudent ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="STUDENT_READ">
      <AppShell>
        <PageHeader
          title="Master Siswa"
          description="Kelola data siswa yang digunakan untuk absensi, penilaian, dan laporan perkembangan."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Siswa</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Siswa" description="Data menggunakan server-side slice, pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_220px_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari nomor / nama / level siswa..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={academyFilter} onChange={(event) => { setAcademyFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {studentsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {studentsQuery.isError ? <ErrorMessage message="Gagal memuat data siswa." /> : null}
          {!studentsQuery.isLoading && students.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah siswa baru." /> : null}
          {students.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("studentNo")}>No Siswa{sortLabel("studentNo")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("fullName")}>Nama{sortLabel("fullName")}</button></th><th className="px-3 py-3">Akademi</th><th className="px-3 py-3">Level</th><th className="px-3 py-3"><button onClick={() => changeSort("status")}>Status{sortLabel("status")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{students.map((student) => <tr key={student.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 font-medium text-gray-900">{student.studentNo || "-"}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{student.fullName}</p>{student.nickname ? <p className="text-xs text-gray-500">Panggilan: {student.nickname}</p> : null}</td><td className="px-3 py-3 text-sm text-gray-600">{academyNameById.get(student.academyId) ?? student.academyId}</td><td className="px-3 py-3 text-sm text-gray-600">{student.currentLevel || "-"}</td><td className="px-3 py-3"><TailAdminBadge tone={student.status === "INACTIVE" ? "warning" : "success"}>{student.status === "INACTIVE" ? "Nonaktif" : "Aktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(student)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(student)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!studentsQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!studentsQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi siswa gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative w-full max-w-2xl rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedStudent ? "Edit Siswa" : "Tambah Siswa"}</h3><p className="mt-1 text-sm text-gray-500">Lengkapi data dasar siswa untuk kebutuhan operasional latihan.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Akademi<select required value={form.academyId} onChange={(event) => setForm({ ...form, academyId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Pilih Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Nomor Siswa<input required value={form.studentNo} onChange={(event) => setForm({ ...form, studentNo: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="STD-001" /></label><label className="block text-sm font-medium text-gray-700">Nama Lengkap<input required value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Nama Panggilan<input value={form.nickname} onChange={(event) => setForm({ ...form, nickname: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Level Saat Ini<input value={form.currentLevel} onChange={(event) => setForm({ ...form, currentLevel: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" placeholder="Beginner" /></label></div><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedStudent ? "Perbarui Siswa" : "Simpan Siswa"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus siswa?" description={<span>Siswa <strong>{pendingDelete?.fullName}</strong> akan dihapus. Pastikan tidak ada data operasional aktif yang masih bergantung pada siswa ini.</span>} confirmLabel="Ya, hapus siswa" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}

function toPayload(form: StudentFormState): StudentRequest {
  return {
    academyId: form.academyId,
    studentNo: form.studentNo.trim().toUpperCase(),
    fullName: form.fullName.trim(),
    nickname: form.nickname.trim() || undefined,
    currentLevel: form.currentLevel.trim() || undefined,
    active: form.active,
  };
}
