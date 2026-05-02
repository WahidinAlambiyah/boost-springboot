"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";

import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { progressReportService } from "@/features/reports/progress-report.service";
import { studentService } from "@/features/students/student.service";
import { QUERY_KEYS } from "@/lib/query-keys";

const trendLabel: Record<string, string> = {
  UP: "Naik",
  DOWN: "Turun",
  STABLE: "Stabil",
};

export default function StudentProgressReport() {
  const [studentId, setStudentId] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  const filters = useMemo(
    () => ({
      studentId,
      from: from || undefined,
      to: to || undefined,
    }),
    [from, studentId, to],
  );

  const studentsQuery = useQuery({
    queryKey: QUERY_KEYS.students.list(),
    queryFn: () => studentService.list(),
    retry: false,
  });

  const progressQuery = useQuery({
    queryKey: QUERY_KEYS.studentProgressReport.filter(filters),
    queryFn: () => progressReportService.getStudentProgressReport(filters),
    enabled: Boolean(studentId),
  });

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-3 rounded-lg border border-zinc-200 bg-white p-4 md:grid-cols-3">
        <label className="text-sm">
          <span className="mb-1 block">Student</span>
          <select
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            value={studentId}
            onChange={(event) => setStudentId(event.target.value)}
          >
            <option value="">Pilih student</option>
            {studentsQuery.data?.map((student) => (
              <option key={student.id} value={student.id}>
                {student.fullName}
              </option>
            ))}
          </select>
        </label>

        <label className="text-sm">
          <span className="mb-1 block">Dari Tanggal</span>
          <input
            type="date"
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
          />
        </label>

        <label className="text-sm">
          <span className="mb-1 block">Sampai Tanggal</span>
          <input
            type="date"
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            value={to}
            onChange={(event) => setTo(event.target.value)}
          />
        </label>
      </div>

      {studentsQuery.isLoading ? <LoadingSkeleton rows={3} /> : null}
      {studentsQuery.isError ? <ErrorMessage message="Gagal memuat daftar student." /> : null}

      {!studentId ? (
        <EmptyState title="Pilih student" description="Pilih student dan periode untuk melihat progress report." />
      ) : null}

      {studentId && progressQuery.isLoading ? <LoadingSkeleton rows={8} /> : null}
      {studentId && progressQuery.isError ? <ErrorMessage message="Gagal memuat progress report." /> : null}

      {studentId && progressQuery.data ? (
        <div className="space-y-4">
          <section className="rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">Data Anak</h2>
            <div className="mt-3 grid grid-cols-1 gap-2 text-sm text-zinc-700 md:grid-cols-2">
              <p><span className="font-medium">Nama:</span> {progressQuery.data.student.fullName}</p>
              <p><span className="font-medium">Nickname:</span> {progressQuery.data.student.nickname || "-"}</p>
              <p><span className="font-medium">Student No:</span> {progressQuery.data.student.studentNo || "-"}</p>
              <p><span className="font-medium">Status:</span> {progressQuery.data.student.status || "-"}</p>
            </div>
          </section>

          <section className="rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">Attendance Summary</h2>
            <div className="mt-3 grid grid-cols-2 gap-3 text-sm md:grid-cols-3">
              <div className="rounded border border-zinc-200 p-3">Total: {progressQuery.data.attendanceSummary.total}</div>
              <div className="rounded border border-zinc-200 p-3">Present: {progressQuery.data.attendanceSummary.present}</div>
              <div className="rounded border border-zinc-200 p-3">Permit: {progressQuery.data.attendanceSummary.permit}</div>
              <div className="rounded border border-zinc-200 p-3">Sick: {progressQuery.data.attendanceSummary.sick}</div>
              <div className="rounded border border-zinc-200 p-3">Absent: {progressQuery.data.attendanceSummary.absent}</div>
              <div className="rounded border border-zinc-200 p-3">Alpha: {progressQuery.data.attendanceSummary.alpha}</div>
            </div>
          </section>

          <section className="rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">Skill Progress</h2>
            {progressQuery.data.skillProgress.length === 0 ? (
              <p className="mt-3 text-sm text-zinc-600">Belum ada data skill progress.</p>
            ) : (
              <div className="mt-3 overflow-x-auto">
                <table className="min-w-full border-collapse text-sm">
                  <thead>
                    <tr className="border-b border-zinc-200 text-left text-zinc-600">
                      <th className="px-3 py-2">Skill</th>
                      <th className="px-3 py-2">Latest</th>
                      <th className="px-3 py-2">Average</th>
                      <th className="px-3 py-2">Trend</th>
                    </tr>
                  </thead>
                  <tbody>
                    {progressQuery.data.skillProgress.map((item, index) => (
                      <tr key={`${item.skillCode || item.skillName}-${index}`} className="border-b border-zinc-100">
                        <td className="px-3 py-2">{item.skillName}</td>
                        <td className="px-3 py-2">{item.latest}</td>
                        <td className="px-3 py-2">{item.average.toFixed(2)}</td>
                        <td className="px-3 py-2">{trendLabel[item.trend] || item.trend}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="rounded-lg border border-zinc-200 bg-white p-4">
            <h2 className="text-lg font-semibold text-zinc-900">Coach Notes & Recommendation</h2>
            <div className="mt-3 space-y-3 text-sm text-zinc-700">
              <p><span className="font-medium">Coach Notes:</span> {progressQuery.data.coachNotes || "-"}</p>
              <p><span className="font-medium">Recommendation:</span> {progressQuery.data.recommendation || "-"}</p>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}
