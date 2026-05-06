"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Academy, TrainingPackage } from "@/lib/api-types";
import { TrainingPackageFormSubmitValues, TrainingPackageFormValues, trainingPackageFormSchema } from "@/features/training-packages/training-package.schema";

interface TrainingPackageFormProps {
  academies: Academy[];
  initialData?: TrainingPackage | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: TrainingPackageFormSubmitValues) => void;
  onCancelEdit?: () => void;
}

export default function TrainingPackageForm({ academies, initialData, canWrite, isSubmitting = false, onSubmit, onCancelEdit }: TrainingPackageFormProps) {
  const form = useForm<TrainingPackageFormValues, unknown, TrainingPackageFormSubmitValues>({
    resolver: zodResolver(trainingPackageFormSchema),
    defaultValues: { academyId: "", code: "", name: "", packageType: "TRIAL", price: 0, sessionQuota: null, validityDays: null, description: "", isActive: true },
  });

  const packageType = form.watch("packageType");

  useEffect(() => {
    form.reset({ academyId: initialData?.academyId ?? "", code: initialData?.code ?? "", name: initialData?.name ?? "", packageType: initialData?.packageType ?? "TRIAL", price: initialData?.price ?? 0, sessionQuota: initialData?.sessionQuota ?? null, validityDays: null, description: initialData?.description ?? "", isActive: initialData?.isActive ?? true });
  }, [form, initialData]);

  return <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Academy</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("academyId")}><option value="">Pilih academy</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Code</span><input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("code")} /></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Name</span><input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("name")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Package Type</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("packageType")}><option value="TRIAL">TRIAL</option><option value="PER_SESSION">PER_SESSION</option><option value="MONTHLY">MONTHLY</option><option value="SESSION_BUNDLE">SESSION_BUNDLE</option></select></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Price</span><input type="number" min={0} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("price")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Session Quota</span><input type="number" min={1} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || !(packageType === "PER_SESSION" || packageType === "SESSION_BUNDLE")} {...form.register("sessionQuota")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Validity Days</span><input type="number" min={1} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || !(packageType === "TRIAL" || packageType === "MONTHLY")} {...form.register("validityDays")} /></label>
    </div>
    <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Description</span><textarea rows={3} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("description")} /></label>
    <label className="inline-flex items-center gap-2 text-sm text-zinc-700"><input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />Active</label>
    {canWrite ? <div className="flex gap-2"><button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : initialData ? "Update Paket" : "Tambah Paket"}</button>{initialData && onCancelEdit ? <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>Batal Edit</button> : null}</div> : null}
  </form>;
}
