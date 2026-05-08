import { SectionCard } from "@/app/components/section-card";

import { StudentProgressReportResponse } from "../student-progress-report.types";
import { formatDate, getPeriodText } from "./student-progress-formatters";

interface StudentProfileSummaryCardProps {
  report: StudentProgressReportResponse;
}

export function StudentProfileSummaryCard({ report }: StudentProfileSummaryCardProps) {
  const guardians = report.student.guardianNames?.filter(Boolean) ?? [];

  const items = [
    { label: "Nama", value: report.student.fullName },
    { label: "Nickname", value: report.student.nickname || "-" },
    { label: "No. Student", value: report.student.studentNo || "-" },
    { label: "Level", value: report.student.currentLevel || "-" },
    { label: "Academy", value: report.student.academyName || "-" },
    { label: "Usia", value: report.student.ageText || "-" },
    { label: "Tanggal Lahir", value: formatDate(report.student.dateOfBirth) },
    { label: "Orang Tua/Wali", value: guardians.length ? guardians.join(", ") : "-" },
  ];

  return (
    <SectionCard title="Student Profile" description={`Periode report: ${getPeriodText(report)}`}>
      <dl className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
        {items.map((item) => (
          <div key={item.label} className="rounded-lg bg-zinc-50 p-3">
            <dt className="text-xs font-medium uppercase tracking-wide text-zinc-500">{item.label}</dt>
            <dd className="mt-1 font-semibold text-zinc-900">{item.value}</dd>
          </div>
        ))}
      </dl>
    </SectionCard>
  );
}
