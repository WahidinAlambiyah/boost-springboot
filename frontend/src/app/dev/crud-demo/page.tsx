"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import { PageHeader } from "@/app/components/page-header";
import { SearchInput } from "@/app/components/search-input";
import { SectionCard } from "@/app/components/section-card";
import DevCrudForm from "@/features/dev-crud/components/dev-crud-form";
import DevCrudTable from "@/features/dev-crud/components/dev-crud-table";
import type { DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { devCrudService } from "@/features/dev-crud/dev-crud.service";
import { DEV_TOOLS_WRITE_PERMISSIONS, type TrainingCenterDemo } from "@/features/dev-crud/dev-crud.types";
import { canAny } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function DevCrudDemoPage() {
  const authorities = useAuthStore((state) => state.authorities);
  // TODO: replace fallback write checks with DEV_TOOLS_WRITE when write dev tools permission is available.
  const canWrite = useMemo(() => canAny(authorities, [...DEV_TOOLS_WRITE_PERMISSIONS]), [authorities]);
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<TrainingCenterDemo | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const [mutationError, setMutationError] = useState<string>();

  const trainingCentersQuery = useQuery({
    queryKey: QUERY_KEYS.devCrudTrainingCenters.filter({ search: search || undefined }),
    queryFn: () => devCrudService.list({ search }),
  });

  const resetFormState = () => {
    setSelected(null);
    setFormVersion((current) => current + 1);
  };

  const invalidateTrainingCenters = async () => {
    await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.devCrudTrainingCenters.all });
  };

  const createMutation = useMutation({
    mutationFn: (payload: DevCrudFormValues) => devCrudService.create(payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Training center demo gagal ditambahkan."),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: DevCrudFormValues }) => devCrudService.update(id, payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Training center demo gagal diperbarui."),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => devCrudService.remove(id),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Training center demo gagal dihapus."),
  });

  const handleSubmit = (values: DevCrudFormValues) => {
    if (selected) {
      updateMutation.mutate({ id: selected.id, payload: values });
      return;
    }

    createMutation.mutate(values);
  };

  const handleDelete = (item: TrainingCenterDemo) => {
    deleteMutation.mutate(item.id);
  };

  const handleStartCreate = () => {
    setMutationError(undefined);
    resetFormState();
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending || deleteMutation.isPending;
  const queryError = trainingCentersQuery.isError ? "Gagal memuat data training center demo." : undefined;

  return (
    <AppShell>
      <PageHeader
        title="Dev CRUD Demo"
        description="Playground protected untuk mencoba pola create, read, update, dan delete training center berbasis localStorage."
        actions={
          <button
            type="button"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={!canWrite}
            onClick={handleStartCreate}
          >
            Tambah Training Center
          </button>
        }
      />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
        <SectionCard title="Training Center Mock" description="Tabel memakai React Query dan service localStorage agar aman untuk eksplorasi developer.">
          <SearchInput
            className="mb-4"
            label="Filter training center demo"
            placeholder="Filter code, name, atau location..."
            value={search}
            onChange={setSearch}
          />
          <DevCrudTable
            items={trainingCentersQuery.data ?? []}
            canWrite={canWrite}
            error={queryError}
            isLoading={trainingCentersQuery.isLoading}
            onEdit={setSelected}
            onDelete={handleDelete}
          />
        </SectionCard>

        <SectionCard title={selected ? "Edit Training Center" : "Tambah Training Center"} description="Form memakai react-hook-form dan zod schema.">
          {mutationError ? <ErrorMessage className="mb-4" message={mutationError} /> : null}
          <DevCrudForm
            key={`dev-crud-form-${selected?.id ?? "new"}-${formVersion}`}
            initialData={selected}
            canWrite={canWrite}
            isSubmitting={isSubmitting}
            onCancelEdit={() => setSelected(null)}
            onSubmit={handleSubmit}
          />
        </SectionCard>
      </div>
    </AppShell>
  );
}
