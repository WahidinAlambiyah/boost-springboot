"use client";

import { useMemo, useState } from "react";

import AppShell from "@/app/components/app-shell";
import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import { StatusBadge } from "@/app/components/status-badge";
import { devCrudMockItems } from "@/features/dev-crud/dev-crud.mock";
import type { TrainingCenterDemo, TrainingCenterDemoStatus } from "@/features/dev-crud/dev-crud.types";

const statusTone: Record<TrainingCenterDemoStatus, "default" | "success"> = {
  ACTIVE: "success",
  INACTIVE: "default",
};

const tableErrorMessage = "Gagal memuat data training center demo. Ini adalah pesan error dummy untuk validasi UI.";

export default function DevTableDemoPage() {
  // TODO: replace dummy create/update/delete checks with DEV_TOOLS_WRITE when dev tools write permission is available.
  const [lastAction, setLastAction] = useState("Belum ada action yang dijalankan.");

  const baseColumns = useMemo<DataTableColumn<TrainingCenterDemo>[]>(
    () => [
      {
        key: "code",
        header: "Kode",
        render: (trainingCenter) => <span className="font-medium text-zinc-900">{trainingCenter.code}</span>,
      },
      {
        key: "name",
        header: "Nama",
        render: (trainingCenter) => trainingCenter.name,
      },
      {
        key: "location",
        header: "Lokasi",
        render: (trainingCenter) => trainingCenter.location,
      },
      {
        key: "activeStudents",
        header: "Murid Aktif",
        render: (trainingCenter) => trainingCenter.activeStudents.toLocaleString("id-ID"),
      },
      {
        key: "coachCount",
        header: "Coach",
        render: (trainingCenter) => trainingCenter.coachCount.toLocaleString("id-ID"),
      },
      {
        key: "status",
        header: "Status",
        render: (trainingCenter) => (
          <StatusBadge status={trainingCenter.status} variant={statusTone[trainingCenter.status]} />
        ),
      },
    ],
    [],
  );

  const actionColumns = useMemo<DataTableColumn<TrainingCenterDemo>[]>(
    () => [
      ...baseColumns,
      {
        key: "actions",
        header: "Aksi",
        headerClassName: "text-right",
        className: "text-right",
        render: (trainingCenter) => (
          <div className="flex flex-wrap justify-end gap-2">
            <ConfirmActionButton
              className="px-3 py-1 text-xs"
              confirmMessage={`Edit ${trainingCenter.name}? Ini hanya contoh action demo.`}
              label="Edit"
              onConfirm={() => setLastAction(`Edit: ${trainingCenter.name}`)}
            />
            <ConfirmActionButton
              className="px-3 py-1 text-xs"
              confirmMessage={`Hapus ${trainingCenter.name}? Ini hanya contoh action demo.`}
              label="Hapus"
              onConfirm={() => setLastAction(`Hapus: ${trainingCenter.name}`)}
              variant="danger"
            />
          </div>
        ),
      },
    ],
    [baseColumns],
  );

  return (
    // TODO: replace USER_READ with DEV_TOOLS_READ when dev tools permission is available.
    <RequirePermission permissions="USER_READ">
      <AppShell>
        <PageHeader
          title="Dev Table Demo"
          description="Halaman protected untuk memvalidasi state DataTable reusable dengan dummy TrainingCenterDemo."
        />

        <div className="space-y-6">
          <SectionCard title="DataTable Normal" description="Contoh tabel standar dengan dummy TrainingCenterDemo.">
            <DataTable
              columns={baseColumns}
              data={devCrudMockItems}
              getRowKey={(trainingCenter) => trainingCenter.id}
            />
          </SectionCard>

          <SectionCard title="DataTable Loading" description="Contoh skeleton loading saat data masih dimuat.">
            <DataTable
              columns={baseColumns}
              data={[]}
              getRowKey={(trainingCenter) => trainingCenter.id}
              isLoading
            />
          </SectionCard>

          <SectionCard title="DataTable Empty" description="Contoh empty state dengan title dan description khusus.">
            <DataTable
              columns={baseColumns}
              data={[]}
              emptyTitle="Belum ada training center demo"
              emptyDescription="Data kosong ini disengaja untuk mengecek copy empty state di halaman admin."
              getRowKey={(trainingCenter) => trainingCenter.id}
            />
          </SectionCard>

          <SectionCard title="DataTable Error" description="Contoh pesan error dummy saat request gagal.">
            <DataTable
              columns={baseColumns}
              data={[]}
              error={tableErrorMessage}
              getRowKey={(trainingCenter) => trainingCenter.id}
            />
          </SectionCard>

          <SectionCard
            title="DataTable Action Column"
            description="Contoh kolom action edit/delete memakai render function agar mudah dicopy ke tabel fitur lain."
            actions={<span className="text-xs text-zinc-500">{lastAction}</span>}
          >
            <DataTable
              columns={actionColumns}
              data={devCrudMockItems}
              getRowKey={(trainingCenter) => trainingCenter.id}
            />
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
