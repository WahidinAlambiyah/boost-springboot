"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import RequirePermission from "@/app/components/require-permission";
import AppShell from "@/app/components/app-shell";
import { ReschedulePayload, SchedulingSummary } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import {
  parseErrorMessage,
  parseValidationErrors,
  useStandardErrorRedirect,
} from "@/lib/error-handler";
import { api } from "@/lib/api";

const SCHEDULING_QUERY_KEY = ["scheduling"];

const toIsoDateTime = (value: string) => new Date(value).toISOString();

export default function SchedulingPage() {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();

  const [classGroupId, setClassGroupId] = useState("");
  const [previousStartAt, setPreviousStartAt] = useState("");
  const [newStartAt, setNewStartAt] = useState("");
  const [reason, setReason] = useState("");
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const schedulingQuery = useQuery({
    queryKey: SCHEDULING_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get<ApiResponse<SchedulingSummary[]>>("/api/scheduling");
      return response.data.data;
    },
  });

  const rescheduleMutation = useMutation({
    mutationFn: async (payload: ReschedulePayload) => {
      const response = await api.post<ApiResponse<string>>("/api/scheduling/reschedule", payload);
      return response.data;
    },
    onSuccess: async (data) => {
      setSubmitMessage(data.message);
      setValidationErrors({});
      await queryClient.invalidateQueries({ queryKey: SCHEDULING_QUERY_KEY });
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));

      const errors = parseValidationErrors(error);
      setValidationErrors(errors);
    },
  });

  if (schedulingQuery.error) {
    handleErrorRedirect(schedulingQuery.error);
  }

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitMessage(null);
    setValidationErrors({});

    rescheduleMutation.mutate({
      classGroupId,
      previousStartAt: toIsoDateTime(previousStartAt),
      newStartAt: toIsoDateTime(newStartAt),
      reason,
    });
  };

  return (
    <RequirePermission permissions={["SCHEDULING_READ", "SCHEDULING_RESCHEDULE"]} mode="any">
      <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Scheduling Module</h1>
      <p className="mt-2 text-zinc-600">Data scheduling + aksi reschedule.</p>

      {schedulingQuery.isLoading ? <p className="mt-4 text-sm text-zinc-600">Memuat jadwal...</p> : null}
      {schedulingQuery.isError ? (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Gagal mengambil data scheduling.
        </p>
      ) : null}

      {schedulingQuery.data ? (
        <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-100 text-zinc-700">
              <tr>
                <th className="px-4 py-2">Code</th>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {schedulingQuery.data.map((item) => (
                <tr key={item.code} className="border-t border-zinc-200 text-zinc-800">
                  <td className="px-4 py-2">{item.code}</td>
                  <td className="px-4 py-2">{item.name}</td>
                  <td className="px-4 py-2">{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      <section className="mt-8 max-w-xl rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-zinc-900">Reschedule Kelas</h2>
        <form className="mt-4 space-y-4" onSubmit={onSubmit}>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Class Group ID</span>
            <input
              value={classGroupId}
              onChange={(event) => setClassGroupId(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              placeholder="UUID classGroupId"
              required
            />
            {validationErrors.classGroupId ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.classGroupId}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Previous Start At</span>
            <input
              type="datetime-local"
              value={previousStartAt}
              onChange={(event) => setPreviousStartAt(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              required
            />
            {validationErrors.previousStartAt ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.previousStartAt}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">New Start At</span>
            <input
              type="datetime-local"
              value={newStartAt}
              onChange={(event) => setNewStartAt(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              required
            />
            {validationErrors.newStartAt ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.newStartAt}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Reason (optional)</span>
            <textarea
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              rows={3}
            />
            {validationErrors.reason ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.reason}</p>
            ) : null}
          </label>

          <button
            type="submit"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
            disabled={rescheduleMutation.isPending}
          >
            {rescheduleMutation.isPending ? "Memproses..." : "Reschedule"}
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
