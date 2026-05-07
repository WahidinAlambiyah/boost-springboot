"use client";

import type React from "react";

import { ErrorMessage } from "./error-message";

export interface DataTableColumn<T> {
  key: string;
  header: React.ReactNode;
  render: (row: T, index: number) => React.ReactNode;
  className?: string;
  headerClassName?: string;
}

export interface DataTableActionColumn<T> {
  key?: string;
  header?: React.ReactNode;
  render: (row: T, index: number) => React.ReactNode;
  className?: string;
  headerClassName?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  isLoading?: boolean;
  error?: string;
  errorTitle?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  loadingRows?: number;
  getRowKey?: (row: T, index: number) => React.Key;
  actionColumn?: DataTableActionColumn<T>;
  ariaLabel?: string;
  className?: string;
}

function getColumnClassName(...classNames: Array<string | undefined>) {
  return classNames.filter(Boolean).join(" ");
}

function renderSkeletonRows(rowCount: number, columnCount: number) {
  return Array.from({ length: rowCount }).map((_, rowIndex) => (
    <tr key={`loading-row-${rowIndex}`}>
      {Array.from({ length: columnCount }).map((__, columnIndex) => (
        <td key={`loading-cell-${rowIndex}-${columnIndex}`} className="px-4 py-3">
          <div className="h-4 w-full min-w-20 animate-pulse rounded bg-zinc-200" />
        </td>
      ))}
    </tr>
  ));
}

export function DataTable<T>({
  columns,
  data,
  isLoading = false,
  error,
  errorTitle,
  emptyTitle = "No data available",
  emptyDescription = "Belum ada data untuk ditampilkan.",
  loadingRows = 5,
  getRowKey,
  actionColumn,
  ariaLabel,
  className,
}: DataTableProps<T>) {
  const resolvedColumns: DataTableColumn<T>[] = actionColumn
    ? [
        ...columns,
        {
          key: actionColumn.key ?? "actions",
          header: actionColumn.header ?? "Aksi",
          render: actionColumn.render,
          className: getColumnClassName("text-right", actionColumn.className),
          headerClassName: getColumnClassName("text-right", actionColumn.headerClassName),
        },
      ]
    : columns;

  const containerClassName = getColumnClassName(
    "w-full max-w-full overflow-hidden rounded-lg border border-zinc-200 bg-white shadow-sm",
    className,
  );
  const colSpan = Math.max(resolvedColumns.length, 1);

  return (
    <div className={containerClassName}>
      <div className="w-full overflow-x-auto">
        <table className="w-full min-w-max divide-y divide-zinc-200 text-sm" aria-label={ariaLabel}>
          <thead className="bg-zinc-50">
            <tr>
              {resolvedColumns.map((column) => (
                <th
                  key={column.key}
                  className={getColumnClassName(
                    "whitespace-nowrap px-4 py-3 text-left font-semibold text-zinc-700",
                    column.headerClassName,
                  )}
                  scope="col"
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 bg-white">
            {isLoading ? renderSkeletonRows(Math.max(loadingRows, 1), colSpan) : null}

            {!isLoading && error ? (
              <tr>
                <td colSpan={colSpan} className="px-4 py-4">
                  <ErrorMessage title={errorTitle} message={error} />
                </td>
              </tr>
            ) : null}

            {!isLoading && !error && data.length === 0 ? (
              <tr>
                <td colSpan={colSpan} className="px-4 py-8 text-center">
                  <div className="mx-auto max-w-md rounded-lg border border-dashed border-zinc-300 bg-zinc-50 p-6">
                    <h3 className="text-base font-semibold text-zinc-900">{emptyTitle}</h3>
                    <p className="mt-2 text-sm text-zinc-600">{emptyDescription}</p>
                  </div>
                </td>
              </tr>
            ) : null}

            {!isLoading && !error
              ? data.map((row, index) => (
                  <tr
                    key={getRowKey ? getRowKey(row, index) : index}
                    className="transition hover:bg-zinc-50"
                  >
                    {resolvedColumns.map((column) => (
                      <td
                        key={column.key}
                        className={getColumnClassName(
                          "whitespace-nowrap px-4 py-3 text-zinc-700",
                          column.className,
                        )}
                      >
                        {column.render(row, index)}
                      </td>
                    ))}
                  </tr>
                ))
              : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
