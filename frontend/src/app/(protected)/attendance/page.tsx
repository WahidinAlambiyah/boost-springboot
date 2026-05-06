"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import AttendanceTable from "@/features/attendance/components/attendance-table";
import { attendanceService, SessionAttendanceStudent } from "@/features/attendance/attendance.service";
import { classSessionService } from "@/features/class-sessions/class-session.service";
import { parseErrorMessage, useStandardErrorRedirect } from "@/lib/error-handler";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";


export default function AttendancePage() {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();
  const authorities = useAuthStore((state) => state.authorities);
  const canMarkAttendance = can(authorities, "ATTENDANCE_MARK");

  const [selectedSessionId, setSelectedSessionId] = useState("");
  const [rowPatches, setRowPatches] = useState<Record<string, Partial<SessionAttendanceStudent>>>({});
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);

  const sessionsQuery = useQuery({
    queryKey: QUERY_KEYS.classSessions.list(),
    queryFn: () => classSessionService.getClassSessions(),
  });

  const attendanceDetailQuery = useQuery({
    queryKey: QUERY_KEYS.attendanceSessions.detail(selectedSessionId),
    queryFn: () => attendanceService.getAttendanceBySession(selectedSessionId),
    enabled: Boolean(selectedSessionId),
  });

  const rows = useMemo(
    () => (attendanceDetailQuery.data ?? []).map((row) => ({ ...row, ...rowPatches[row.studentId] })),
    [attendanceDetailQuery.data, rowPatches],
  );

  if (sessionsQuery.error) {
    handleErrorRedirect(sessionsQuery.error);
  }

  if (attendanceDetailQuery.error) {
    handleErrorRedirect(attendanceDetailQuery.error);
  }

  const updateRow = (studentId: string, patch: Partial<SessionAttendanceStudent>) => {
    setRowPatches((currentPatches) => ({
      ...currentPatches,
      [studentId]: { ...currentPatches[studentId], ...patch },
    }));
  };

  const submitBulkMutation = useMutation({
    mutationFn: () =>
      attendanceService.submitAttendanceBulk(selectedSessionId, {
        records: rows.map((row) => ({
          studentId: row.studentId,
          attendance: {
            attendanceStatus: row.attendanceStatus,
            remarks: row.remarks || undefined,
          },
        })),
      }),
    onSuccess: async (response) => {
      setSubmitMessage(response.message || "Attendance berhasil disimpan.");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: QUERY_KEYS.attendanceSessions.detail(selectedSessionId) }),
        queryClient.invalidateQueries({ queryKey: QUERY_KEYS.attendanceSessions.all }),
      ]);
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
    },
  });

  const selectedSessionLabel = useMemo(() => {
    const session = sessionsQuery.data?.find((item) => item.id === selectedSessionId);
    if (!session) {
      return null;
    }

    return `${session.sessionDate} • ${session.startTime}-${session.endTime} • ${session.classGroupId}`;
  }, [selectedSessionId, sessionsQuery.data]);

  return (
    <RequirePermission permissions="ATTENDANCE_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Attendance per Class Session</h1>
        <p className="mt-2 text-zinc-600">Pilih sesi kelas, ubah status/remarks murid, lalu submit attendance secara bulk.</p>

        <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Class Session</span>
            <select
              value={selectedSessionId}
              onChange={(event) => {
                setSelectedSessionId(event.target.value);
                setSubmitMessage(null);
                setRowPatches({});
              }}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            >
              <option value="">Pilih class session</option>
              {(sessionsQuery.data ?? []).map((session) => (
                <option key={session.id} value={session.id}>
                  {session.sessionDate} • {session.startTime}-{session.endTime} • {session.classGroupId}
                </option>
              ))}
            </select>
          </label>

          {sessionsQuery.isLoading ? <p className="mt-3 text-sm text-zinc-600">Memuat daftar class session...</p> : null}
          {sessionsQuery.isError ? (
            <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">
              {parseErrorMessage(sessionsQuery.error)}
            </p>
          ) : null}
        </section>

        {selectedSessionId ? (
          <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">Daftar Murid & Attendance</h2>
            {selectedSessionLabel ? <p className="mt-1 text-sm text-zinc-600">{selectedSessionLabel}</p> : null}

            {attendanceDetailQuery.isLoading ? <p className="mt-3 text-sm text-zinc-600">Memuat daftar murid...</p> : null}
            {attendanceDetailQuery.isError ? (
              <p className="mt-3 rounded border border-red-200 bg-red-50 p-2 text-sm text-red-700">
                {parseErrorMessage(attendanceDetailQuery.error)}
              </p>
            ) : null}

            {rows.length > 0 ? (
              <AttendanceTable
                rows={rows}
                onUpdateRow={updateRow}
                disabled={!canMarkAttendance || submitBulkMutation.isPending}
              />
            ) : null}

            {!attendanceDetailQuery.isLoading && !attendanceDetailQuery.isError && rows.length === 0 ? (
              <p className="mt-3 text-sm text-zinc-600">Belum ada murid pada sesi ini.</p>
            ) : null}

            <div className="mt-4 flex items-center gap-3">
              <button
                type="button"
                onClick={() => submitBulkMutation.mutate()}
                className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-50"
                disabled={!canMarkAttendance || submitBulkMutation.isPending || rows.length === 0}
              >
                {submitBulkMutation.isPending ? "Menyimpan..." : "Submit Bulk Attendance"}
              </button>
              {!canMarkAttendance ? <p className="text-xs text-amber-700">Anda tidak punya permission ATTENDANCE_MARK.</p> : null}
            </div>

            {submitMessage ? (
              <p className="mt-3 rounded-md border border-zinc-200 bg-zinc-50 p-2 text-sm text-zinc-700">
                Status tersimpan: {submitMessage}
              </p>
            ) : null}
          </section>
        ) : null}
      </AppShell>
    </RequirePermission>
  );
}
