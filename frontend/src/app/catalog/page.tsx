"use client";

import { useQuery } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { CatalogSummary } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { useStandardErrorRedirect } from "@/lib/error-handler";
import { api } from "@/lib/api";

const CATALOG_QUERY_KEY = ["catalog"];

export default function CatalogPage() {
  const handleErrorRedirect = useStandardErrorRedirect();

  const catalogQuery = useQuery({
    queryKey: CATALOG_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get<ApiResponse<CatalogSummary[]>>("/api/catalog");
      return response.data.data;
    },
  });

  if (catalogQuery.error) {
    handleErrorRedirect(catalogQuery.error);
  }

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Catalog Module</h1>
      <p className="mt-2 text-zinc-600">Data katalog dari endpoint /api/catalog.</p>

      {catalogQuery.isLoading ? (
        <p className="mt-4 text-sm text-zinc-600">Memuat data catalog...</p>
      ) : null}

      {catalogQuery.isError ? (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Gagal mengambil data catalog.
        </p>
      ) : null}

      {catalogQuery.data ? (
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
              {catalogQuery.data.map((item) => (
                <tr key={item.code} className="border-t border-zinc-200 text-zinc-800">
                  <td className="px-4 py-2">{item.code}</td>
                  <td className="px-4 py-2">{item.name}</td>
                  <td className="px-4 py-2">{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </AppShell>
  );
}
