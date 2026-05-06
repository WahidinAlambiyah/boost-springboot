"use client";

export interface PaginationBarProps {
  page: number;
  size: number;
  totalElements?: number;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

export function PaginationBar({ page, size, totalElements, onPageChange, onSizeChange }: PaginationBarProps) {
  const totalPages = typeof totalElements === "number" ? Math.max(1, Math.ceil(totalElements / size)) : undefined;
  const isFirstPage = page <= 0;
  const isLastPage = totalPages ? page >= totalPages - 1 : false;

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-zinc-200 bg-white px-4 py-3 text-sm text-zinc-700 shadow-sm">
      <div className="flex items-center gap-2">
        <span>Rows per page</span>
        <select
          value={size}
          onChange={(event) => onSizeChange(Number(event.target.value))}
          className="rounded-md border border-zinc-300 bg-white px-2 py-1 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
        >
          {PAGE_SIZE_OPTIONS.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </div>

      <div className="flex items-center gap-3">
        <span className="text-zinc-600">
          Page {page + 1}
          {totalPages ? ` of ${totalPages}` : null}
          {typeof totalElements === "number" ? ` · ${totalElements} total` : null}
        </span>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={isFirstPage}
            className="rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm font-medium text-zinc-700 shadow-sm transition hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Previous
          </button>
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={isLastPage}
            className="rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm font-medium text-zinc-700 shadow-sm transition hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
