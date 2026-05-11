"use client";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminCard } from "@/app/components/tailadmin";

export default function FinancePaymentsPage() {
  return (
    <RequirePermission permissions={["BILLING_READ", "PAYMENT_RECORD"]} mode="any">
      <AppShell>
        <PageHeader title="Pembayaran" description="Kelola pencatatan dan riwayat pembayaran." />
        <TailAdminCard>
          <EmptyState title="Modul pembayaran belum tersedia" description="Halaman ini disiapkan sebagai route baru untuk struktur menu Keuangan phase berikutnya." />
        </TailAdminCard>
      </AppShell>
    </RequirePermission>
  );
}
