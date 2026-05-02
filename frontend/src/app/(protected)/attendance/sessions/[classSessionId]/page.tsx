"use client";

import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import AttendanceTable from "@/features/attendance/components/attendance-table";
import { attendanceService, SessionAttendanceStudent } from "@/features/attendance/attendance.service";
import { parseErrorMessage, useStandardErrorRedirect } from "@/lib/error-handler";
import { AttendanceRecordRequest } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function AttendanceSessionDetailPage({ params }: { params: { classSessionId: string } }) {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();
  const authorities = useAuthStore((state) => state.authorities);
  const canMarkAttendance = can(authorities, "ATTENDANCE_MARK");

  const classSessionId = params.classSessionId;
  const [rows, setRows] = useState<SessionAttendanceStudent[]>([]);
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const [pendingStudentId, setPendingStudentId] = useState<string | null>(null);

  const attendanceDetailQuery = useQuery({
    queryKey: QUERY_KEYS.attendanceSessions.byClassSession(classSessionId),
    queryFn: () => attendanceService.getAttendanceBySession(classSessionId),
    enabled: Boolean(classSessionId),
  });

  useEffect(() => {
    if (attendanceDetailQuery.data) {
      setRows(attendanceDetailQuery.data);
    }
  }, [attendanceDetailQuery.data]);

  if (attendanceDetailQuery.error) {
    handleErrorRedirect(attendanceDetailQuery.error);
  }

  const updateRow = (studentId: string, patch: Partial<SessionAttendanceStudent>) => {
    setRows((currentRows) => currentRows.map((row) => (row.studentId === studentId ? { ...row, ...patch } : row)));
  };

  const hasAnyAttendanceRecord = useMemo(
    () => rows.some((row) => Boolean(row.checkInAt) || Boolean(row.remarks) || row.attendanceStatus !== "PRESENT"),
    [rows],
  );

  const submitBulkMutation = useMutation({
    mutationFn: () =>
      attendanceService.submitAttendanceBulk(classSessionId, {
        records: rows.map((row) => ({
          studentId: row.studentId,
          attendance: {
            attendanceStatus: row.attendanceStatus,
            remarks: row.remarks || undefined,
          },
        })),
      } satisfies AttendanceRecordRequest),
    onSuccess: async (response) => {
      setSubmitMessage(response.message || "Attendance berhasil disimpan.");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: QUERY_KEYS.attendanceSessions.detail(classSessionId) }),
        queryClient.invalidateQueries({ queryKey: QUERY_KEYS.attendanceSessions.all }),
      ]);
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
    },
  });

  const updatePerStudentMutation = useMutation({
    mutationFn: async (studentId: string) => {
      const row = rows.find((item) => item.studentId === studentId);
      if (!row) return;
      setPendingStudentId(studentId);
      return attendanceService.updateAttendanceByStudent(classSessionId, studentId, {
        attendanceStatus: row.attendanceStatus,
        remarks: row.remarks || undefined,
      });
    },
    onSuccess: (response) => {
      if (response) setSubmitMessage(response.message || "Attendance murid berhasil diperbarui.");
    },
    onSettled: () => setPendingStudentId(null),
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
    },
  });

  return (
    <RequirePermission permissions="ATTENDANCE_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Attendance Session Detail</h1>
        <p className="mt-2 text-zinc-600">Session ID: {classSessionId}</p>

        <section className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
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
              onMarkAllPresent={() => {
                setRows((currentRows) => currentRows.map((row) => ({ ...row, attendanceStatus: "PRESENT", remarks: "" })));
                setSubmitMessage("Semua murid ditandai hadir. Silakan submit untuk menyimpan.");
              }}
              showMarkAllPresent={!hasAnyAttendanceRecord}
              onSubmitStudent={(studentId) => updatePerStudentMutation.mutate(studentId)}
              pendingStudentId={pendingStudentId}
              disabled={!canMarkAttendance || submitBulkMutation.isPending || updatePerStudentMutation.isPending}
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
            <p className="mt-3 rounded-md border border-zinc-200 bg-zinc-50 p-2 text-sm text-zinc-700">{submitMessage}</p>
          ) : null}
        </section>
      </AppShell>
    </RequirePermission>
  );
}
