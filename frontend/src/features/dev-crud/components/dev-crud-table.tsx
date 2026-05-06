"use client";

import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { StatusBadge } from "@/app/components/status-badge";
import type { TrainingCenterDemo, TrainingCenterDemoStatus } from "@/features/dev-crud/dev-crud.types";

interface DevCrudTableProps {
  items: TrainingCenterDemo[];
  canWrite?: boolean;
  isLoading?: boolean;
  error?: string;
  onEdit?: (item: TrainingCenterDemo) => void;
  onDelete?: (item: TrainingCenterDemo) => void;
}

const statusTone: Record<TrainingCenterDemoStatus, "default" | "success"> = {
  ACTIVE: "success",
  INACTIVE: "default",
};

export default function DevCrudTable({
  items,
  canWrite = false,
  isLoading = false,
  error,
  onEdit,
  onDelete,
}: DevCrudTableProps) {
  const columns: DataTableColumn<TrainingCenterDemo>[] = [
    {
      key: "code",
      header: "Kode",
      render: (item) => <span className="font-medium text-zinc-900">{item.code}</span>,
    },
    {
      key: "name",
      header: "Nama",
      render: (item) => item.name,
    },
    {
      key: "location",
      header: "Lokasi",
      render: (item) => item.location,
    },
    {
      key: "activeStudents",
      header: "Murid Aktif",
      render: (item) => item.activeStudents.toLocaleString("id-ID"),
    },
    {
      key: "coachCount",
      header: "Coach",
      render: (item) => item.coachCount.toLocaleString("id-ID"),
    },
    {
      key: "status",
      header: "Status",
      render: (item) => <StatusBadge status={item.status} variant={statusTone[item.status]} />,
    },
  ];

  if (canWrite) {
    columns.push({
      key: "actions",
      header: "Aksi",
      render: (item) => (
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className="rounded-md border border-zinc-300 px-3 py-1 text-xs font-medium text-zinc-700 hover:bg-zinc-50"
            onClick={() => onEdit?.(item)}
          >
            Edit
          </button>
          <ConfirmActionButton
            className="px-3 py-1 text-xs"
            confirmMessage={`Training center ${item.name} akan dihapus.`}
            label="Hapus"
            onConfirm={() => onDelete?.(item)}
            variant="danger"
          />
        </div>
      ),
    });
  }

  return (
    <DataTable
      columns={columns}
      data={items}
      error={error}
      getRowKey={(item) => item.id}
      isLoading={isLoading}
      emptyTitle="Belum ada training center"
      emptyDescription="Gunakan form demo untuk menambahkan data training center lokal."
    />
  );
}
