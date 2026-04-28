"use client";

import { useEffect, useState } from "react";

import { ApiErrorToastPayload, subscribeApiErrorToast } from "@/lib/http-error-events";

export default function ApiErrorToast() {
  const [toast, setToast] = useState<ApiErrorToastPayload | null>(null);

  useEffect(() => {
    return subscribeApiErrorToast((payload) => {
      setToast(payload);
    });
  }, []);

  useEffect(() => {
    if (!toast) {
      return;
    }

    const timeout = window.setTimeout(() => {
      setToast((current) => (current?.id === toast.id ? null : current));
    }, 5000);

    return () => {
      window.clearTimeout(timeout);
    };
  }, [toast]);

  if (!toast) {
    return null;
  }

  return (
    <div className="fixed bottom-4 right-4 z-50 w-full max-w-sm rounded-md border border-amber-200 bg-amber-50 p-3 shadow-lg">
      <p className="text-sm text-amber-800">{toast.message}</p>
      <div className="mt-3 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={() => setToast(null)}
          className="rounded-md border border-zinc-300 bg-white px-2.5 py-1.5 text-xs text-zinc-700"
        >
          Tutup
        </button>
        {toast.onRetry ? (
          <button
            type="button"
            onClick={() => {
              void toast.onRetry?.();
              setToast(null);
            }}
            className="rounded-md bg-zinc-900 px-2.5 py-1.5 text-xs font-medium text-white"
          >
            {toast.retryLabel ?? "Coba lagi"}
          </button>
        ) : null}
      </div>
    </div>
  );
}
