"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import DevCrudForm from "@/features/dev-crud/components/dev-crud-form";
import DevCrudTable from "@/features/dev-crud/components/dev-crud-table";
import type { DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { devCrudService } from "@/features/dev-crud/dev-crud.service";
import { DEV_TOOLS_READ_PERMISSIONS, DEV_TOOLS_WRITE_PERMISSIONS, type TrainingCenterDemo } from "@/features/dev-crud/dev-crud.types";
import { canAny } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function DevCrudDemoPage() {
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => canAny(authorities, [...DEV_TOOLS_WRITE_PERMISSIONS]), [authorities]);
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<TrainingCenterDemo | null>(null);
  const [mutationError, setMutationError] = useState<string>();

  const trainingCentersQuery = useQuery({
    queryKey: QUERY_KEYS.devCrudTrainingCenters.list(),
    queryFn: () => devCrudService.list(),
  });

  const invalidateTrainingCenters = async () => {
    await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.devCrudTrainingCenters.all });
  };

  const createMutation = useMutation({
    mutationFn: (payload: DevCrudFormValues) => devCrudService.create(payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      setSelected(null);
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Training center demo gagal ditambahkan."),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: DevCrudFormValues }) => devCrudService.update(id, payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      setSelected(null);
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Training center demo gagal diperbarui."),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => devCrudService.remove(id),
    onMutate: () => setMutationError(undefined),
    onSuccess: async (_data, deletedId) => {
      if (selected?.id === deletedId) {
        setSelected(null);
      }
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

  const isSubmitting = createMutation.isPending || updateMutation.isPending || deleteMutation.isPending;
  const queryError = trainingCentersQuery.isError ? "Gagal memuat data training center demo." : undefined;

  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev CRUD Demo"
          description="Playground protected untuk mencoba pola create, read, update, dan delete training center berbasis localStorage."
        />

        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
          <SectionCard title="Training Center Mock" description="Tabel memakai React Query dan service localStorage agar aman untuk eksplorasi developer.">
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
              initialData={selected}
              canWrite={canWrite}
              isSubmitting={isSubmitting}
              onCancelEdit={() => setSelected(null)}
              onSubmit={handleSubmit}
            />
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
