"use client";

import { useQuery } from "@tanstack/react-query";

import RequirePermission from "@/app/components/require-permission";
import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { useStandardErrorRedirect } from "@/lib/error-handler";
import { QUERY_KEYS } from "@/lib/query-keys";
import { catalogService } from "@/features/catalog/catalog.service";

export default function CatalogPage() {
  const handleErrorRedirect = useStandardErrorRedirect();

  const catalogQuery = useQuery({
    queryKey: QUERY_KEYS.catalog,
    queryFn: catalogService.getSummaries,
  });

  if (catalogQuery.error) {
    handleErrorRedirect(catalogQuery.error);
  }

  return (
    <RequirePermission permissions={["CLASS_READ", "CLASS_WRITE"]} mode="any">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Catalog Module</h1>
        <p className="mt-2 text-zinc-600">Data katalog dari endpoint /api/catalog.</p>

        {catalogQuery.isLoading ? <LoadingSkeleton rows={4} /> : null}

        {catalogQuery.isError ? (
          <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            Gagal mengambil data catalog.
          </p>
        ) : null}

        {catalogQuery.data?.length === 0 ? (
          <EmptyState
            title="Data kosong"
            description="Belum ada data catalog yang dapat ditampilkan."
          />
        ) : null}

        {catalogQuery.data?.length ? (
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
    </RequirePermission>
  );
}
