"use client";

import { useQuery } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { NotificationSummary } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { api } from "@/lib/api";
import { useStandardErrorRedirect } from "@/lib/error-handler";

const NOTIFICATION_QUERY_KEY = ["notification"];

const statusBadgeClass = (status: string) => {
  const normalizedStatus = status.toUpperCase();

  if (normalizedStatus.includes("SENT") || normalizedStatus.includes("DELIVERED")) {
    return "bg-emerald-100 text-emerald-700 border-emerald-200";
  }

  if (normalizedStatus.includes("PENDING") || normalizedStatus.includes("QUEUE")) {
    return "bg-amber-100 text-amber-700 border-amber-200";
  }

  if (normalizedStatus.includes("FAILED") || normalizedStatus.includes("ERROR")) {
    return "bg-red-100 text-red-700 border-red-200";
  }

  return "bg-zinc-100 text-zinc-700 border-zinc-200";
};

export default function NotificationPage() {
  const handleErrorRedirect = useStandardErrorRedirect();

  const notificationQuery = useQuery({
    queryKey: NOTIFICATION_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get<ApiResponse<NotificationSummary[]>>("/api/notification");
      return response.data.data;
    },
  });

  if (notificationQuery.error) {
    handleErrorRedirect(notificationQuery.error);
  }

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Notification Module</h1>
      <p className="mt-2 text-zinc-600">Ringkasan notifikasi terbaru.</p>

      {notificationQuery.isLoading ? (
        <p className="mt-4 text-sm text-zinc-600">Memuat notifikasi...</p>
      ) : null}

      {notificationQuery.isError ? (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Gagal mengambil data notification.
        </p>
      ) : null}

      {notificationQuery.data ? (
        <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-100 text-zinc-700">
              <tr>
                <th className="px-4 py-2">Code</th>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {notificationQuery.data.map((item) => (
                <tr key={item.code} className="border-t border-zinc-200 text-zinc-800">
                  <td className="px-4 py-2">{item.code}</td>
                  <td className="px-4 py-2">{item.name}</td>
                  <td className="px-4 py-2">
                    <span
                      className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-semibold ${statusBadgeClass(item.status)}`}
                    >
                      {item.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </AppShell>
  );
}
