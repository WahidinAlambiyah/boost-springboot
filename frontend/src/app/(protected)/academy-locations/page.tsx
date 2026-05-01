"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import { AcademyLocation } from "@/lib/api-types";
import { academyService } from "@/features/academies/academy.service";
import AcademyLocationForm from "@/features/academy-locations/components/academy-location-form";
import AcademyLocationTable from "@/features/academy-locations/components/academy-location-table";
import { AcademyLocationFormValues } from "@/features/academy-locations/academy-location.schema";
import { academyLocationService } from "@/features/academy-locations/academy-location.service";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function AcademyLocationsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const [academyId, setAcademyId] = useState("");
  const [selectedLocation, setSelectedLocation] = useState<AcademyLocation | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const canWrite = useMemo(() => can(authorities, "LOCATION_WRITE"), [authorities]);

  const academiesQuery = useQuery({ queryKey: QUERY_KEYS.academies.list(), queryFn: () => academyService.list() });
  const locationsQuery = useQuery({
    queryKey: QUERY_KEYS.academyLocations.byAcademy(academyId || undefined),
    queryFn: () => academyLocationService.list({ academyId: academyId || undefined }),
  });

  const invalidateLocations = async () => queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academyLocations.all });

  const createMutation = useMutation({ mutationFn: (values: AcademyLocationFormValues) => academyLocationService.create(values), onSuccess: async () => { setFormVersion((prev) => prev + 1); await invalidateLocations(); } });
  const updateMutation = useMutation({ mutationFn: ({ id, values }: { id: string; values: AcademyLocationFormValues }) => academyLocationService.update(id, values), onSuccess: async () => { setSelectedLocation(null); setFormVersion((prev) => prev + 1); await invalidateLocations(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => academyLocationService.remove(id), onSuccess: invalidateLocations });

  return (
    <RequirePermission permissions="LOCATION_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Academy Locations</h1>
        <div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4">
          <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Filter Academy</span>
            <select value={academyId} onChange={(event) => setAcademyId(event.target.value)} className="w-full rounded-md border border-zinc-300 px-3 py-2"><option value="">Semua academy</option>{academiesQuery.data?.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select>
          </label>
        </div>
        <div className="mt-6">{locationsQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}{locationsQuery.isError ? <ErrorMessage message="Gagal memuat academy locations." /> : null}{locationsQuery.data && locationsQuery.data.length === 0 ? <EmptyState title="Belum ada location" description="Silakan tambah location baru." /> : null}{locationsQuery.data && locationsQuery.data.length > 0 ? <AcademyLocationTable locations={locationsQuery.data} academies={academiesQuery.data ?? []} canWrite={canWrite} onEdit={setSelectedLocation} onDelete={(location) => deleteMutation.mutate(location.id)} /> : null}{(createMutation.error || updateMutation.error || deleteMutation.error) ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}</div>
        <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4"><h2 className="text-lg font-semibold text-zinc-900">{selectedLocation ? "Edit Location" : "Tambah Location"}</h2><div className="mt-3"><AcademyLocationForm key={`location-form-${selectedLocation?.id ?? "new"}-${formVersion}`} academies={academiesQuery.data ?? []} initialData={selectedLocation} canWrite={canWrite} isSubmitting={createMutation.isPending || updateMutation.isPending} onCancelEdit={() => setSelectedLocation(null)} onSubmit={(values) => selectedLocation ? updateMutation.mutate({ id: selectedLocation.id, values }) : createMutation.mutate(values)} /></div></div>
      </AppShell>
    </RequirePermission>
  );
}
