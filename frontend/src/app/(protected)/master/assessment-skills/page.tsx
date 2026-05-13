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
import { assessmentSkillService } from "@/features/assessment-skills/assessment-skill.service";
import { AssessmentSkill } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

type SortDirection = "asc" | "desc";

interface SkillFormState {
  academyId: string;
  code: string;
  name: string;
  description: string;
  orderNo: string;
  maxScore: string;
  active: boolean;
}

const emptyForm: SkillFormState = {
  academyId: "",
  code: "",
  name: "",
  description: "",
  orderNo: "1",
  maxScore: "5",
  active: true,
};

const buildForm = (skill: AssessmentSkill): SkillFormState => ({
  academyId: skill.academyId ?? "",
  code: skill.code,
  name: skill.name,
  description: skill.description ?? "",
  orderNo: String(skill.orderNo ?? 1),
  maxScore: String(skill.maxScore ?? 5),
  active: skill.active,
});

export default function MasterAssessmentSkillsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "ASSESSMENT_WRITE"), [authorities]);
  const [search, setSearch] = useState("");
  const [academyFilter, setAcademyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState("orderNo");
  const [direction, setDirection] = useState<SortDirection>("asc");
  const [selectedSkill, setSelectedSkill] = useState<AssessmentSkill | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AssessmentSkill | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<SkillFormState>(emptyForm);
  const activeParam = activeFilter === "aktif" ? true : activeFilter === "nonaktif" ? false : null;

  const academiesQuery = useQuery({
    queryKey: ["master", "academies", "skill-options"],
    queryFn: () => academyService.slice({ size: 100, sort: "name", direction: "asc", active: true }),
    placeholderData: keepPreviousData,
  });

  const skillsQuery = useQuery({
    queryKey: ["master", "assessment-skills", { search, academyFilter, activeParam, page, size, sort, direction }],
    queryFn: () => assessmentSkillService.slice({ search, academyId: academyFilter, active: activeParam, page, size, sort, direction }),
    placeholderData: keepPreviousData,
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: ["master", "assessment-skills"] });

  const createMutation = useMutation({
    mutationFn: () => assessmentSkillService.create(toPayload(form)),
    onSuccess: async () => {
      setIsFormOpen(false);
      setForm(emptyForm);
      setPage(0);
      await invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      if (!selectedSkill) throw new Error("Skill penilaian belum dipilih");
      return assessmentSkillService.update(selectedSkill.id, toPayload(form));
    },
    onSuccess: async () => {
      setSelectedSkill(null);
      setIsFormOpen(false);
      setForm(emptyForm);
      await invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => assessmentSkillService.remove(id),
    onSuccess: async () => {
      setPendingDelete(null);
      await invalidate();
    },
  });

  const skills = skillsQuery.data?.items ?? [];
  const academies = academiesQuery.data?.items ?? [];
  const academyNameById = useMemo(() => new Map(academies.map((academy) => [academy.id, academy.name])), [academies]);
  const isRefetching = skillsQuery.isFetching && !skillsQuery.isLoading;
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
    setSelectedSkill(null);
    setForm({ ...emptyForm, academyId: academyFilter || "" });
    setIsFormOpen(true);
  };
  const openEdit = (skill: AssessmentSkill) => {
    setSelectedSkill(skill);
    setForm(buildForm(skill));
    setIsFormOpen(true);
  };
  const closeForm = () => {
    setSelectedSkill(null);
    setForm(emptyForm);
    setIsFormOpen(false);
  };
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    selectedSkill ? updateMutation.mutate() : createMutation.mutate();
  };

  return (
    <RequirePermission permissions="ASSESSMENT_READ">
      <AppShell>
        <PageHeader
          title="Skill Penilaian"
          description="Kelola indikator penilaian yang digunakan coach untuk menilai perkembangan siswa."
          actions={<TailAdminButton onClick={openCreate} disabled={!canWrite}>Tambah Skill</TailAdminButton>}
        />
        <TailAdminCard title="Daftar Skill Penilaian" description="Data menggunakan server-side slice, pencarian, filter, sorting, paging, dan konfirmasi aksi.">
          <div className="mb-4 grid gap-3 md:grid-cols-[1fr_220px_150px_120px]">
            <input value={search} onChange={(event) => { setSearch(event.target.value); resetPage(); }} placeholder="Cari kode / nama skill..." className="h-10 rounded-lg border border-gray-300 px-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100" />
            <select value={academyFilter} onChange={(event) => { setAcademyFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Global + Semua Akademi</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
            <select value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value="">Semua Status</option><option value="aktif">Aktif</option><option value="nonaktif">Nonaktif</option></select>
            <select value={size} onChange={(event) => { setSize(Number(event.target.value)); resetPage(); }} className="h-10 rounded-lg border border-gray-300 px-3 text-sm"><option value={10}>10 baris</option><option value={25}>25 baris</option><option value={50}>50 baris</option></select>
          </div>
          {skillsQuery.isLoading ? <LoadingSkeleton rows={7} /> : null}
          {skillsQuery.isError ? <ErrorMessage message="Gagal memuat data skill penilaian." /> : null}
          {!skillsQuery.isLoading && skills.length === 0 ? <EmptyState title="Data tidak ditemukan" description="Ubah pencarian/filter atau tambah skill penilaian baru." /> : null}
          {skills.length > 0 ? <div className="relative overflow-x-auto rounded-xl border border-gray-100">{isRefetching ? <div className="absolute inset-x-0 top-0 h-1 animate-pulse bg-blue-500" /> : null}<table className="min-w-full text-left text-sm"><thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500"><tr><th className="px-3 py-3"><button onClick={() => changeSort("orderNo")}>Urutan{sortLabel("orderNo")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("code")}>Kode{sortLabel("code")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("name")}>Nama Skill{sortLabel("name")}</button></th><th className="px-3 py-3">Akademi</th><th className="px-3 py-3"><button onClick={() => changeSort("maxScore")}>Max Score{sortLabel("maxScore")}</button></th><th className="px-3 py-3"><button onClick={() => changeSort("active")}>Status{sortLabel("active")}</button></th><th className="px-3 py-3 text-right">Aksi</th></tr></thead><tbody className="divide-y divide-gray-100">{skills.map((skill) => <tr key={skill.id} className={isRefetching ? "opacity-60" : ""}><td className="px-3 py-3 text-sm text-gray-700">{skill.orderNo ?? "-"}</td><td className="px-3 py-3 font-medium text-gray-900">{skill.code}</td><td className="px-3 py-3"><p className="font-medium text-gray-900">{skill.name}</p>{skill.description ? <p className="text-xs text-gray-500">{skill.description}</p> : null}</td><td className="px-3 py-3 text-sm text-gray-600">{skill.academyId ? academyNameById.get(skill.academyId) ?? skill.academyId : "Global"}</td><td className="px-3 py-3 text-sm text-gray-700">{skill.maxScore ?? 5}</td><td className="px-3 py-3"><TailAdminBadge tone={skill.active ? "success" : "warning"}>{skill.active ? "Aktif" : "Nonaktif"}</TailAdminBadge></td><td className="px-3 py-3"><div className="flex justify-end gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => openEdit(skill)} disabled={!canWrite}>Edit</TailAdminButton><TailAdminButton size="sm" variant="danger" onClick={() => setPendingDelete(skill)} disabled={!canWrite || deleteMutation.isPending}>Hapus</TailAdminButton></div></td></tr>)}</tbody></table></div> : null}
          <div className="mt-4 flex flex-col gap-3 text-sm text-gray-600 sm:flex-row sm:items-center sm:justify-between"><span>Halaman {page + 1} · Maksimal {size} baris {isRefetching ? "· Memperbarui..." : ""}</span><div className="flex gap-2"><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={!skillsQuery.data?.hasPrevious}>Sebelumnya</TailAdminButton><TailAdminButton size="sm" variant="secondary" onClick={() => setPage((current) => current + 1)} disabled={!skillsQuery.data?.hasNext}>Berikutnya</TailAdminButton></div></div>
          {mutationError ? <ErrorMessage message="Aksi skill penilaian gagal diproses. Periksa input atau permission admin." /> : null}
        </TailAdminCard>

        {isFormOpen ? <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm"><button type="button" aria-label="Tutup modal" className="absolute inset-0 cursor-default" onClick={isSubmitting ? undefined : closeForm} /><div className="relative max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-gray-200 bg-white p-6 shadow-xl"><h3 className="text-lg font-semibold text-gray-900">{selectedSkill ? "Edit Skill Penilaian" : "Tambah Skill Penilaian"}</h3><p className="mt-1 text-sm text-gray-500">Gunakan akademi kosong untuk membuat skill global yang dapat dipakai semua akademi.</p><form onSubmit={handleSubmit} className="mt-5 space-y-4"><label className="block text-sm font-medium text-gray-700">Akademi<select value={form.academyId} onChange={(event) => setForm({ ...form, academyId: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Global</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Kode<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase" placeholder="BALANCE" /></label><label className="block text-sm font-medium text-gray-700">Nama Skill<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="block text-sm font-medium text-gray-700">Deskripsi<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} className="mt-1 min-h-20 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><div className="grid gap-4 md:grid-cols-2"><label className="block text-sm font-medium text-gray-700">Urutan<input type="number" min={0} value={form.orderNo} onChange={(event) => setForm({ ...form, orderNo: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label><label className="block text-sm font-medium text-gray-700">Nilai Maksimal<input required type="number" min={1} value={form.maxScore} onChange={(event) => setForm({ ...form, maxScore: event.target.value })} className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" /></label></div><label className="flex items-center gap-2 text-sm font-medium text-gray-700"><input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />Aktif</label><div className="flex justify-end gap-2"><TailAdminButton variant="secondary" onClick={closeForm} disabled={isSubmitting}>Batal</TailAdminButton><TailAdminButton type="submit" disabled={isSubmitting}>{selectedSkill ? "Perbarui Skill" : "Simpan Skill"}</TailAdminButton></div></form></div></div> : null}
        <ConfirmModal open={Boolean(pendingDelete)} title="Hapus skill penilaian?" description={<span>Skill <strong>{pendingDelete?.name}</strong> akan dihapus. Pastikan tidak ada data penilaian aktif yang masih bergantung pada skill ini.</span>} confirmLabel="Ya, hapus skill" tone="danger" loading={deleteMutation.isPending} onCancel={() => setPendingDelete(null)} onConfirm={() => pendingDelete && deleteMutation.mutate(pendingDelete.id)} />
      </AppShell>
    </RequirePermission>
  );
}

function toPayload(form: SkillFormState) {
  return {
    academyId: form.academyId || undefined,
    code: form.code.trim().toUpperCase(),
    name: form.name.trim(),
    description: form.description.trim() || undefined,
    orderNo: form.orderNo ? Number(form.orderNo) : undefined,
    active: form.active,
    maxScore: Number(form.maxScore || 5),
  };
}
