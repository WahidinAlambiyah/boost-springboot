"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import { FormField } from "@/app/components/form-field";
import { devCrudFormSchema, type DevCrudFormInputValues, type DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { TRAINING_CENTER_DEMO_STATUSES, type TrainingCenterDemo } from "@/features/dev-crud/dev-crud.types";

interface DevCrudFormProps {
  initialData?: TrainingCenterDemo | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: DevCrudFormValues) => void;
  onCancelEdit?: () => void;
}

const defaultValues: DevCrudFormInputValues = {
  code: "",
  name: "",
  location: "",
  activeStudents: 0,
  coachCount: 0,
  status: "ACTIVE",
};

const inputClassName = "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 disabled:bg-zinc-100 disabled:text-zinc-500";

export default function DevCrudForm({
  initialData,
  canWrite,
  isSubmitting = false,
  onSubmit,
  onCancelEdit,
}: DevCrudFormProps) {
  const form = useForm<DevCrudFormInputValues, unknown, DevCrudFormValues>({
    resolver: zodResolver(devCrudFormSchema),
    defaultValues,
  });

  useEffect(() => {
    form.reset(
      initialData
        ? {
            code: initialData.code,
            name: initialData.name,
            location: initialData.location,
            activeStudents: initialData.activeStudents,
            coachCount: initialData.coachCount,
            status: initialData.status,
          }
        : defaultValues,
    );
  }, [form, initialData]);

  return (
    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <FormField label="Kode" required error={form.formState.errors.code?.message} htmlFor="dev-crud-code">
          <input
            id="dev-crud-code"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="BTC-JKT"
            {...form.register("code")}
          />
        </FormField>

        <FormField label="Nama Training Center" required error={form.formState.errors.name?.message} htmlFor="dev-crud-name">
          <input
            id="dev-crud-name"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Boost Training Center Jakarta"
            {...form.register("name")}
          />
        </FormField>

        <FormField label="Lokasi" required error={form.formState.errors.location?.message} htmlFor="dev-crud-location">
          <input
            id="dev-crud-location"
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            placeholder="Jakarta Selatan"
            {...form.register("location")}
          />
        </FormField>

        <FormField label="Murid Aktif" required error={form.formState.errors.activeStudents?.message} htmlFor="dev-crud-active-students">
          <input
            id="dev-crud-active-students"
            type="number"
            min={0}
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            {...form.register("activeStudents", { valueAsNumber: true })}
          />
        </FormField>

        <FormField label="Jumlah Coach" required error={form.formState.errors.coachCount?.message} htmlFor="dev-crud-coach-count">
          <input
            id="dev-crud-coach-count"
            type="number"
            min={0}
            className={inputClassName}
            disabled={!canWrite || isSubmitting}
            {...form.register("coachCount", { valueAsNumber: true })}
          />
        </FormField>

        <FormField label="Status" required error={form.formState.errors.status?.message} htmlFor="dev-crud-status">
          <select id="dev-crud-status" className={inputClassName} disabled={!canWrite || isSubmitting} {...form.register("status")}>
            {TRAINING_CENTER_DEMO_STATUSES.map((status) => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        </FormField>
      </div>

      {canWrite ? (
        <div className="flex flex-wrap gap-2">
          <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : initialData ? "Update training center" : "Tambah training center"}
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
