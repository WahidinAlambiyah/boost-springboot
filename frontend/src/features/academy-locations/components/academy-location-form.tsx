"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Academy, AcademyLocation } from "@/lib/api-types";
import { AcademyLocationFormValues, academyLocationFormSchema } from "@/features/academy-locations/academy-location.schema";

interface AcademyLocationFormProps {
  academies: Academy[];
  initialData?: AcademyLocation | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: AcademyLocationFormValues) => void;
  onCancelEdit?: () => void;
}

export default function AcademyLocationForm({ academies, initialData, canWrite, isSubmitting = false, onSubmit, onCancelEdit }: AcademyLocationFormProps) {
  const form = useForm<AcademyLocationFormValues>({
    resolver: zodResolver(academyLocationFormSchema),
    defaultValues: { academyId: "", code: "", name: "", address: "", googleMapsUrl: "", isActive: true },
  });

  useEffect(() => {
    form.reset({
      academyId: initialData?.academyId ?? "",
      code: initialData?.code ?? "",
      name: initialData?.name ?? "",
      address: initialData?.address ?? "",
      googleMapsUrl: (initialData as AcademyLocation & { googleMapsUrl?: string })?.googleMapsUrl ?? "",
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  return <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>{/* form */}
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Academy</span>
        <select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("academyId")}>
          <option value="">Pilih academy</option>
          {academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}
        </select><p className="mt-1 text-xs text-red-600">{form.formState.errors.academyId?.message}</p></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Code</span>
        <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("code")} />
        <p className="mt-1 text-xs text-red-600">{form.formState.errors.code?.message}</p></label>
    </div>
    <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Name</span>
      <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("name")} />
      <p className="mt-1 text-xs text-red-600">{form.formState.errors.name?.message}</p></label>
    <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Address</span>
      <textarea rows={3} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("address")} />
      <p className="mt-1 text-xs text-red-600">{form.formState.errors.address?.message}</p></label>
    <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Google Maps URL</span>
      <input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("googleMapsUrl")} />
      <p className="mt-1 text-xs text-red-600">{form.formState.errors.googleMapsUrl?.message}</p></label>
    <label className="inline-flex items-center gap-2 text-sm text-zinc-700"><input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />Active</label>
    {canWrite ? <div className="flex gap-2"><button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : initialData ? "Update Location" : "Tambah Location"}</button>{initialData && onCancelEdit ? <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>Batal Edit</button> : null}</div> : null}
  </form>;
}
