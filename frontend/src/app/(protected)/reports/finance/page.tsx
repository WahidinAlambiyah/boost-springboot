"use client";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminCard } from "@/app/components/tailadmin";

export default function FinanceReportPage() {
  return (
    <RequirePermission permissions={["BILLING_READ", "REPORT_EXPORT"]} mode="any">
      <AppShell>
        <PageHeader title="Laporan Keuangan" description="Rekap tagihan, pembayaran, dan transaksi academy." />
        <TailAdminCard>
          <EmptyState title="Laporan keuangan belum tersedia" description="Halaman ini disiapkan untuk phase laporan berikutnya." />
        </TailAdminCard>
      </AppShell>
    </RequirePermission>
  );
}
