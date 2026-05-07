# CRUD Module Pattern

Panduan ini adalah standar membuat modul CRUD di frontend. Tujuannya agar struktur file, kontrak data, validasi, React Query, tabel, dan permission konsisten di semua fitur admin/protected.

## Struktur folder module CRUD

Gunakan satu folder domain di `src/features/{module-name}` dan satu route protected di `src/app/(protected)/{route}`.

```text
src/features/{module-name}/
├── {module}.types.ts
├── {module}.schema.ts
├── {module}.service.ts
└── components/
    ├── {module}-form.tsx
    └── {module}-table.tsx

src/app/(protected)/{route}/page.tsx
```

Contoh untuk modul `training-centers`:

```text
src/features/training-centers/
├── training-center.types.ts
├── training-center.schema.ts
├── training-center.service.ts
└── components/
    ├── training-center-form.tsx
    └── training-center-table.tsx

src/app/(protected)/training-centers/page.tsx
```

Aturan penempatan:

- `*.types.ts` berisi tipe response, request, enum/union, dan filter list.
- `*.schema.ts` berisi Zod schema dan type form dari schema.
- `*.service.ts` menjadi satu pintu akses data ke API, adapter, atau mock.
- `components/*-form.tsx` fokus pada input form dan validasi.
- `components/*-table.tsx` fokus pada rendering list, status, dan action row.
- `page.tsx` menjadi orchestration layer untuk query, mutation, permission, search/filter, selected row, dan layout.

## Contoh types

Buat tipe yang eksplisit untuk domain modul. Hindari tipe generik seperti `any`, `Data`, atau `Payload` tanpa konteks domain.

```ts
export const TRAINING_CENTER_STATUSES = ["ACTIVE", "INACTIVE"] as const;

export type TrainingCenterStatus = (typeof TRAINING_CENTER_STATUSES)[number];

export interface TrainingCenterResponse {
  id: string;
  code: string;
  name: string;
  address: string;
  phone?: string;
  status: TrainingCenterStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTrainingCenterRequest {
  code: string;
  name: string;
  address: string;
  phone?: string;
  status: TrainingCenterStatus;
}

export type UpdateTrainingCenterRequest = Partial<CreateTrainingCenterRequest>;

export interface TrainingCenterListParams {
  search?: string;
  status?: TrainingCenterStatus | "ALL";
}
```

Checklist types:

- Response mencerminkan data dari service/API setelah normalisasi.
- Create request hanya memuat field yang dikirim saat create.
- Update request memakai tipe terpisah jika aturan update berbeda dari create.
- List params stabil karena dipakai sebagai bagian dari React Query key.

## Contoh service

Service harus menyembunyikan detail transport dari UI. Page dan component cukup memanggil method service.

```ts
import type {
  CreateTrainingCenterRequest,
  TrainingCenterListParams,
  TrainingCenterResponse,
  UpdateTrainingCenterRequest,
} from "./training-center.types";

export const trainingCenterService = {
  async list(params?: TrainingCenterListParams): Promise<TrainingCenterResponse[]> {
    // return apiClient.get("/training-centers", { params });
    throw new Error(`Implement trainingCenterService.list with ${JSON.stringify(params ?? {})}`);
  },

  async getById(id: string): Promise<TrainingCenterResponse> {
    // return apiClient.get(`/training-centers/${id}`);
    throw new Error(`Implement trainingCenterService.getById for ${id}`);
  },

  async create(payload: CreateTrainingCenterRequest): Promise<TrainingCenterResponse> {
    // return apiClient.post("/training-centers", payload);
    throw new Error(`Implement trainingCenterService.create for ${payload.code}`);
  },

  async update(id: string, payload: UpdateTrainingCenterRequest): Promise<TrainingCenterResponse> {
    // return apiClient.patch(`/training-centers/${id}`, payload);
    throw new Error(`Implement trainingCenterService.update for ${id} with ${JSON.stringify(payload)}`);
  },

  async remove(id: string): Promise<void> {
    // return apiClient.delete(`/training-centers/${id}`);
    throw new Error(`Implement trainingCenterService.remove for ${id}`);
  },
};
```

Standar method minimal:

- `list(params?)`
- `getById(id)` jika fitur membutuhkan detail/edit by id
- `create(payload)`
- `update(id, payload)`
- `remove(id)`

Jika backend belum tersedia, service boleh memakai mock lokal, tetapi kontrak method dan tipe return tetap sama agar mudah diganti ke API sungguhan.

## Contoh React Query

Tambahkan key module ke `QUERY_KEYS` terlebih dahulu:

```ts
trainingCenters: createEntityKeys<{ search?: string; status?: string }>("trainingCenters"),
```

Contoh orchestration di `src/app/(protected)/training-centers/page.tsx`:

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
import TrainingCenterForm from "@/features/training-centers/components/training-center-form";
import TrainingCenterTable from "@/features/training-centers/components/training-center-table";
import type { TrainingCenterFormValues } from "@/features/training-centers/training-center.schema";
import { trainingCenterService } from "@/features/training-centers/training-center.service";
import type { TrainingCenterResponse } from "@/features/training-centers/training-center.types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function TrainingCentersPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = can(authorities, "TRAINING_CENTER_WRITE");
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<TrainingCenterResponse | null>(null);
  const [mutationError, setMutationError] = useState<string>();

  const listQuery = useQuery({
    queryKey: QUERY_KEYS.trainingCenters.filter({ search: search || undefined }),
    queryFn: () => trainingCenterService.list({ search: search || undefined }),
  });

  const invalidateTrainingCenters = async () => {
    await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.trainingCenters.all });
  };

  const createMutation = useMutation({
    mutationFn: trainingCenterService.create,
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      setSelected(null);
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Data gagal dibuat."),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: TrainingCenterFormValues }) =>
      trainingCenterService.update(id, payload),
    onMutate: () => setMutationError(undefined),
    onSuccess: async () => {
      setSelected(null);
      await invalidateTrainingCenters();
    },
    onError: () => setMutationError("Data gagal diperbarui."),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => trainingCenterService.remove(id),
    onMutate: () => setMutationError(undefined),
    onSuccess: invalidateTrainingCenters,
    onError: () => setMutationError("Data gagal dihapus."),
  });

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  const formInitialValues = useMemo(
    () =>
      selected
        ? {
            code: selected.code,
            name: selected.name,
            address: selected.address,
            phone: selected.phone ?? "",
            status: selected.status,
          }
        : undefined,
    [selected],
  );

  const handleSubmit = (values: TrainingCenterFormValues) => {
    if (selected) {
      updateMutation.mutate({ id: selected.id, payload: values });
      return;
    }

    createMutation.mutate(values);
  };

  return (
    <RequirePermission permissions="TRAINING_CENTER_READ">
      <AppShell>
        <PageHeader
          title="Training Centers"
          description="Kelola lokasi training center yang tersedia."
        />

        {mutationError ? <ErrorMessage message={mutationError} /> : null}

        <SectionCard title="Filter" description="Cari data berdasarkan kode atau nama.">
          <SearchInput value={search} onChange={setSearch} placeholder="Cari training center..." />
        </SectionCard>

        <SectionCard title={selected ? "Edit training center" : "Tambah training center"}>
          <RequirePermission permissions="TRAINING_CENTER_WRITE">
            <TrainingCenterForm
              initialValues={formInitialValues}
              isSubmitting={isSubmitting}
              onCancel={() => setSelected(null)}
              onSubmit={handleSubmit}
            />
          </RequirePermission>
        </SectionCard>

        <SectionCard title="Daftar training center">
          <TrainingCenterTable
            data={listQuery.data ?? []}
            error={listQuery.error ? "Data gagal dimuat." : undefined}
            isLoading={listQuery.isLoading}
            canWrite={canWrite}
            onDelete={(row) => deleteMutation.mutate(row.id)}
            onEdit={setSelected}
          />
        </SectionCard>
      </AppShell>
    </RequirePermission>
  );
}
```

Catatan React Query:

- Query key harus mencakup filter yang memengaruhi hasil list.
- Mutation harus invalidate query list/detail yang terdampak.
- Jangan panggil service langsung di component table/form kecuali untuk kebutuhan lokal yang sangat spesifik.
- Simpan error mutation di page agar pesan error tidak tersebar di banyak component.

## Contoh form React Hook Form + Zod

Definisikan schema di `{module}.schema.ts`:

```ts
import { z } from "zod";

import { TRAINING_CENTER_STATUSES } from "./training-center.types";

export const trainingCenterFormSchema = z.object({
  code: z.string().trim().min(2, "Kode minimal 2 karakter").max(32, "Kode maksimal 32 karakter"),
  name: z.string().trim().min(3, "Nama minimal 3 karakter"),
  address: z.string().trim().min(5, "Alamat minimal 5 karakter"),
  phone: z.string().trim().optional().or(z.literal("")),
  status: z.enum(TRAINING_CENTER_STATUSES, { message: "Status wajib dipilih" }),
});

export type TrainingCenterFormInputValues = z.input<typeof trainingCenterFormSchema>;
export type TrainingCenterFormValues = z.output<typeof trainingCenterFormSchema>;
```

Contoh form di `components/training-center-form.tsx`:

```tsx
"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import {
  trainingCenterFormSchema,
  type TrainingCenterFormInputValues,
  type TrainingCenterFormValues,
} from "../training-center.schema";

const defaultValues: TrainingCenterFormInputValues = {
  code: "",
  name: "",
  address: "",
  phone: "",
  status: "ACTIVE",
};

type TrainingCenterFormProps = {
  initialValues?: TrainingCenterFormInputValues;
  isSubmitting?: boolean;
  onCancel?: () => void;
  onSubmit: (values: TrainingCenterFormValues) => void;
};

export default function TrainingCenterForm({
  initialValues,
  isSubmitting = false,
  onCancel,
  onSubmit,
}: TrainingCenterFormProps) {
  const form = useForm<TrainingCenterFormInputValues, unknown, TrainingCenterFormValues>({
    resolver: zodResolver(trainingCenterFormSchema),
    defaultValues,
  });

  useEffect(() => {
    form.reset(initialValues ?? defaultValues);
  }, [form, initialValues]);

  return (
    <form className="grid gap-4 md:grid-cols-2" onSubmit={form.handleSubmit(onSubmit)}>
      <label className="grid gap-2 text-sm font-medium">
        Kode
        <input className="rounded border px-3 py-2" {...form.register("code")} />
        {form.formState.errors.code ? (
          <span className="text-sm text-red-600">{form.formState.errors.code.message}</span>
        ) : null}
      </label>

      <label className="grid gap-2 text-sm font-medium">
        Nama
        <input className="rounded border px-3 py-2" {...form.register("name")} />
        {form.formState.errors.name ? (
          <span className="text-sm text-red-600">{form.formState.errors.name.message}</span>
        ) : null}
      </label>

      <label className="grid gap-2 text-sm font-medium md:col-span-2">
        Alamat
        <textarea className="rounded border px-3 py-2" rows={3} {...form.register("address")} />
        {form.formState.errors.address ? (
          <span className="text-sm text-red-600">{form.formState.errors.address.message}</span>
        ) : null}
      </label>

      <label className="grid gap-2 text-sm font-medium">
        Telepon
        <input className="rounded border px-3 py-2" {...form.register("phone")} />
      </label>

      <label className="grid gap-2 text-sm font-medium">
        Status
        <select className="rounded border px-3 py-2" {...form.register("status")}>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </label>

      <div className="flex gap-2 md:col-span-2">
        <button className="rounded bg-zinc-900 px-4 py-2 text-white" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Menyimpan..." : "Simpan"}
        </button>
        {onCancel ? (
          <button className="rounded border px-4 py-2" type="button" onClick={onCancel}>
            Batal
          </button>
        ) : null}
      </div>
    </form>
  );
}
```

Aturan form:

- Gunakan `zodResolver(schema)` sebagai resolver React Hook Form.
- Pisahkan `InputValues` dan `Values` jika schema melakukan transform/coerce.
- Reset form saat `initialValues` berubah agar mode edit menampilkan data terpilih.
- Disable tombol submit saat mutation pending.

## Contoh table dengan DataTable

Gunakan `DataTable` agar loading, empty, error, horizontal scroll, dan action column konsisten.

```tsx
"use client";

import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { StatusBadge } from "@/app/components/status-badge";

import type { TrainingCenterResponse } from "../training-center.types";

type TrainingCenterTableProps = {
  data: TrainingCenterResponse[];
  error?: string;
  isLoading?: boolean;
  canWrite?: boolean;
  onDelete: (row: TrainingCenterResponse) => void;
  onEdit: (row: TrainingCenterResponse) => void;
};

const columns: DataTableColumn<TrainingCenterResponse>[] = [
  {
    key: "code",
    header: "Kode",
    render: (row) => row.code,
  },
  {
    key: "name",
    header: "Nama",
    render: (row) => row.name,
  },
  {
    key: "address",
    header: "Alamat",
    render: (row) => row.address,
  },
  {
    key: "status",
    header: "Status",
    render: (row) => <StatusBadge status={row.status} />,
  },
];

export default function TrainingCenterTable({
  data,
  error,
  isLoading = false,
  canWrite = false,
  onDelete,
  onEdit,
}: TrainingCenterTableProps) {
  return (
    <DataTable
      ariaLabel="Daftar training center"
      columns={columns}
      data={data}
      emptyTitle="Belum ada training center"
      emptyDescription="Tambahkan training center pertama untuk mulai mengelola lokasi."
      error={error}
      getRowKey={(row) => row.id}
      isLoading={isLoading}
      actionColumn={
        canWrite
          ? {
              render: (row) => (
                <div className="flex justify-end gap-2">
                  <button className="rounded border px-3 py-1" type="button" onClick={() => onEdit(row)}>
                    Edit
                  </button>
                  <button className="rounded border border-red-300 px-3 py-1 text-red-700" type="button" onClick={() => onDelete(row)}>
                    Hapus
                  </button>
                </div>
              ),
            }
          : undefined
      }
    />
  );
}
```

Aturan table:

- Column config ditulis dengan `DataTableColumn<T>[]` agar type-safe.
- Gunakan `getRowKey` berbasis id stabil.
- Action edit/delete dikirim dari page melalui callback.
- Sembunyikan action column jika user tidak memiliki permission write.
- Untuk delete produksi, gunakan komponen konfirmasi jika tersedia pada modul tersebut.

## Contoh permission

Gunakan `RequirePermission` untuk guard halaman atau section. Gunakan helper `can` untuk kondisi render action.

```tsx
import RequirePermission from "@/app/components/require-permission";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

export default function ExamplePermissionBlock() {
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = can(authorities, "TRAINING_CENTER_WRITE");

  return (
    <RequirePermission permissions="TRAINING_CENTER_READ">
      <RequirePermission permissions="TRAINING_CENTER_WRITE">
        <button type="button">Tambah data</button>
      </RequirePermission>

      {canWrite ? <button type="button">Edit row</button> : null}
    </RequirePermission>
  );
}
```

Jika halaman bisa dibuka oleh salah satu dari beberapa permission, gunakan `mode="any"`:

```tsx
<RequirePermission permissions={["TRAINING_CENTER_READ", "TRAINING_CENTER_WRITE"]} mode="any">
  <TrainingCentersPageContent />
</RequirePermission>
```

Standar permission:

- Page list/detail minimal diguard dengan permission read.
- Form create/edit dan action delete diguard dengan permission write.
- Jangan hanya menyembunyikan tombol; endpoint/backend tetap harus enforce authorization.
- Nama permission mengikuti authority backend yang tersedia.

## Checklist sebelum merge

Sebelum membuka PR/merge modul CRUD baru, pastikan:

- [ ] Struktur folder mengikuti `src/features/{module-name}` dan route protected.
- [ ] Types response/request/filter sudah eksplisit dan tidak memakai `any`.
- [ ] Zod schema mencakup field wajib, format, min/max, dan pesan validasi user-friendly.
- [ ] Form memakai React Hook Form + `zodResolver` dan reset saat `initialValues` berubah.
- [ ] Service menjadi satu pintu akses data dan tidak ada call API langsung dari table/form.
- [ ] `QUERY_KEYS` sudah ditambahkan dan query key memasukkan filter yang memengaruhi list.
- [ ] Mutation create/update/delete melakukan invalidate query yang terdampak.
- [ ] Page memakai `RequirePermission` untuk read dan write.
- [ ] Action table disembunyikan atau disabled untuk user tanpa permission write.
- [ ] Table memakai `DataTable` dengan loading, error, empty state, dan `getRowKey`.
- [ ] Delete action memakai konfirmasi jika operasi tidak bisa dibatalkan.
- [ ] Empty state dan error message jelas dalam Bahasa Indonesia.
- [ ] Unit/integration/e2e test relevan sudah ditambahkan atau diperbarui jika ada logic baru.
- [ ] `npm run lint`, `npm run test`, atau check lain yang relevan sudah dijalankan sesuai perubahan.
