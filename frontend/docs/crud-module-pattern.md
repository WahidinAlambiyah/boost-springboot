# CRUD Module Pattern

Panduan ini mendefinisikan pola standar untuk membuat modul CRUD di frontend. Contoh memakai struktur `src/features/*`, React Query, React Hook Form, Zod, dan komponen reusable yang sudah tersedia di `frontend/src/app/components/*`.

## Struktur Folder Modul

Gunakan satu folder per domain modul. Ganti `{module-name}`, `{module}`, dan `{route}` sesuai nama fitur.

```text
src/features/{module-name}/{module}.types.ts
src/features/{module-name}/{module}.schema.ts
src/features/{module-name}/{module}.service.ts
src/features/{module-name}/components/{module}-form.tsx
src/features/{module-name}/components/{module}-table.tsx
src/app/(protected)/{route}/page.tsx
```

Contoh mapping untuk modul `training-centers`:

```text
src/features/training-centers/training-center.types.ts
src/features/training-centers/training-center.schema.ts
src/features/training-centers/training-center.service.ts
src/features/training-centers/components/training-center-form.tsx
src/features/training-centers/components/training-center-table.tsx
src/app/(protected)/training-centers/page.tsx
```

## Contoh Types

Definisikan tipe response, payload create/update, dan parameter list di file `{module}.types.ts`. Gunakan nama domain yang eksplisit agar mudah dibaca di komponen dan service.

```ts
export const ENTITY_STATUSES = ["ACTIVE", "INACTIVE"] as const;

export type EntityStatus = (typeof ENTITY_STATUSES)[number];

export interface EntityResponse {
  id: string;
  code: string;
  name: string;
  description?: string;
  status: EntityStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRequest {
  code: string;
  name: string;
  description?: string;
  status: EntityStatus;
}

export type UpdateRequest = Partial<CreateRequest>;

export interface ListParams {
  search?: string;
  status?: EntityStatus | "ALL";
}
```

Catatan:

- `EntityResponse` merepresentasikan data dari API atau adapter service.
- `CreateRequest` berisi field wajib untuk create.
- `UpdateRequest` boleh `Partial<CreateRequest>` jika endpoint update mendukung partial update.
- `ListParams` dipakai sebagai input query dan bagian dari `QUERY_KEYS`.

## Contoh Schema

Definisikan schema validasi di file `{module}.schema.ts`. Pola yang dipakai frontend adalah Zod sebagai resolver React Hook Form.

```ts
import { z } from "zod";

import { ENTITY_STATUSES } from "./entity.types";

export const entityFormSchema = z.object({
  code: z.string().trim().min(2, "Kode minimal 2 karakter").max(32, "Kode maksimal 32 karakter"),
  name: z.string().trim().min(3, "Nama minimal 3 karakter"),
  description: z.string().trim().optional().or(z.literal("")),
  status: z.enum(ENTITY_STATUSES, { message: "Status wajib dipilih" }),
});

export type EntityFormInputValues = z.input<typeof entityFormSchema>;
export type EntityFormValues = z.output<typeof entityFormSchema>;
```

## Contoh Service

Service ditempatkan di `{module}.service.ts` dan menjadi satu pintu akses data untuk page/component. Jika memakai API backend, panggil wrapper HTTP yang sudah dipakai modul terkait; jika memakai mock lokal, tetap pertahankan kontrak method yang sama.

```ts
import type { CreateRequest, EntityResponse, ListParams, UpdateRequest } from "./entity.types";

export const entityService = {
  async list(params?: ListParams): Promise<EntityResponse[]> {
    // return httpClient.get("/entities", { params });
    throw new Error("Implement entityService.list");
  },

  async getById(id: string): Promise<EntityResponse> {
    // return httpClient.get(`/entities/${id}`);
    throw new Error(`Implement entityService.getById for ${id}`);
  },

  async create(payload: CreateRequest): Promise<EntityResponse> {
    // return httpClient.post("/entities", payload);
    throw new Error(`Implement entityService.create for ${payload.code}`);
  },

  async update(id: string, payload: UpdateRequest): Promise<EntityResponse> {
    // return httpClient.patch(`/entities/${id}`, payload);
    throw new Error(`Implement entityService.update for ${id}`);
  },

  async remove(id: string): Promise<void> {
    // return httpClient.delete(`/entities/${id}`);
    throw new Error(`Implement entityService.remove for ${id}`);
  },
};
```

Method minimal yang harus tersedia:

- `list`
- `getById`
- `create`
- `update`
- `remove`

## Contoh React Query

Page protected menjadi orchestration layer untuk query, mutation, permission, dan state UI seperti search/selected row. Gunakan `useQuery` untuk list, `useMutation` untuk create/update/delete, lalu panggil `queryClient.invalidateQueries(...)` setelah mutation sukses.

```tsx
"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SearchInput } from "@/app/components/search-input";
import { SectionCard } from "@/app/components/section-card";
import EntityForm from "@/features/entities/components/entity-form";
import EntityTable from "@/features/entities/components/entity-table";
import type { EntityFormValues } from "@/features/entities/entity.schema";
import { entityService } from "@/features/entities/entity.service";
import type { EntityResponse } from "@/features/entities/entity.types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function EntitiesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "MODULE_WRITE"), [authorities]);

  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<EntityResponse | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const [mutationError, setMutationError] = useState<string>();

  const entitiesQuery = useQuery({
    queryKey: QUERY_KEYS.entities.filter({ search: search || undefined }),
    queryFn: () => entityService.list({ search }),
  });

  const resetFormState = () => {
    setSelected(null);
    setFormVersion((current) => current + 1);
  };

  const invalidateEntities = async () => {
    await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.entities.all });
  };

  const createMutation = useMutation({
    mutationFn: (payload: EntityFormValues) => entityService.create(payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateEntities();
    },
    onError: () => setMutationError("Data gagal ditambahkan."),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: EntityFormValues }) => entityService.update(id, payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateEntities();
    },
    onError: () => setMutationError("Data gagal diperbarui."),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => entityService.remove(id),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      resetFormState();
      await invalidateEntities();
    },
    onError: () => setMutationError("Data gagal dihapus."),
  });

  const handleSubmit = (values: EntityFormValues) => {
    if (selected) {
      updateMutation.mutate({ id: selected.id, payload: values });
      return;
    }

    createMutation.mutate(values);
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending || deleteMutation.isPending;
  const queryError = entitiesQuery.isError ? "Gagal memuat data." : undefined;

  return (
    <RequirePermission permissions="MODULE_READ">
      <AppShell>
        <PageHeader title="Entities" description="Kelola data entity." />

        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
          <SectionCard title="Daftar Entity" description="Filter dan kelola data entity.">
            <SearchInput
              className="mb-4"
              label="Cari entity"
              placeholder="Filter code atau name..."
              value={search}
              onChange={setSearch}
            />
            <EntityTable
              items={entitiesQuery.data ?? []}
              canWrite={canWrite}
              error={queryError}
              isLoading={entitiesQuery.isLoading}
              onEdit={setSelected}
              onDelete={(item) => deleteMutation.mutate(item.id)}
            />
          </SectionCard>

          <SectionCard title={selected ? "Edit Entity" : "Tambah Entity"} description="Form memakai React Hook Form dan Zod.">
            {mutationError ? <ErrorMessage className="mb-4" message={mutationError} /> : null}
            <EntityForm
              key={`entity-form-${selected?.id ?? "new"}-${formVersion}`}
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
```

Pastikan menambahkan key modul ke `QUERY_KEYS`, misalnya:

```ts
entities: createEntityKeys<{ search?: string; status?: string }>("entities"),
```

## Contoh Form

Form memakai React Hook Form, Zod schema, `FormField`, dan `ErrorMessage`. `ErrorMessage` dipakai untuk error form-level atau submit-level, sedangkan error field ditampilkan melalui prop `error` pada `FormField`.

```tsx
"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import { ErrorMessage } from "@/app/components/error-message";
import { FormField } from "@/app/components/form-field";
import { entityFormSchema, type EntityFormInputValues, type EntityFormValues } from "@/features/entities/entity.schema";
import { ENTITY_STATUSES, type EntityResponse } from "@/features/entities/entity.types";

interface EntityFormProps {
  initialData?: EntityResponse | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  submitError?: string;
  onSubmit: (values: EntityFormValues) => void;
  onCancelEdit?: () => void;
}

const defaultValues: EntityFormInputValues = {
  code: "",
  name: "",
  description: "",
  status: "ACTIVE",
};

const inputClassName = "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 disabled:bg-zinc-100 disabled:text-zinc-500";

export default function EntityForm({
  initialData,
  canWrite,
  isSubmitting = false,
  submitError,
  onSubmit,
  onCancelEdit,
}: EntityFormProps) {
  const form = useForm<EntityFormInputValues, unknown, EntityFormValues>({
    resolver: zodResolver(entityFormSchema),
    defaultValues,
  });

  useEffect(() => {
    form.reset(
      initialData
        ? {
            code: initialData.code,
            name: initialData.name,
            description: initialData.description ?? "",
            status: initialData.status,
          }
        : defaultValues,
    );
  }, [form, initialData]);

  return (
    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
      {submitError ? <ErrorMessage message={submitError} /> : null}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <FormField label="Kode" required error={form.formState.errors.code?.message} htmlFor="entity-code">
          <input
            id="entity-code"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="ENT-001"
            {...form.register("code")}
          />
        </FormField>

        <FormField label="Nama" required error={form.formState.errors.name?.message} htmlFor="entity-name">
          <input
            id="entity-name"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Entity utama"
            {...form.register("name")}
          />
        </FormField>

        <FormField label="Deskripsi" error={form.formState.errors.description?.message} htmlFor="entity-description">
          <textarea
            id="entity-description"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Deskripsi singkat"
            {...form.register("description")}
          />
        </FormField>

        <FormField label="Status" required error={form.formState.errors.status?.message} htmlFor="entity-status">
          <select id="entity-status" className={inputClassName} disabled={!canWrite || isSubmitting} {...form.register("status")}>
            {ENTITY_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </FormField>
      </div>

      {canWrite ? (
        <div className="flex flex-wrap gap-2">
          <button
            type="submit"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Menyimpan..." : initialData ? "Update entity" : "Tambah entity"}
          </button>
          {initialData && onCancelEdit ? (
            <button
              type="button"
              className="rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700"
              onClick={onCancelEdit}
              disabled={isSubmitting}
            >
              Batal edit
            </button>
          ) : null}
        </div>
      ) : (
        <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          Mode read-only. Anda tidak memiliki permission untuk mengubah data.
        </p>
      )}
    </form>
  );
}
```

## Contoh Table

Table memakai `DataTable` agar loading, empty, dan error state konsisten. Gunakan `StatusBadge` untuk status dan `ConfirmActionButton` untuk delete action yang butuh konfirmasi.

```tsx
"use client";

import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { StatusBadge } from "@/app/components/status-badge";
import type { EntityResponse, EntityStatus } from "@/features/entities/entity.types";

interface EntityTableProps {
  items: EntityResponse[];
  canWrite?: boolean;
  isLoading?: boolean;
  error?: string;
  onEdit?: (item: EntityResponse) => void;
  onDelete?: (item: EntityResponse) => void;
}

const statusTone: Record<EntityStatus, "default" | "success"> = {
  ACTIVE: "success",
  INACTIVE: "default",
};

export default function EntityTable({
  items,
  canWrite = false,
  isLoading = false,
  error,
  onEdit,
  onDelete,
}: EntityTableProps) {
  const columns: DataTableColumn<EntityResponse>[] = [
    {
      key: "code",
      header: "Kode",
      render: (item) => <span className="font-medium text-zinc-900">{item.code}</span>,
    },
    {
      key: "name",
      header: "Nama",
      render: (item) => item.name,
    },
    {
      key: "status",
      header: "Status",
      render: (item) => <StatusBadge status={item.status} variant={statusTone[item.status]} />,
    },
  ];

  if (canWrite) {
    columns.push({
      key: "actions",
      header: "Aksi",
      render: (item) => (
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className="rounded-md border border-zinc-300 px-3 py-1 text-xs font-medium text-zinc-700 hover:bg-zinc-50"
            onClick={() => onEdit?.(item)}
          >
            Edit
          </button>
          <ConfirmActionButton
            className="px-3 py-1 text-xs"
            confirmMessage={`Entity ${item.name} akan dihapus.`}
            label="Hapus"
            onConfirm={() => onDelete?.(item)}
            variant="danger"
          />
        </div>
      ),
    });
  }

  return (
    <DataTable
      columns={columns}
      data={items}
      error={error}
      getRowKey={(item) => item.id}
      isLoading={isLoading}
      emptyTitle="Belum ada entity"
      emptyDescription="Tambahkan data entity baru untuk mulai."
    />
  );
}
```

## Permission

Gunakan `RequirePermission` untuk guard page atau blok fitur. Jika user boleh masuk ketika punya salah satu permission, pakai `mode="any"`.

```tsx
import RequirePermission from "@/app/components/require-permission";

export default function ProtectedModulePage() {
  return (
    <RequirePermission permissions={["MODULE_READ", "MODULE_WRITE"]} mode="any">
      {/* konten modul */}
    </RequirePermission>
  );
}
```

Rekomendasi:

- Page list biasanya butuh `MODULE_READ`.
- Tombol create/update/delete dan form submit dicek dengan `MODULE_WRITE`.
- Jangan hanya menyembunyikan tombol; tetap disable form/action ketika `canWrite` bernilai `false`.

## Checklist Sebelum Merge

- [ ] typecheck sukses
- [ ] lint sukses
- [ ] build sukses
- [ ] loading state ada
- [ ] empty state ada
- [ ] error state ada
- [ ] permission dicek
- [ ] form validation ada
- [ ] mutation invalidate query
- [ ] mobile responsive
