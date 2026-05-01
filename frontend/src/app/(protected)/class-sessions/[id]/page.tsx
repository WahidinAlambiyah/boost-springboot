"use client";

import { useMemo } from "react";
import { useParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import { academyLocationService } from "@/features/academy-locations/academy-location.service";
import SessionCoachManager from "@/features/class-sessions/components/session-coach-manager";
import { classSessionService } from "@/features/class-sessions/class-session.service";
import { parseErrorMessage } from "@/lib/error-handler";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function ClassSessionDetailPage() {
  const params = useParams<{ id: string }>();
  const sessionId = params.id;
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = can(authorities, "SCHEDULE_WRITE");

  const sessionQuery = useQuery({
    queryKey: QUERY_KEYS.classSessions.detail(sessionId),
    queryFn: () => classSessionService.getClassSessionById(sessionId),
    enabled: Boolean(sessionId),
  });

  const coachesQuery = useQuery({
    queryKey: QUERY_KEYS.classSessionCoaches.filter({ sessionId }),
    queryFn: () => classSessionService.getSessionCoaches(sessionId),
    enabled: Boolean(sessionId),
  });

  const locationsQuery = useQuery({
    queryKey: QUERY_KEYS.academyLocations.byAcademy(sessionQuery.data?.academyId),
    queryFn: () => academyLocationService.list({ academyId: sessionQuery.data?.academyId }),
    enabled: Boolean(sessionQuery.data?.academyId),
  });

  const locationName = useMemo(() => {
    const location = (locationsQuery.data ?? []).find((item) => item.id === sessionQuery.data?.locationId);
    return location?.name ?? sessionQuery.data?.locationId ?? "-";
  }, [locationsQuery.data, sessionQuery.data?.locationId]);

  const invalidate = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.classSessions.detail(sessionId) }),
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.classSessionCoaches.filter({ sessionId }) }),
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.classSessions.all }),
    ]);
  };

  const addCoachMutation = useMutation({
    mutationFn: (payload: { coachId: string; attendanceStatus?: string }) => classSessionService.createSessionCoach(sessionId, payload),
    onSuccess: async () => invalidate(),
  });

  const updateCoachMutation = useMutation({
    mutationFn: ({ sessionCoachId, payload }: { sessionCoachId: string; payload: { attendanceStatus?: string } }) =>
      classSessionService.updateSessionCoach(sessionId, sessionCoachId, payload),
    onSuccess: async () => invalidate(),
  });

  const deleteCoachMutation = useMutation({
    mutationFn: (sessionCoachId: string) => classSessionService.deleteSessionCoach(sessionId, sessionCoachId),
    onSuccess: async () => invalidate(),
  });

  return (
    <RequirePermission permissions="SCHEDULE_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Class Session Detail</h1>

        {sessionQuery.isLoading ? <p className="mt-4 text-sm text-zinc-600">Loading session...</p> : null}
        {sessionQuery.isError ? <p className="mt-4 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{parseErrorMessage(sessionQuery.error)}</p> : null}

        {sessionQuery.data ? (
          <section className="mt-4 space-y-4">
            <div className="grid grid-cols-1 gap-3 rounded-lg border border-zinc-200 bg-white p-4 md:grid-cols-2">
              <DetailItem label="Academy" value={sessionQuery.data.academyId} />
              <DetailItem label="Lokasi" value={locationName} />
              <DetailItem label="Tanggal" value={sessionQuery.data.sessionDate} />
              <DetailItem label="Jam" value={`${sessionQuery.data.startTime} - ${sessionQuery.data.endTime}`} />
              <DetailItem label="Status" value={sessionQuery.data.status} />
              <DetailItem label="Notes" value={(sessionQuery.data as { notes?: string }).notes ?? "-"} />
            </div>

            <div className="rounded-lg border border-zinc-200 bg-white p-4">
              <SessionCoachManager
                coaches={coachesQuery.data ?? []}
                canWrite={canWrite}
                onAdd={(payload) => addCoachMutation.mutate(payload)}
                onUpdate={(sessionCoachId, payload) => updateCoachMutation.mutate({ sessionCoachId, payload })}
                onRemove={(sessionCoachId) => deleteCoachMutation.mutate(sessionCoachId)}
              />

              {coachesQuery.isError ? <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{parseErrorMessage(coachesQuery.error)}</p> : null}
              {addCoachMutation.isError ? <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{parseErrorMessage(addCoachMutation.error)}</p> : null}
              {updateCoachMutation.isError ? <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{parseErrorMessage(updateCoachMutation.error)}</p> : null}
              {deleteCoachMutation.isError ? <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">{parseErrorMessage(deleteCoachMutation.error)}</p> : null}
            </div>
          </section>
        ) : null}
      </AppShell>
    </RequirePermission>
  );
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-zinc-500">{label}</p>
      <p className="mt-1 text-sm font-medium text-zinc-900">{value || "-"}</p>
    </div>
  );
}
