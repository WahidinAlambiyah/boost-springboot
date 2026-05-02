"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import { academyService } from "@/features/academies/academy.service";
import { academyLocationService } from "@/features/academy-locations/academy-location.service";
import ClassSessionForm from "@/features/class-sessions/components/class-session-form";
import SessionConflictAlert from "@/features/class-sessions/components/session-conflict-alert";
import { classSessionService } from "@/features/class-sessions/class-session.service";
import { ClassSession, ClassSessionCreateRequest, ClassSessionUpdateRequest } from "@/lib/api-types";
import { parseErrorMessage } from "@/lib/error-handler";
import { QUERY_KEYS } from "@/lib/query-keys";

export default function ClassSessionsPage() {
  const queryClient = useQueryClient();
  const [academyId, setAcademyId] = useState("");
  const [locationId, setLocationId] = useState("");
  const [sessionDate, setSessionDate] = useState("");
  const [classGroupId, setClassGroupId] = useState("");
  const [editingSession, setEditingSession] = useState<ClassSession | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [conflicts, setConflicts] = useState<Awaited<ReturnType<typeof classSessionService.getSessionConflicts>>>([]);

  const academiesQuery = useQuery({ queryKey: QUERY_KEYS.academies.list(), queryFn: () => academyService.list() });
  const locationsQuery = useQuery({ queryKey: QUERY_KEYS.academyLocations.byAcademy(academyId || undefined), queryFn: () => academyLocationService.list({ academyId: academyId || undefined }) });
  const sessionsQuery = useQuery({
    queryKey: QUERY_KEYS.classSessions.filter({ academyId: academyId || undefined, from: sessionDate || undefined, to: sessionDate || undefined }),
    queryFn: () => classSessionService.getClassSessions({ academyId: academyId || undefined, locationId: locationId || undefined, sessionDate: sessionDate || undefined }),
  });

  const conflictMutation = useMutation({
    mutationFn: (payload: { academyId: string; locationId: string; sessionDate: string; startTime: string; endTime: string; excludeSessionId?: string }) =>
      classSessionService.getSessionConflicts(payload),
    onSuccess: async (data, payload) => {
      setConflicts(data);
      await queryClient.setQueryData(
        QUERY_KEYS.classSessions.conflicts({
          academyId: payload.academyId,
          from: payload.sessionDate,
          to: payload.sessionDate,
        }),
        data,
      );
    },
  });

  const invalidateSessions = async () => {
    await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.classSessions.all });
  };

  const createMutation = useMutation({
    mutationFn: (payload: ClassSessionCreateRequest) => classSessionService.createClassSession(payload),
    onSuccess: async () => {
      setSubmitError(null);
      setConflicts([]);
      setClassGroupId("");
      await invalidateSessions();
    },
    onError: (error) => setSubmitError(parseErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ClassSessionUpdateRequest }) => classSessionService.updateClassSession(id, payload),
    onSuccess: async () => {
      setSubmitError(null);
      setConflicts([]);
      setEditingSession(null);
      await invalidateSessions();
    },
    onError: (error) => setSubmitError(parseErrorMessage(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => classSessionService.deleteClassSession(id),
    onSuccess: async () => {
      await invalidateSessions();
    },
    onError: (error) => setSubmitError(parseErrorMessage(error)),
  });

  const locationMap = useMemo(() => new Map((locationsQuery.data ?? []).map((location) => [location.id, location.name])), [locationsQuery.data]);

  return (
    <RequirePermission permissions="SCHEDULE_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Class Sessions</h1>
        <p className="mt-1 text-sm text-zinc-600">Kelola jadwal latihan dengan filter academy, lokasi, dan tanggal.</p>

        <section className="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-zinc-200 bg-white p-4 md:grid-cols-3">
          <label className="text-sm"><span className="mb-1 block font-medium text-zinc-700">Academy</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" value={academyId} onChange={(event) => setAcademyId(event.target.value)}><option value="">Semua academy</option>{(academiesQuery.data ?? []).map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label>
          <label className="text-sm"><span className="mb-1 block font-medium text-zinc-700">Lokasi</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" value={locationId} onChange={(event) => setLocationId(event.target.value)}><option value="">Semua lokasi</option>{(locationsQuery.data ?? []).map((location) => <option key={location.id} value={location.id}>{location.name}</option>)}</select></label>
          <label className="text-sm"><span className="mb-1 block font-medium text-zinc-700">Tanggal</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" value={sessionDate} onChange={(event) => setSessionDate(event.target.value)} /></label>
        </section>

        <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Tanggal/Jam</th><th className="px-4 py-2">Lokasi</th><th className="px-4 py-2">Coach Assigned</th><th className="px-4 py-2">Status</th><th className="px-4 py-2">Aksi</th></tr></thead>
            <tbody>{(sessionsQuery.data ?? []).map((session) => <tr key={session.id} className="border-t border-zinc-200"><td className="px-4 py-2">{session.sessionDate} {session.startTime} - {session.endTime}</td><td className="px-4 py-2">{session.locationId ? locationMap.get(session.locationId) ?? session.locationId : "-"}</td><td className="px-4 py-2">{session.coaches?.length ? session.coaches.slice(0, 2).map((coach) => coach.coachId).join(", ") : session.coachIds.join(", ") || "-"}</td><td className="px-4 py-2">{session.status}</td><td className="px-4 py-2"><RequirePermission permissions="SCHEDULE_WRITE"><div className="flex gap-2"><button className="rounded border border-zinc-300 px-2 py-1" onClick={() => setEditingSession(session)}>Edit</button><button className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={() => deleteMutation.mutate(session.id)}>Cancel/Delete</button></div></RequirePermission></td></tr>)}</tbody>
          </table>
        </div>

        <RequirePermission permissions="SCHEDULE_WRITE">
          <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">{editingSession ? "Edit Session" : "Tambah Session"}</h2>
            <label className="mt-3 block text-sm"><span className="mb-1 block font-medium text-zinc-700">Class Group ID</span><input className="w-full rounded-md border border-zinc-300 px-3 py-2" value={classGroupId} onChange={(event) => setClassGroupId(event.target.value)} disabled={Boolean(editingSession)} /></label>
            <div className="mt-3"><ClassSessionForm classGroupId={editingSession?.classGroupId ?? classGroupId} locations={locationsQuery.data ?? []} canWrite initialData={editingSession} isSubmitting={createMutation.isPending || updateMutation.isPending} onConflictCheck={(values) => conflictMutation.mutate({ ...values, excludeSessionId: editingSession?.id })} onSubmit={(values) => { setSubmitError(null); const payload = { classGroupId: values.classGroupId, sessionDate: values.sessionDate, startTime: values.startTime, endTime: values.endTime, locationId: values.locationId, status: values.status }; if (editingSession) { updateMutation.mutate({ id: editingSession.id, payload }); return; } createMutation.mutate(payload); }} /></div>
            <div className="mt-3"><SessionConflictAlert conflicts={conflicts} isLoading={conflictMutation.isPending} /></div>
            {submitError ? <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{submitError}</p> : null}
          </section>
        </RequirePermission>
      </AppShell>
    </RequirePermission>
  );
}
