"use client";

import { AttendanceStatus, SessionAttendanceStudent } from "@/features/attendance/attendance.service";

const ATTENDANCE_STATUS_OPTIONS: AttendanceStatus[] = ["PRESENT", "ABSENT", "PERMIT", "SICK", "LATE"];

type AttendanceTableProps = {
  rows: SessionAttendanceStudent[];
  disabled?: boolean;
  onUpdateRow: (studentId: string, patch: Partial<SessionAttendanceStudent>) => void;
  onMarkAllPresent?: () => void;
  showMarkAllPresent?: boolean;
  onSubmitStudent?: (studentId: string) => void;
  pendingStudentId?: string | null;
};

export default function AttendanceTable({
  rows,
  disabled = false,
  onUpdateRow,
  onMarkAllPresent,
  showMarkAllPresent = false,
  onSubmitStudent,
  pendingStudentId = null,
}: AttendanceTableProps) {
  const summary = rows.reduce(
    (acc, row) => {
      acc[row.attendanceStatus] += 1;
      return acc;
    },
    { PRESENT: 0, ABSENT: 0, PERMIT: 0, SICK: 0, LATE: 0 } as Record<AttendanceStatus, number>,
  );

  return (
    <>
      <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-zinc-600">
        <span className="rounded bg-zinc-100 px-2 py-1">Present: {summary.PRESENT}</span>
        <span className="rounded bg-zinc-100 px-2 py-1">Absent: {summary.ABSENT}</span>
        <span className="rounded bg-zinc-100 px-2 py-1">Permit: {summary.PERMIT}</span>
        <span className="rounded bg-zinc-100 px-2 py-1">Sick: {summary.SICK}</span>
        <span className="rounded bg-zinc-100 px-2 py-1">Late: {summary.LATE}</span>
      </div>

      {showMarkAllPresent && onMarkAllPresent ? (
        <div className="mt-3">
          <button
            type="button"
            onClick={onMarkAllPresent}
            disabled={disabled || rows.length === 0}
            className="rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Tandai Hadir Semua
          </button>
        </div>
      ) : null}

      <div className="mt-4 overflow-x-auto">
        <table className="min-w-full border-collapse text-sm">
          <thead>
            <tr className="border-b border-zinc-200 text-left text-zinc-700">
              <th className="px-3 py-2">Murid</th>
              <th className="px-3 py-2">Status</th>
              <th className="px-3 py-2">Remarks</th>
              {onSubmitStudent ? <th className="px-3 py-2">Aksi</th> : null}
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.studentId} className="border-b border-zinc-100 align-top">
                <td className="px-3 py-2">
                  <p className="font-medium text-zinc-900">{row.studentName || row.studentId}</p>
                  <p className="text-xs text-zinc-500">{row.studentId}</p>
                </td>
                <td className="px-3 py-2">
                  <select
                    value={row.attendanceStatus}
                    onChange={(event) =>
                      onUpdateRow(row.studentId, {
                        attendanceStatus: event.target.value as AttendanceStatus,
                      })
                    }
                    className="w-full rounded-md border border-zinc-300 px-2 py-1"
                    disabled={disabled}
                  >
                    {ATTENDANCE_STATUS_OPTIONS.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-3 py-2">
                  <textarea
                    value={row.remarks || ""}
                    onChange={(event) => onUpdateRow(row.studentId, { remarks: event.target.value })}
                    className="min-h-20 w-full rounded-md border border-zinc-300 px-2 py-1"
                    placeholder="Tambahkan catatan jika perlu"
                    disabled={disabled}
                  />
                </td>
                {onSubmitStudent ? (
                  <td className="px-3 py-2">
                    <button
                      type="button"
                      onClick={() => onSubmitStudent(row.studentId)}
                      className="rounded-md border border-zinc-300 px-2 py-1 text-xs text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={disabled || pendingStudentId === row.studentId}
                    >
                      {pendingStudentId === row.studentId ? "Menyimpan..." : "Update Murid"}
                    </button>
                  </td>
                ) : null}

              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
