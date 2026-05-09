"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";

import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { QUERY_KEYS } from "@/lib/query-keys";

import { studentProgressReportService } from "../student-progress-report.service";
import { AttendanceSummaryCard } from "./attendance-summary-card";
import { AttendanceTimeline } from "./attendance-timeline";
import { CoachNotesList } from "./coach-notes-list";
import { SkillProgressList } from "./skill-progress-list";
import { StudentProgressFilter, StudentProgressFilterValue } from "./student-progress-filter";
import { StudentProfileSummaryCard } from "./student-profile-summary-card";
import { UpcomingSessionsCard } from "./upcoming-sessions-card";
import { WhatsappSummaryCard } from "./whatsapp-summary-card";

const initialFilter: StudentProgressFilterValue = {
  academyId: "",
  studentId: "",
  from: "",
  to: "",
};

export default function StudentProgressReport() {
  const [filter, setFilter] = useState<StudentProgressFilterValue>(initialFilter);
  const [submittedFilter, setSubmittedFilter] = useState<StudentProgressFilterValue | null>(null);

  const reportParams = useMemo(
    () => ({
      from: submittedFilter?.from || undefined,
      to: submittedFilter?.to || undefined,
    }),
    [submittedFilter],
  );

  const progressQuery = useQuery({
    queryKey: QUERY_KEYS.studentProgressReport.byStudent(submittedFilter?.studentId ?? "", reportParams),
    queryFn: () => studentProgressReportService.getStudentProgressReport(submittedFilter?.studentId ?? "", reportParams),
    enabled: Boolean(submittedFilter?.academyId && submittedFilter?.studentId),
    retry: false,
  });

  const handleGenerate = () => {
    if (!filter.academyId || !filter.studentId) return;

    if (submittedFilter && JSON.stringify(submittedFilter) === JSON.stringify(filter)) {
      void progressQuery.refetch();
      return;
    }

    setSubmittedFilter(filter);
  };

  return (
    <div className="space-y-5">
      <StudentProgressFilter
        value={filter}
        onChange={setFilter}
        onGenerate={handleGenerate}
        isGenerating={progressQuery.isFetching}
      />

      {!submittedFilter ? (
        <EmptyState
          title="Pilih academy dan student untuk generate report"
          description="Gunakan filter di atas untuk memilih academy, student, dan periode report, lalu klik Generate."
        />
      ) : null}

      {submittedFilter && progressQuery.isLoading ? <LoadingSkeleton rows={8} /> : null}

      {submittedFilter && progressQuery.isError ? (
        <ErrorMessage
          title="Gagal memuat progress report"
          message="Pastikan academy, student, dan permission REPORT_PROGRESS_READ sudah sesuai lalu coba generate ulang."
        />
      ) : null}

      {progressQuery.data ? (
        <div className="space-y-5">
          <StudentProfileSummaryCard report={progressQuery.data} />
          <AttendanceSummaryCard attendanceSummary={progressQuery.data.attendanceSummary} />
          <AttendanceTimeline items={progressQuery.data.attendanceTimeline} />
          <SkillProgressList items={progressQuery.data.skillProgress ?? []} />
          <CoachNotesList
            notes={progressQuery.data.coachNotes}
            recommendations={progressQuery.data.nextRecommendations}
            legacyRecommendation={progressQuery.data.recommendation}
          />
          <UpcomingSessionsCard items={progressQuery.data.upcomingSessions} />
          <WhatsappSummaryCard report={progressQuery.data} params={reportParams} />
        </div>
      ) : null}
    </div>
  );
}
