"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import { Academy } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";
import { AcademyFormValues } from "@/features/academies/academy.schema";
import { academyService } from "@/features/academies/academy.service";
import AcademyForm from "@/features/academies/components/academy-form";
import AcademyTable from "@/features/academies/components/academy-table";

export default function AcademiesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);

  const [search, setSearch] = useState("");
  const [selectedAcademy, setSelectedAcademy] = useState<Academy | null>(null);

  const canWrite = useMemo(() => can(authorities, "ACADEMY_WRITE"), [authorities]);

  const academiesQuery = useQuery({
    queryKey: [...QUERY_KEYS.academies.list(), search],
    queryFn: () => academyService.list({ search }),
  });

  const createMutation = useMutation({
    mutationFn: (values: AcademyFormValues) =>
      academyService.create({
        code: values.code,
        name: values.name,
        description: values.description || undefined,
        phone: values.phone || undefined,
        email: values.email || undefined,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: AcademyFormValues }) =>
      academyService.update(id, {
        name: values.name,
        description: values.description || undefined,
        phone: values.phone || undefined,
        email: values.email || undefined,
      }),
    onSuccess: async () => {
      setSelectedAcademy(null);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => academyService.remove(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.academies.all });
    },
  });

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <RequirePermission permissions="ACADEMY_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Academies</h1>

        <div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-zinc-700">Search</span>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2"
              placeholder="Cari code / name academy"
            />
          </label>
        </div>

        <div className="mt-6">
          {academiesQuery.isLoading ? <p>Memuat academies...</p> : null}
          {academiesQuery.data ? (
            <AcademyTable
              academies={academiesQuery.data}
              canWrite={canWrite}
              onEdit={setSelectedAcademy}
              onDelete={(academy) => deleteMutation.mutate(academy.id)}
            />
          ) : null}
        </div>

        <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="text-lg font-semibold text-zinc-900">
            {selectedAcademy ? "Edit Academy" : "Tambah Academy"}
          </h2>
          <div className="mt-3">
            <AcademyForm
              initialData={selectedAcademy}
              canWrite={canWrite}
              isSubmitting={isSubmitting}
              onCancelEdit={() => setSelectedAcademy(null)}
              onSubmit={(values) => {
                if (selectedAcademy) {
                  updateMutation.mutate({ id: selectedAcademy.id, values });
                  return;
                }
                createMutation.mutate(values);
              }}
            />
          </div>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
