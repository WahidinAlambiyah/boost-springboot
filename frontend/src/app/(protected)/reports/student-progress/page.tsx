"use client";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import StudentProgressReport from "@/features/reports/components/student-progress-report";

export default function StudentProgressReportPage() {
  return (
    <RequirePermission permissions="REPORT_PROGRESS_READ">
      <AppShell>
        <PageHeader
          title="Student Progress Report"
          description="Generate report attendance, skill progress, coach notes, upcoming sessions, dan ringkasan WhatsApp untuk admin, coach, atau orang tua."
        />

        <StudentProgressReport />
      </AppShell>
    </RequirePermission>
  );
}
