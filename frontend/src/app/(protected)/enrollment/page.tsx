"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import RequirePermission from "@/app/components/require-permission";
import AppShell from "@/app/components/app-shell";
import { EnrollmentRegisterPayload } from "@/lib/api-types";
import {
  parseErrorMessage,
  parseValidationErrors,
  useStandardErrorRedirect,
} from "@/lib/error-handler";
import { QUERY_KEYS } from "@/lib/query-keys";
import { enrollmentService } from "@/features/enrollment/enrollment.service";

export default function EnrollmentPage() {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();

  const [studentId, setStudentId] = useState("");
  const [classCode, setClassCode] = useState("");
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const enrollmentQuery = useQuery({
    queryKey: QUERY_KEYS.enrollment,
    queryFn: enrollmentService.getSummaries,
  });

  const registerMutation = useMutation({
    mutationFn: (payload: EnrollmentRegisterPayload) => enrollmentService.register(payload),
    onSuccess: async (data) => {
      setSubmitMessage(data.message);
      setValidationErrors({});
      setStudentId("");
      setClassCode("");
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.enrollment });
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
      setValidationErrors(parseValidationErrors(error));
    },
  });

  const cancelMutation = useMutation({
    mutationFn: (enrollmentId: string) => enrollmentService.cancel(enrollmentId),
    onSuccess: async (data) => {
      setSubmitMessage(data.message);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.enrollment });
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
    },
  });

  if (enrollmentQuery.error) {
    handleErrorRedirect(enrollmentQuery.error);
  }

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitMessage(null);
    setValidationErrors({});

    registerMutation.mutate({ studentId, classCode });
  };

  return (
    <RequirePermission permissions={["ENROLLMENT_READ", "ENROLLMENT_WRITE"]} mode="any">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Enrollment Module</h1>
        <p className="mt-2 text-zinc-600">Data enrollment + aksi register/cancel enrollment.</p>

        {enrollmentQuery.isLoading ? (
          <p className="mt-4 text-sm text-zinc-600">Memuat enrollment...</p>
        ) : null}

        {enrollmentQuery.isError ? (
          <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            Gagal mengambil data enrollment.
          </p>
        ) : null}

        {enrollmentQuery.data ? (
          <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="bg-zinc-100 text-zinc-700">
                <tr>
                  <th className="px-4 py-2">ID</th>
                  <th className="px-4 py-2">Student</th>
                  <th className="px-4 py-2">Class</th>
                  <th className="px-4 py-2">Status</th>
                  <th className="px-4 py-2">Action</th>
                </tr>
              </thead>
              <tbody>
                {enrollmentQuery.data.map((item) => (
                  <tr key={item.id} className="border-t border-zinc-200 text-zinc-800">
                    <td className="px-4 py-2">{item.id}</td>
                    <td className="px-4 py-2">{item.studentName}</td>
                    <td className="px-4 py-2">{item.classCode}</td>
                    <td className="px-4 py-2">{item.status}</td>
                    <td className="px-4 py-2">
                      <button
                        type="button"
                        className="rounded-md border border-zinc-300 px-3 py-1 text-xs text-zinc-700 disabled:opacity-50"
                        onClick={() => cancelMutation.mutate(item.id)}
                        disabled={cancelMutation.isPending}
                      >
                        {cancelMutation.isPending ? "Memproses..." : "Cancel"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}

        <section className="mt-8 max-w-xl rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="text-lg font-semibold text-zinc-900">Register Enrollment</h2>
          <form className="mt-4 space-y-4" onSubmit={onSubmit}>
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">Student ID</span>
              <input
                value={studentId}
                onChange={(event) => setStudentId(event.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                placeholder="UUID studentId"
                required
              />
              {validationErrors.studentId ? (
                <p className="mt-1 text-xs text-red-600">{validationErrors.studentId}</p>
              ) : null}
            </label>

            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">Class Code</span>
              <input
                value={classCode}
                onChange={(event) => setClassCode(event.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                placeholder="Misal: CLASS-001"
                required
              />
              {validationErrors.classCode ? (
                <p className="mt-1 text-xs text-red-600">{validationErrors.classCode}</p>
              ) : null}
            </label>

            <button
              type="submit"
              className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
              disabled={registerMutation.isPending}
            >
              {registerMutation.isPending ? "Memproses..." : "Register"}
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
