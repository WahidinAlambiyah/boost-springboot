"use client";

import { useMemo, useState, useSyncExternalStore } from "react";

import { getHttpDebugEntries, subscribeHttpDebugEntries } from "@/lib/http-debug-store";

const isDebugEnabled =
  process.env.NEXT_PUBLIC_HTTP_DEBUG === "true" && process.env.NODE_ENV !== "production";

const matchesFilter = (value: string, query: string) => value.toLowerCase().includes(query.toLowerCase());

export default function HttpDebugDrawer() {
  const entries = useSyncExternalStore(subscribeHttpDebugEntries, getHttpDebugEntries, getHttpDebugEntries);
  const [open, setOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState("");
  const [methodFilter, setMethodFilter] = useState("");
  const [pathFilter, setPathFilter] = useState("");

  const filteredEntries = useMemo(() => {
    return entries.filter((entry) => {
      const statusMatch = statusFilter ? matchesFilter(String(entry.status), statusFilter) : true;
      const methodMatch = methodFilter ? matchesFilter(entry.method, methodFilter) : true;
      const pathMatch = pathFilter ? matchesFilter(entry.path, pathFilter) : true;
      return statusMatch && methodMatch && pathMatch;
    });
  }, [entries, methodFilter, pathFilter, statusFilter]);

  const handleCopy = async (requestId: string) => {
    await navigator.clipboard.writeText(requestId);
  };

  if (!isDebugEnabled) {
    return null;
  }

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="fixed bottom-4 right-4 z-40 rounded-md bg-zinc-900 px-3 py-2 text-xs font-semibold text-white shadow-lg"
      >
        HTTP Debug
      </button>

      {open ? (
        <aside className="fixed inset-y-0 right-0 z-50 flex w-full max-w-2xl flex-col border-l border-zinc-200 bg-white shadow-2xl">
          <div className="flex items-center justify-between border-b border-zinc-200 px-4 py-3">
            <h2 className="text-sm font-semibold text-zinc-900">HTTP Debug Drawer</h2>
            <button type="button" onClick={() => setOpen(false)} className="text-sm text-zinc-600 hover:text-zinc-900">
              Close
            </button>
          </div>

          <div className="grid grid-cols-1 gap-3 border-b border-zinc-200 px-4 py-3 md:grid-cols-3">
            <input value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)} placeholder="Filter status" className="rounded border border-zinc-300 px-2 py-1 text-sm" />
            <input value={methodFilter} onChange={(event) => setMethodFilter(event.target.value)} placeholder="Filter method" className="rounded border border-zinc-300 px-2 py-1 text-sm" />
            <input value={pathFilter} onChange={(event) => setPathFilter(event.target.value)} placeholder="Filter path" className="rounded border border-zinc-300 px-2 py-1 text-sm" />
          </div>

          <div className="flex-1 overflow-auto px-4 py-3">
            {filteredEntries.length === 0 ? (
              <p className="text-sm text-zinc-500">No request entries match current filters.</p>
            ) : (
              <ul className="space-y-3">
                {filteredEntries.map((entry) => (
                  <li key={`${entry.requestId}-${entry.timestamp}`} className="rounded-md border border-zinc-200 p-3 text-sm">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded bg-zinc-100 px-2 py-0.5 font-semibold">{entry.method}</span>
                      <span className="font-semibold text-zinc-800">{entry.status}</span>
                      <span className="text-zinc-700">{entry.path}</span>
                    </div>
                    <p className="mt-1 text-xs text-zinc-500">{new Date(entry.timestamp).toLocaleTimeString()} • {entry.durationMs}ms</p>
                    <div className="mt-2 flex items-center gap-2 text-xs">
                      <code className="rounded bg-zinc-100 px-2 py-1">{entry.requestId}</code>
                      <button type="button" onClick={() => void handleCopy(entry.requestId)} className="rounded border border-zinc-300 px-2 py-1 hover:bg-zinc-100">
                        Copy requestId
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </aside>
      ) : null}
    </>
  );
}
