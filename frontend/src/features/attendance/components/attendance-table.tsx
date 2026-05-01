"use client";

import { AttendanceStatus } from "@/features/attendance/attendance.service";

const statusOptions: AttendanceStatus[] = ["PRESENT", "ABSENT", "PERMIT", "SICK", "LATE"];

interface AttendanceTableRow {
  studentId: string;
  studentName: string;
  attendanceStatus?: AttendanceStatus;
  remarks?: string;
}

interface AttendanceTableProps {
  rows: AttendanceTableRow[];
  isSubmitting?: boolean;
  submitMessage?: string | null;
  onStatusChange: (studentId: string, status?: AttendanceStatus) => void;
  onRemarksChange: (studentId: string, remarks: string) => void;
  onMarkAllPresent: () => void;
  onSubmitBulk: () => void;
}

export default function AttendanceTable({
  rows,
  isSubmitting = false,
  submitMessage,
  onStatusChange,
  onRemarksChange,
  onMarkAllPresent,
  onSubmitBulk,
}: AttendanceTableProps) {
  return (
    <section className="rounded-lg border border-zinc-200 bg-white p-3 sm:p-4">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-base font-semibold text-zinc-900 sm:text-lg">Absensi murid per sesi</h2>
        <button
          type="button"
          className="min-h-11 rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50"
          onClick={onMarkAllPresent}
        >
          Hadir semua (yang belum diisi)
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full min-w-[680px] text-left text-sm">
          <thead className="bg-zinc-100 text-zinc-700">
            <tr>
              <th className="px-2 py-2 sm:px-3">Murid</th>
              <th className="px-2 py-2 sm:px-3">Status</th>
              <th className="px-2 py-2 sm:px-3">Remarks</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.studentId} className="border-t border-zinc-200 align-top">
                <td className="px-2 py-2 text-zinc-800 sm:px-3">{row.studentName}</td>
                <td className="px-2 py-2 sm:px-3">
                  <select
                    value={row.attendanceStatus ?? ""}
                    onChange={(event) =>
                      onStatusChange(row.studentId, (event.target.value || undefined) as AttendanceStatus | undefined)
                    }
                    className="min-h-12 w-full rounded-md border border-zinc-300 px-3 py-2 text-base"
                  >
                    <option value="">Pilih status</option>
                    {statusOptions.map((statusOption) => (
                      <option key={statusOption} value={statusOption}>
                        {statusOption}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-2 py-2 sm:px-3">
                  <input
                    value={row.remarks ?? ""}
                    onChange={(event) => onRemarksChange(row.studentId, event.target.value)}
                    placeholder="Catatan"
                    className="min-h-12 w-full rounded-md border border-zinc-300 px-3 py-2 text-base"
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center">
        <button
          type="button"
          className="min-h-12 rounded-md bg-zinc-900 px-5 py-2 text-base font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
          onClick={onSubmitBulk}
          disabled={isSubmitting || rows.length === 0}
        >
          {isSubmitting ? "Mengirim..." : "Submit bulk attendance"}
        </button>
        {submitMessage ? (
          <p className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {submitMessage}
          </p>
        ) : null}
      </div>
    </section>
  );
}

export type { AttendanceTableProps, AttendanceTableRow };
