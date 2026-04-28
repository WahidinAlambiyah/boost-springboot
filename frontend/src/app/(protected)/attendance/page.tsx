"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import RequirePermission from "@/app/components/require-permission";
import AppShell from "@/app/components/app-shell";
import { AttendanceSubmitPayload, AttendanceSummary } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { api } from "@/lib/api";
import {
  parseErrorMessage,
  parseValidationErrors,
  useStandardErrorRedirect,
} from "@/lib/error-handler";

const ATTENDANCE_QUERY_KEY = ["attendance"];

const toIsoDate = (value: string) => new Date(value).toISOString();

export default function AttendancePage() {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();

  const [enrollmentId, setEnrollmentId] = useState("");
  const [sessionDate, setSessionDate] = useState("");
  const [status, setStatus] = useState("PRESENT");
  const [note, setNote] = useState("");
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const attendanceQuery = useQuery({
    queryKey: ATTENDANCE_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get<ApiResponse<AttendanceSummary[]>>("/api/attendance");
      return response.data.data;
    },
  });

  const submitMutation = useMutation({
    mutationFn: async (payload: AttendanceSubmitPayload) => {
      const idempotencyKey = crypto.randomUUID();
      const response = await api.post<ApiResponse<string>>(
        "/api/attendance/submit",
        payload,
        {
          headers: {
            "Idempotency-Key": idempotencyKey,
          },
        },
      );
      return response.data;
    },
    onSuccess: async (data) => {
      setSubmitMessage(data.message);
      setValidationErrors({});
      setEnrollmentId("");
      setSessionDate("");
      setStatus("PRESENT");
      setNote("");
      await queryClient.invalidateQueries({ queryKey: ATTENDANCE_QUERY_KEY });
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
      setValidationErrors(parseValidationErrors(error));
    },
  });

  if (attendanceQuery.error) {
    handleErrorRedirect(attendanceQuery.error);
  }

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitMessage(null);
    setValidationErrors({});

    submitMutation.mutate({
      enrollmentId,
      sessionDate: toIsoDate(sessionDate),
      status,
      note,
    });
  };

  return (
    <RequirePermission permissions={["ATTENDANCE_READ", "ATTENDANCE_MARK"]} mode="any">
      <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Attendance Module</h1>
      <p className="mt-2 text-zinc-600">Data attendance + aksi submit attendance.</p>

      {attendanceQuery.isLoading ? (
        <p className="mt-4 text-sm text-zinc-600">Memuat attendance...</p>
      ) : null}

      {attendanceQuery.isError ? (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Gagal mengambil data attendance.
        </p>
      ) : null}

      {attendanceQuery.data ? (
        <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-100 text-zinc-700">
              <tr>
                <th className="px-4 py-2">ID</th>
                <th className="px-4 py-2">Enrollment ID</th>
                <th className="px-4 py-2">Session Date</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {attendanceQuery.data.map((item) => (
                <tr key={item.id} className="border-t border-zinc-200 text-zinc-800">
                  <td className="px-4 py-2">{item.id}</td>
                  <td className="px-4 py-2">{item.enrollmentId}</td>
                  <td className="px-4 py-2">{item.sessionDate}</td>
                  <td className="px-4 py-2">{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      <section className="mt-8 max-w-xl rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-zinc-900">Submit Attendance</h2>
        <form className="mt-4 space-y-4" onSubmit={onSubmit}>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Enrollment ID</span>
            <input
              value={enrollmentId}
              onChange={(event) => setEnrollmentId(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              placeholder="UUID enrollmentId"
              required
            />
            {validationErrors.enrollmentId ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.enrollmentId}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Session Date</span>
            <input
              type="date"
              value={sessionDate}
              onChange={(event) => setSessionDate(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              required
            />
            {validationErrors.sessionDate ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.sessionDate}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Status</span>
            <select
              value={status}
              onChange={(event) => setStatus(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              required
            >
              <option value="PRESENT">PRESENT</option>
              <option value="ABSENT">ABSENT</option>
              <option value="SICK">SICK</option>
              <option value="EXCUSED">EXCUSED</option>
            </select>
            {validationErrors.status ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.status}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Note (optional)</span>
            <textarea
              value={note}
              onChange={(event) => setNote(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              rows={3}
            />
            {validationErrors.note ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.note}</p>
            ) : null}
          </label>

          <button
            type="submit"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
            disabled={submitMutation.isPending}
          >
            {submitMutation.isPending ? "Memproses..." : "Submit Attendance"}
          </button>
        </form>

        {submitMessage ? (
          <p className="mt-3 rounded-md border border-zinc-200 bg-zinc-50 p-2 text-sm text-zinc-700">
            {submitMessage}
          </p>
        ) : null}
      </section>
      </AppShell>
    </RequirePermission>
  );
}
