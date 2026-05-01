"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Academy } from "@/lib/api-types";
import { AcademyFormValues, academyFormSchema } from "@/features/academies/academy.schema";

interface AcademyFormProps {
  initialData?: Academy | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: AcademyFormValues) => void;
  onCancelEdit?: () => void;
}

export default function AcademyForm({
  initialData,
  canWrite,
  isSubmitting = false,
  onSubmit,
  onCancelEdit,
}: AcademyFormProps) {
  const form = useForm<AcademyFormValues>({
    resolver: zodResolver(academyFormSchema),
    defaultValues: {
      code: "",
      name: "",
      description: "",
      phone: "",
      email: "",
      isActive: true,
    },
  });

  useEffect(() => {
    form.reset({
      code: initialData?.code ?? "",
      name: initialData?.name ?? "",
      description: initialData?.description ?? "",
      phone: initialData?.phone ?? "",
      email: initialData?.email ?? "",
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  return (
    <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Code</span>
          <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("code")} />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.code?.message}</p>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Name</span>
          <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("name")} />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.name?.message}</p>
        </label>
      </div>

      <label className="block text-sm">
        <span className="mb-1 block font-medium text-zinc-700">Description</span>
        <textarea rows={3} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("description")} />
        <p className="mt-1 text-xs text-red-600">{form.formState.errors.description?.message}</p>
      </label>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Phone</span>
          <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("phone")} />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.phone?.message}</p>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Email</span>
          <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("email")} />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.email?.message}</p>
        </label>
      </div>

      <label className="inline-flex items-center gap-2 text-sm text-zinc-700">
        <input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />
        Active
      </label>

      {canWrite ? (
        <div className="flex gap-2">
          <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : initialData ? "Update Academy" : "Tambah Academy"}
          </button>
          {initialData && onCancelEdit ? (
            <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>
              Batal Edit
            </button>
          ) : null}
        </div>
      ) : null}
    </form>
  );
}
