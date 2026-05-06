"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import { FormField } from "@/app/components/form-field";
import { devCrudFormSchema, type DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { DEV_CRUD_PRIORITIES, DEV_CRUD_STATUSES, type DevCrudItem } from "@/features/dev-crud/dev-crud.types";

interface DevCrudFormProps {
  initialData?: DevCrudItem | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: DevCrudFormValues) => void;
  onCancelEdit?: () => void;
}

const defaultValues: DevCrudFormValues = {
  title: "",
  owner: "",
  status: "DRAFT",
  priority: "MEDIUM",
  dueDate: "",
};

const inputClassName = "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 disabled:bg-zinc-100 disabled:text-zinc-500";

export default function DevCrudForm({
  initialData,
  canWrite,
  isSubmitting = false,
  onSubmit,
  onCancelEdit,
}: DevCrudFormProps) {
  const form = useForm<DevCrudFormValues>({
    resolver: zodResolver(devCrudFormSchema),
    defaultValues,
  });

  useEffect(() => {
    form.reset(
      initialData
        ? {
            title: initialData.title,
            owner: initialData.owner,
            status: initialData.status,
            priority: initialData.priority,
            dueDate: initialData.dueDate,
          }
        : defaultValues,
    );
  }, [form, initialData]);

  return (
    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <FormField label="Judul" required error={form.formState.errors.title?.message} htmlFor="dev-crud-title">
          <input
            id="dev-crud-title"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Nama pekerjaan demo"
            {...form.register("title")}
          />
        </FormField>

        <FormField label="Owner" required error={form.formState.errors.owner?.message} htmlFor="dev-crud-owner">
          <input
            id="dev-crud-owner"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Tim pemilik"
            {...form.register("owner")}
          />
        </FormField>

        <FormField label="Status" required error={form.formState.errors.status?.message} htmlFor="dev-crud-status">
          <select id="dev-crud-status" className={inputClassName} disabled={!canWrite || isSubmitting} {...form.register("status")}>
            {DEV_CRUD_STATUSES.map((status) => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        </FormField>

        <FormField label="Prioritas" required error={form.formState.errors.priority?.message} htmlFor="dev-crud-priority">
          <select id="dev-crud-priority" className={inputClassName} disabled={!canWrite || isSubmitting} {...form.register("priority")}>
            {DEV_CRUD_PRIORITIES.map((priority) => (
              <option key={priority} value={priority}>{priority}</option>
            ))}
          </select>
        </FormField>

        <FormField label="Jatuh Tempo" required error={form.formState.errors.dueDate?.message} htmlFor="dev-crud-due-date">
          <input
            id="dev-crud-due-date"
            type="date"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            {...form.register("dueDate")}
          />
        </FormField>
      </div>

      {canWrite ? (
        <div className="flex flex-wrap gap-2">
          <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60" disabled={isSubmitting}>
            {initialData ? "Update data" : "Tambah data"}
          </button>
          {initialData && onCancelEdit ? (
            <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700" onClick={onCancelEdit} disabled={isSubmitting}>
              Batal edit
            </button>
          ) : null}
        </div>
      ) : (
        <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          Mode read-only. Permission write dev tools belum tersedia untuk akun ini.
        </p>
      )}
    </form>
  );
}
