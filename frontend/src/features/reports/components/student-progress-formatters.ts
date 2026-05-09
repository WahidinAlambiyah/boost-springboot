import {
  StudentProgressAttendanceSummary,
  StudentProgressReportParams,
  StudentProgressReportResponse,
  StudentProgressSkillProgress,
} from "../student-progress-report.types";

export const trendLabel: Record<string, string> = {
  UP: "Naik",
  DOWN: "Turun",
  FLAT: "Stabil",
  STABLE: "Stabil",
};

export const statusLabel: Record<string, string> = {
  PRESENT: "Hadir",
  ABSENT: "Absen",
  PERMIT: "Izin",
  SICK: "Sakit",
  LATE: "Terlambat",
  ALPHA: "Alpha",
};

export function toNumber(value: number | string | null | undefined, fallback = 0) {
  if (typeof value === "number") return Number.isFinite(value) ? value : fallback;
  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : fallback;
  }
  return fallback;
}

export function formatDate(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("id-ID", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));
}

export function formatTimeRange(start?: string | null, end?: string | null) {
  if (!start && !end) return "-";
  if (!end) return start?.slice(0, 5) ?? "-";
  if (!start) return end.slice(0, 5);
  return `${start.slice(0, 5)} - ${end.slice(0, 5)}`;
}

export function getTotalSessions(summary: StudentProgressAttendanceSummary) {
  return toNumber(summary.totalSessions ?? summary.total);
}

export function getAttendanceRate(summary: StudentProgressAttendanceSummary) {
  const explicitRate = toNumber(summary.attendanceRate, Number.NaN);
  if (Number.isFinite(explicitRate)) return explicitRate;

  const totalSessions = getTotalSessions(summary);
  if (totalSessions <= 0) return 0;
  return (toNumber(summary.present) / totalSessions) * 100;
}

export function getSkillLatestScore(skill: StudentProgressSkillProgress) {
  return toNumber(skill.latestScore ?? skill.latest);
}

export function getSkillAverageScore(skill: StudentProgressSkillProgress) {
  return toNumber(skill.averageScore ?? skill.average);
}

export function getPeriodText(report?: StudentProgressReportResponse | null, params?: StudentProgressReportParams) {
  const from = report?.period?.from ?? params?.from;
  const to = report?.period?.to ?? params?.to;

  if (from && to) return `${formatDate(from)} s/d ${formatDate(to)}`;
  if (from) return `Mulai ${formatDate(from)}`;
  if (to) return `Sampai ${formatDate(to)}`;
  return "Semua data";
}

function getCoachNoteLines(report: StudentProgressReportResponse) {
  if (typeof report.coachNotes === "string") return [report.coachNotes].filter(Boolean);

  const notes = report.coachNotes ?? [];
  return notes
    .map((note) => [note.overallNotes, note.recommendation].filter(Boolean).join(" — "))
    .filter(Boolean)
    .slice(0, 3);
}

export function buildFallbackWhatsappSummary(report: StudentProgressReportResponse, params?: StudentProgressReportParams) {
  const attendance = report.attendanceSummary;
  const totalSessions = getTotalSessions(attendance);
  const attendanceRate = getAttendanceRate(attendance).toFixed(0);
  const topSkills = [...(report.skillProgress ?? [])]
    .sort((a, b) => getSkillLatestScore(b) - getSkillLatestScore(a))
    .slice(0, 3);
  const skillsText = topSkills.length
    ? topSkills
        .map((skill, index) => {
          const score = getSkillLatestScore(skill);
          const maxScore = skill.maxScore ? `/${skill.maxScore}` : "";
          const trend = trendLabel[skill.trend ?? ""] ?? skill.trend ?? "-";
          return `${index + 1}. ${skill.skillName}: ${score}${maxScore} (${trend})`;
        })
        .join("\n")
    : "- Belum ada data skill.";

  const coachNotes = getCoachNoteLines(report);
  const recommendationText =
    report.nextRecommendations?.map((item) => `- ${item.title}${item.description ? `: ${item.description}` : ""}`).join("\n") ||
    report.recommendation ||
    "- Lanjutkan latihan rutin dan hadir sesuai jadwal.";

  return [
    `Halo, berikut ringkasan progress ${report.student.fullName}.`,
    `Periode: ${getPeriodText(report, params)}`,
    "",
    `Kehadiran: ${toNumber(attendance.present)}/${totalSessions} sesi (${attendanceRate}%).`,
    `Izin ${toNumber(attendance.permit)}, Sakit ${toNumber(attendance.sick)}, Absen ${toNumber(attendance.absent)}, Terlambat ${toNumber(attendance.late)}.`,
    "",
    "Progress skill:",
    skillsText,
    "",
    "Catatan coach:",
    coachNotes.length ? coachNotes.map((note) => `- ${note}`).join("\n") : "- Belum ada catatan coach.",
    "",
    "Rekomendasi berikutnya:",
    recommendationText,
  ].join("\n");
}
