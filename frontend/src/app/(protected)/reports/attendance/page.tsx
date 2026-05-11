"use client";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminCard } from "@/app/components/tailadmin";

export default function AttendanceReportPage() {
  return (
    <RequirePermission permissions={["ATTENDANCE_READ", "REPORT_PROGRESS_READ"]} mode="any">
      <AppShell>
        <PageHeader title="Laporan Absensi" description="Rekap kehadiran siswa dan sesi latihan." />
        <TailAdminCard>
          <EmptyState title="Laporan absensi belum tersedia" description="Halaman ini disiapkan untuk phase laporan berikutnya." />
        </TailAdminCard>
      </AppShell>
    </RequirePermission>
  );
}
