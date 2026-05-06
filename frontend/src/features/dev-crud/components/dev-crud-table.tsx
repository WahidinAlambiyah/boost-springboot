"use client";

import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { StatusBadge } from "@/app/components/status-badge";
import type { DevCrudItem, DevCrudPriority, DevCrudStatus } from "@/features/dev-crud/dev-crud.types";

interface DevCrudTableProps {
  items: DevCrudItem[];
  canWrite?: boolean;
  isLoading?: boolean;
  error?: string;
  onEdit?: (item: DevCrudItem) => void;
  onDelete?: (item: DevCrudItem) => void;
}

const statusTone: Record<DevCrudStatus, "default" | "success" | "warning" | "danger"> = {
  DRAFT: "warning",
  ACTIVE: "success",
  ARCHIVED: "default",
};

const priorityLabel: Record<DevCrudPriority, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

export default function DevCrudTable({
  items,
  canWrite = false,
  isLoading = false,
  error,
  onEdit,
  onDelete,
}: DevCrudTableProps) {
  const columns: DataTableColumn<DevCrudItem>[] = [
    {
      key: "title",
      header: "Judul",
      render: (item) => <span className="font-medium text-zinc-900">{item.title}</span>,
    },
    {
      key: "owner",
      header: "Owner",
      render: (item) => item.owner,
    },
    {
      key: "status",
      header: "Status",
      render: (item) => <StatusBadge status={item.status} variant={statusTone[item.status]} />,
    },
    {
      key: "priority",
      header: "Prioritas",
      render: (item) => priorityLabel[item.priority],
    },
    {
      key: "dueDate",
      header: "Jatuh Tempo",
      render: (item) => item.dueDate,
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
          <button
            type="button"
            className="rounded-md border border-red-200 px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
            onClick={() => onDelete?.(item)}
          >
            Hapus
          </button>
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
      emptyTitle="Belum ada data dev CRUD"
      emptyDescription="Gunakan form demo untuk menambahkan data mock."
    />
  );
}
