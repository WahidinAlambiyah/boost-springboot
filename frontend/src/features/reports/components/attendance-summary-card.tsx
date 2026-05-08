import { SectionCard } from "@/app/components/section-card";

import { StudentProgressAttendanceSummary } from "../student-progress-report.types";
import { getAttendanceRate, getTotalSessions, toNumber } from "./student-progress-formatters";

interface AttendanceSummaryCardProps {
  attendanceSummary: StudentProgressAttendanceSummary;
}

export function AttendanceSummaryCard({ attendanceSummary }: AttendanceSummaryCardProps) {
  const totalSessions = getTotalSessions(attendanceSummary);
  const attendanceRate = getAttendanceRate(attendanceSummary);
  const progressWidth = `${Math.min(100, Math.max(0, attendanceRate))}%`;

  const stats = [
    { label: "Total Sesi", value: totalSessions },
    { label: "Hadir", value: toNumber(attendanceSummary.present), tone: "text-emerald-700" },
    { label: "Izin", value: toNumber(attendanceSummary.permit), tone: "text-sky-700" },
    { label: "Sakit", value: toNumber(attendanceSummary.sick), tone: "text-amber-700" },
    { label: "Absen", value: toNumber(attendanceSummary.absent), tone: "text-rose-700" },
    { label: "Terlambat", value: toNumber(attendanceSummary.late), tone: "text-orange-700" },
  ];

  return (
    <SectionCard title="Attendance Summary" description="Ringkasan kehadiran untuk periode yang dipilih.">
      <div className="mb-5 rounded-lg bg-emerald-50 p-4">
        <div className="flex items-center justify-between gap-3 text-sm">
          <span className="font-medium text-emerald-900">Attendance Rate</span>
          <span className="text-lg font-bold text-emerald-900">{attendanceRate.toFixed(0)}%</span>
        </div>
        <div className="mt-3 h-3 overflow-hidden rounded-full bg-emerald-100">
          <div className="h-full rounded-full bg-emerald-600 transition-all" style={{ width: progressWidth }} />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        {stats.map((stat) => (
          <div key={stat.label} className="rounded-lg border border-zinc-200 p-3 text-center">
            <p className="text-xs font-medium uppercase tracking-wide text-zinc-500">{stat.label}</p>
            <p className={["mt-1 text-xl font-bold", stat.tone ?? "text-zinc-900"].join(" ")}>{stat.value}</p>
          </div>
        ))}
      </div>
    </SectionCard>
  );
}
