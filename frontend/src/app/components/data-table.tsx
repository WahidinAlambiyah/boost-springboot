"use client";

import type { ReactNode } from "react";
import { ErrorMessage } from "./error-message";
import EmptyState from "./empty-state";
import LoadingSkeleton from "./loading-skeleton";

export interface DataTableColumn<T> {
  key: string;
  header: ReactNode;
  cell?: (row: T, index: number) => ReactNode;
  render?: (row: T, index: number) => ReactNode;
  className?: string;
  headerClassName?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  getRowKey?: (row: T, index: number) => string;
  /** @deprecated Use getRowKey instead. Kept for existing pages during migration. */
  rowKey?: (row: T, index: number) => string;
  isLoading?: boolean;
  error?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  /** @deprecated Use emptyTitle and emptyDescription instead. */
  emptyMessage?: string;
  className?: string;
}

export function DataTable<T>({
  columns,
  data,
  getRowKey,
  rowKey,
  isLoading = false,
  error,
  emptyTitle = "No data available",
  emptyDescription,
  emptyMessage,
  className,
}: DataTableProps<T>) {
  const resolveRowKey = getRowKey ?? rowKey;
  const resolvedEmptyDescription = emptyDescription ?? emptyMessage ?? "Belum ada data untuk ditampilkan.";

  if (isLoading) {
    return <LoadingSkeleton rows={5} className={className} />;
  }

  if (error) {
    return <ErrorMessage className={className} message={error} />;
  }

  if (data.length === 0) {
    return <EmptyState title={emptyTitle} description={resolvedEmptyDescription} />;
  }

  return (
    <div className={["w-full overflow-x-auto rounded-lg border border-zinc-200 bg-white", className].filter(Boolean).join(" ")}>
      <table className="min-w-full divide-y divide-zinc-200 text-sm">
        <thead className="bg-zinc-50">
          <tr>
            {columns.map((column) => (
              <th key={column.key} className={["whitespace-nowrap px-4 py-3 text-left font-semibold text-zinc-700", column.headerClassName].filter(Boolean).join(" ")}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-100">
          {data.map((row, index) => (
            <tr key={resolveRowKey ? resolveRowKey(row, index) : index} className="hover:bg-zinc-50">
              {columns.map((column) => {
                const renderCell = column.cell ?? column.render;

                return (
                  <td key={column.key} className={["px-4 py-3 text-zinc-700", column.className].filter(Boolean).join(" ")}>
                    {renderCell ? renderCell(row, index) : null}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
