"use client";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import StudentProgressReport from "@/features/reports/components/student-progress-report";

export default function StudentProgressReportPage() {
  return (
    <RequirePermission permissions="REPORT_PROGRESS_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Student Progress Report</h1>
        <p className="mt-2 text-zinc-600">Filter student dan periode untuk melihat attendance, skill progress, dan catatan coach.</p>

        <div className="mt-6">
          <StudentProgressReport />
        </div>
      </AppShell>
    </RequirePermission>
  );
}
