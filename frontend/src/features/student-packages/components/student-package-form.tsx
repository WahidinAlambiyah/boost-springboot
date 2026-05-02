"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { TrainingPackage } from "@/lib/api-types";
import {
  studentPackageFormSchema,
  StudentPackageFormSubmitValues,
  StudentPackageFormValues,
  studentPackageStatusOptions,
} from "@/features/student-packages/student-package.schema";
import { StudentPackage } from "@/features/student-packages/student-package.service";

interface StudentPackageFormProps {
  packages: TrainingPackage[];
  initialData?: StudentPackage | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: StudentPackageFormSubmitValues) => void;
  onCancelEdit?: () => void;
}

export default function StudentPackageForm({ packages, initialData, canWrite, isSubmitting = false, onSubmit, onCancelEdit }: StudentPackageFormProps) {
  const form = useForm<StudentPackageFormValues>({
    resolver: zodResolver(studentPackageFormSchema),
    defaultValues: { trainingPackageId: "", startDate: "", endDate: "", remainingSessions: 0, status: "ACTIVE" },
  });

  useEffect(() => {
    form.reset({
      trainingPackageId: initialData?.trainingPackageId ?? "",
      startDate: initialData?.startDate ? initialData.startDate.slice(0, 10) : "",
      endDate: initialData?.endDate ? initialData.endDate.slice(0, 10) : "",
      remainingSessions: initialData?.remainingSessions ?? 0,
      status: initialData?.status ?? "ACTIVE",
    });
  }, [form, initialData]);

  return <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Package</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("trainingPackageId")}><option value="">Pilih package</option>{packages.map((pkg) => <option key={pkg.id} value={pkg.id}>{pkg.name}</option>)}</select></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Status</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("status")}>{studentPackageStatusOptions.map((status) => <option key={status} value={status}>{status}</option>)}</select></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Start Date</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("startDate")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">End Date</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("endDate")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Remaining Sessions</span><input type="number" min={0} className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("remainingSessions")} /></label>
    </div>
    {canWrite ? <div className="flex gap-2"><button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : initialData ? "Update Subscription" : "Assign Package"}</button>{initialData && onCancelEdit ? <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>Batal Edit</button> : null}</div> : null}
  </form>;
}
