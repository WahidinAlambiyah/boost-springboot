"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import { academyService } from "@/features/academies/academy.service";
import CoachProfileForm from "@/features/coach-profiles/components/coach-profile-form";
import CoachProfileTable from "@/features/coach-profiles/components/coach-profile-table";
import { CoachProfileFormSubmitValues } from "@/features/coach-profiles/coach-profile.schema";
import { coachProfileService } from "@/features/coach-profiles/coach-profile.service";
import { CoachProfile } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function CoachProfilesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const [academyId, setAcademyId] = useState("");
  const [selectedProfile, setSelectedProfile] = useState<CoachProfile | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const canWrite = useMemo(() => can(authorities, "COACH_WRITE"), [authorities]);

  const academiesQuery = useQuery({ queryKey: QUERY_KEYS.academies.list(), queryFn: () => academyService.list() });
  const usersQuery = useQuery({ queryKey: ["users", "list"] as const, queryFn: () => coachProfileService.listUsers() });
  const profilesQuery = useQuery({ queryKey: QUERY_KEYS.coachProfiles.byAcademy(academyId || undefined), queryFn: () => coachProfileService.list({ academyId: academyId || undefined }) });

  const invalidateProfiles = async () => queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachProfiles.all });

  const createMutation = useMutation({ mutationFn: (values: CoachProfileFormSubmitValues) => coachProfileService.create(values), onSuccess: async () => { setFormVersion((prev) => prev + 1); await invalidateProfiles(); } });
  const updateMutation = useMutation({ mutationFn: ({ id, values }: { id: string; values: CoachProfileFormSubmitValues }) => coachProfileService.update(id, values), onSuccess: async () => { setSelectedProfile(null); setFormVersion((prev) => prev + 1); await invalidateProfiles(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => coachProfileService.remove(id), onSuccess: invalidateProfiles });

  return <RequirePermission permissions="COACH_READ"><AppShell><h1 className="text-2xl font-semibold text-zinc-900">Coach Profiles</h1><div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4"><label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Filter Academy</span><select value={academyId} onChange={(event) => setAcademyId(event.target.value)} className="w-full rounded-md border border-zinc-300 px-3 py-2"><option value="">Semua academy</option>{academiesQuery.data?.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label></div><div className="mt-6">{profilesQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}{profilesQuery.isError ? <ErrorMessage message="Gagal memuat coach profiles." /> : null}{profilesQuery.data && profilesQuery.data.length === 0 ? <EmptyState title="Belum ada coach profile" description="Silakan tambah coach profile baru." /> : null}{profilesQuery.data && profilesQuery.data.length > 0 ? <CoachProfileTable profiles={profilesQuery.data} academies={academiesQuery.data ?? []} canWrite={canWrite} onEdit={setSelectedProfile} onDelete={(profile) => deleteMutation.mutate(profile.id)} /> : null}{(createMutation.error || updateMutation.error || deleteMutation.error) ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}</div><div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4"><h2 className="text-lg font-semibold text-zinc-900">{selectedProfile ? "Edit Coach" : "Tambah Coach"}</h2><div className="mt-3"><CoachProfileForm key={`coach-form-${selectedProfile?.id ?? "new"}-${formVersion}`} academies={academiesQuery.data ?? []} users={usersQuery.data ?? []} initialData={selectedProfile} canWrite={canWrite} isSubmitting={createMutation.isPending || updateMutation.isPending} onCancelEdit={() => setSelectedProfile(null)} onSubmit={(values) => selectedProfile ? updateMutation.mutate({ id: selectedProfile.id, values }) : createMutation.mutate(values)} /></div></div></AppShell></RequirePermission>;
}
