import { SectionCard } from "@/app/components/section-card";

import { StudentProgressAttendanceTimelineItem } from "../student-progress-report.types";
import { formatDate, formatTimeRange, statusLabel } from "./student-progress-formatters";

interface AttendanceTimelineProps {
  items?: StudentProgressAttendanceTimelineItem[] | null;
}

export function AttendanceTimeline({ items }: AttendanceTimelineProps) {
  const rows = items ?? [];

  return (
    <SectionCard title="Attendance Timeline" description="Riwayat sesi dalam periode report.">
      {!rows.length ? (
        <p className="text-sm text-zinc-600">Belum ada riwayat attendance pada periode ini.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead>
              <tr className="border-b border-zinc-200 text-xs uppercase tracking-wide text-zinc-500">
                <th className="px-3 py-2">Tanggal</th>
                <th className="px-3 py-2">Jam</th>
                <th className="px-3 py-2">Kelas</th>
                <th className="px-3 py-2">Lokasi</th>
                <th className="px-3 py-2">Status</th>
                <th className="px-3 py-2">Remarks</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {rows.map((item) => (
                <tr key={item.sessionId} className="align-top">
                  <td className="px-3 py-3 font-medium text-zinc-900">{formatDate(item.sessionDate)}</td>
                  <td className="px-3 py-3 text-zinc-700">{formatTimeRange(item.startTime, item.endTime)}</td>
                  <td className="px-3 py-3 text-zinc-700">{item.classGroupName || "-"}</td>
                  <td className="px-3 py-3 text-zinc-700">{item.locationName || "-"}</td>
                  <td className="px-3 py-3">
                    <span className="rounded-full bg-zinc-100 px-2 py-1 text-xs font-medium text-zinc-700">
                      {statusLabel[item.status ?? ""] ?? item.status ?? "-"}
                    </span>
                  </td>
                  <td className="px-3 py-3 text-zinc-600">{item.remarks || "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </SectionCard>
  );
}
