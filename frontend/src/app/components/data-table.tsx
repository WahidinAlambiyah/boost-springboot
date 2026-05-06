"use client";

import type React from "react";
import { ErrorMessage } from "./error-message";
import EmptyState from "./empty-state";
import LoadingSkeleton from "./loading-skeleton";

export interface DataTableColumn<T> {
  key: string;
  header: React.ReactNode;
  render: (row: T, index: number) => React.ReactNode;
  className?: string;
  headerClassName?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  isLoading?: boolean;
  error?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  getRowKey?: (row: T, index: number) => string;
}

export function DataTable<T>({
  columns,
  data,
  isLoading = false,
  error,
  emptyTitle = "No data available",
  emptyDescription = "Belum ada data untuk ditampilkan.",
  getRowKey,
}: DataTableProps<T>) {
  if (isLoading) {
    return <LoadingSkeleton rows={5} />;
  }

  if (error) {
    return <ErrorMessage message={error} />;
  }

  if (data.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-zinc-200 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-zinc-200 text-sm">
        <thead className="bg-zinc-50">
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                className={[
                  "whitespace-nowrap px-4 py-3 text-left font-semibold text-zinc-700",
                  column.headerClassName,
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-100">
          {data.map((row, index) => (
            <tr
              key={getRowKey ? getRowKey(row, index) : index}
              className="hover:bg-zinc-50"
            >
              {columns.map((column) => (
                <td
                  key={column.key}
                  className={["px-4 py-3 text-zinc-700", column.className]
                    .filter(Boolean)
                    .join(" ")}
                >
                  {column.render(row, index)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
