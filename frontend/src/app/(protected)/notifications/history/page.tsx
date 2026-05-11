"use client";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { TailAdminCard } from "@/app/components/tailadmin";

export default function NotificationHistoryPage() {
  return (
    <RequirePermission permissions={["NOTIFICATION_READ", "NOTIFICATION_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Riwayat Notifikasi" description="Pantau notifikasi yang pernah dikirim ke orang tua, siswa, atau coach." />
        <TailAdminCard>
          <EmptyState title="Riwayat notifikasi belum tersedia" description="Halaman ini disiapkan untuk phase notifikasi berikutnya." />
        </TailAdminCard>
      </AppShell>
    </RequirePermission>
  );
}
