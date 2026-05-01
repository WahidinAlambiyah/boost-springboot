"use client";

import type { ReactNode } from "react";

export interface DataTableColumn<T> {
  key: string;
  header: ReactNode;
  cell: (row: T, index: number) => ReactNode;
  className?: string;
  headerClassName?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  rowKey: (row: T, index: number) => string;
  emptyMessage?: string;
  className?: string;
}

export function DataTable<T>({ columns, data, rowKey, emptyMessage = "No data available.", className }: DataTableProps<T>) {
  return (
    <div className={["overflow-x-auto rounded-lg border border-zinc-200 bg-white", className].filter(Boolean).join(" ")}>
      <table className="min-w-full divide-y divide-zinc-200 text-sm">
        <thead className="bg-zinc-50">
          <tr>
            {columns.map((column) => (
              <th key={column.key} className={["px-4 py-3 text-left font-semibold text-zinc-700", column.headerClassName].filter(Boolean).join(" ")}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-100">
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-6 text-center text-zinc-500">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, index) => (
              <tr key={rowKey(row, index)} className="hover:bg-zinc-50">
                {columns.map((column) => (
                  <td key={column.key} className={["px-4 py-3 text-zinc-700", column.className].filter(Boolean).join(" ")}>
                    {column.cell(row, index)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
