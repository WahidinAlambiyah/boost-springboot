export interface HttpDebugEntry {
  requestId: string;
  method: string;
  path: string;
  status: number;
  durationMs: number;
  timestamp: string;
}

const MAX_ENTRIES = 20;
let entries: HttpDebugEntry[] = [];
const listeners = new Set<() => void>();

const notify = () => {
  listeners.forEach((listener) => listener());
};

export const addHttpDebugEntry = (entry: HttpDebugEntry) => {
  entries = [entry, ...entries].slice(0, MAX_ENTRIES);
  notify();
};

export const getHttpDebugEntries = () => entries;

export const subscribeHttpDebugEntries = (listener: () => void) => {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
};
