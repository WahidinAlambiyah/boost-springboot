"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { AcademyLocation, ClassSession } from "@/lib/api-types";
import { classSessionFormSchema, ClassSessionFormValues } from "@/features/class-sessions/class-session.schema";

interface ClassSessionFormProps {
  classGroupId: string;
  locations: AcademyLocation[];
  canWrite: boolean;
  initialData?: ClassSession | null;
  isSubmitting?: boolean;
  onSubmit: (values: ClassSessionFormValues & { classGroupId: string }) => void;
  onConflictCheck?: (values: Pick<ClassSessionFormValues, "academyId" | "sessionDate" | "startTime" | "endTime" | "locationId">) => void;
}

export default function ClassSessionForm({ classGroupId, locations, canWrite, initialData, isSubmitting = false, onSubmit, onConflictCheck }: ClassSessionFormProps) {
  const form = useForm<ClassSessionFormValues>({
    resolver: zodResolver(classSessionFormSchema),
    defaultValues: { academyId: "", locationId: "", sessionDate: "", startTime: "", endTime: "", notes: "", status: "SCHEDULED" },
  });

  useEffect(() => {
    form.reset({
      academyId: initialData?.academyId ?? "",
      locationId: initialData?.locationId ?? "",
      sessionDate: initialData?.sessionDate ?? "",
      startTime: initialData?.startTime ?? "",
      endTime: initialData?.endTime ?? "",
      notes: "",
      status: initialData?.status ?? "SCHEDULED",
    });
  }, [form, initialData]);

  const academyId = form.watch("academyId");
  const locationId = form.watch("locationId");
  const sessionDate = form.watch("sessionDate");
  const startTime = form.watch("startTime");
  const endTime = form.watch("endTime");

  useEffect(() => {
    if (!onConflictCheck || !academyId || !locationId || !sessionDate || !startTime || !endTime) {
      return;
    }

    const timeoutId = window.setTimeout(() => {
      onConflictCheck({ academyId, locationId, sessionDate, startTime, endTime });
    }, 450);

    return () => window.clearTimeout(timeoutId);
  }, [academyId, locationId, sessionDate, startTime, endTime, onConflictCheck]);

  return <form className="space-y-3" onSubmit={form.handleSubmit((values) => onSubmit({ ...values, classGroupId }))}>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Academy ID</span><input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("academyId")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Lokasi</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("locationId")}><option value="">Pilih lokasi</option>{locations.map((location) => <option key={location.id} value={location.id}>{location.name}</option>)}</select></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Tanggal</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("sessionDate")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Mulai</span><input type="time" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("startTime")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Selesai</span><input type="time" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("endTime")} /></label>
    </div>
    <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Status</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("status")}><option value="SCHEDULED">SCHEDULED</option><option value="CANCELLED">CANCELLED</option><option value="COMPLETED">COMPLETED</option></select></label>
    {canWrite ? <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : initialData ? "Update Session" : "Tambah Session"}</button> : null}
  </form>;
}
