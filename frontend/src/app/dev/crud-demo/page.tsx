"use client";

import { useEffect, useMemo, useState } from "react";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import DevCrudForm from "@/features/dev-crud/components/dev-crud-form";
import DevCrudTable from "@/features/dev-crud/components/dev-crud-table";
import type { DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { devCrudService } from "@/features/dev-crud/dev-crud.service";
import { DEV_TOOLS_READ_PERMISSIONS, DEV_TOOLS_WRITE_PERMISSIONS, type DevCrudItem } from "@/features/dev-crud/dev-crud.types";
import { canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

export default function DevCrudDemoPage() {
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => canAny(authorities, [...DEV_TOOLS_WRITE_PERMISSIONS]), [authorities]);
  const [items, setItems] = useState<DevCrudItem[]>([]);
  const [selected, setSelected] = useState<DevCrudItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string>();

  const loadItems = async () => {
    setIsLoading(true);
    setError(undefined);

    try {
      setItems(await devCrudService.list());
    } catch {
      setError("Gagal memuat data dev CRUD.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    let isMounted = true;

    const loadInitialItems = async () => {
      try {
        const data = await devCrudService.list();

        if (isMounted) {
          setItems(data);
        }
      } catch {
        if (isMounted) {
          setError("Gagal memuat data dev CRUD.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void loadInitialItems();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSubmit = async (values: DevCrudFormValues) => {
    setIsSubmitting(true);
    setError(undefined);

    try {
      if (selected) {
        await devCrudService.update(selected.id, values);
        setSelected(null);
      } else {
        await devCrudService.create(values);
      }

      await loadItems();
    } catch {
      setError("Aksi dev CRUD gagal diproses.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (item: DevCrudItem) => {
    setIsSubmitting(true);
    setError(undefined);

    try {
      await devCrudService.remove(item.id);
      if (selected?.id === item.id) {
        setSelected(null);
      }
      await loadItems();
    } catch {
      setError("Data dev CRUD gagal dihapus.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev CRUD Demo"
          description="Playground protected untuk mencoba pola create, read, update, dan delete berbasis data mock."
        />

        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
          <SectionCard title="Data Mock" description="Tabel menggunakan service lokal agar aman untuk eksplorasi developer.">
            <DevCrudTable
              items={items}
              canWrite={canWrite}
              error={error}
              isLoading={isLoading}
              onEdit={setSelected}
              onDelete={handleDelete}
            />
          </SectionCard>

          <SectionCard title={selected ? "Edit Data" : "Tambah Data"} description="Form memakai react-hook-form dan zod schema.">
            {error ? <ErrorMessage className="mb-4" message={error} /> : null}
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
