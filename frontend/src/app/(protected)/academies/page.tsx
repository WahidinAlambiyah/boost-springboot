"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import { AcademyFormValues } from "@/features/academies/academy.schema";
import AcademyForm from "@/features/academies/components/academy-form";
import AcademyTable from "@/features/academies/components/academy-table";
import { academyService } from "@/features/academies/academy.service";
import { Academy } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function AcademiesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);

  const [search, setSearch] = useState("");
  const [selectedAcademy, setSelectedAcademy] = useState<Academy | null>(null);
  const [formVersion, setFormVersion] = useState(0);

  const canWrite = useMemo(() => can(authorities, "ACADEMY_WRITE"), [authorities]);

  const academiesQuery = useQuery({
    queryKey: QUERY_KEYS.academies.filter({ search: search || undefined }),
    queryFn: () => academyService.list({ search }),
  });

  const createMutation = useMutation({
    mutationFn: (values: AcademyFormValues) =>
      academyService.create({ code: values.code, name: values.name, description: values.description || undefined, phone: values.phone || undefined, email: values.email || undefined }),
    onSuccess: async () => {
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: AcademyFormValues }) => academyService.update(id, { name: values.name, description: values.description || undefined, phone: values.phone || undefined, email: values.email || undefined }),
    onSuccess: async () => {
      setSelectedAcademy(null);
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all });
    },
  });

  const deleteMutation = useMutation({ mutationFn: (id: string) => academyService.remove(id), onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all }); } });

  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;

  return (
    <RequirePermission permissions="ACADEMY_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Academies</h1>

        <div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-zinc-700">Search</span>
            <input value={search} onChange={(event) => setSearch(event.target.value)} className="w-full rounded-md border border-zinc-300 px-3 py-2" placeholder="Cari code / name academy" />
          </label>
        </div>

        <div className="mt-6">
          {academiesQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}
          {academiesQuery.isError ? <ErrorMessage className="mt-4" message="Gagal memuat data academy." /> : null}
          {academiesQuery.data && academiesQuery.data.length === 0 ? <EmptyState title="Belum ada academy" description="Silakan tambah academy baru untuk mulai." /> : null}
          {academiesQuery.data && academiesQuery.data.length > 0 ? <AcademyTable academies={academiesQuery.data} canWrite={canWrite} onEdit={setSelectedAcademy} onDelete={(academy) => deleteMutation.mutate(academy.id)} /> : null}
          {mutationError ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}
        </div>

        <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="text-lg font-semibold text-zinc-900">{selectedAcademy ? "Edit Academy" : "Tambah Academy"}</h2>
          <div className="mt-3">
            <AcademyForm key={`academy-form-${selectedAcademy?.id ?? "new"}-${formVersion}`} initialData={selectedAcademy} canWrite={canWrite} isSubmitting={isSubmitting} onCancelEdit={() => setSelectedAcademy(null)} onSubmit={(values) => {
              if (selectedAcademy) {
                updateMutation.mutate({ id: selectedAcademy.id, values });
                return;
              }
              createMutation.mutate(values);
            }} />
          </div>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
