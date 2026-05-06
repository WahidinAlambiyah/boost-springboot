"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Academy, CoachProfile } from "@/lib/api-types";
import {
  CoachProfileFormSubmitValues,
  CoachProfileFormValues,
  coachProfileFormSchema,
} from "@/features/coach-profiles/coach-profile.schema";
import { UserOption } from "@/features/coach-profiles/coach-profile.service";

interface CoachProfileFormProps {
  academies: Academy[];
  users: UserOption[];
  initialData?: CoachProfile | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: CoachProfileFormSubmitValues) => void;
  onCancelEdit?: () => void;
}

export default function CoachProfileForm({ academies, users, initialData, canWrite, isSubmitting = false, onSubmit, onCancelEdit }: CoachProfileFormProps) {
  const form = useForm<CoachProfileFormValues, unknown, CoachProfileFormSubmitValues>({
    resolver: zodResolver(coachProfileFormSchema),
    defaultValues: { academyId: "", userId: "", phone: "", employmentType: "FULL_TIME", payType: "SALARY", monthlySalary: null, ratePerSession: null, ratePerStudent: null, isActive: true },
  });

  useEffect(() => {
    form.reset({ academyId: initialData?.academyId ?? "", userId: initialData?.userId ?? "", phone: initialData?.phone ?? "", employmentType: initialData?.employmentType ?? "FULL_TIME", payType: initialData?.payType ?? "SALARY", monthlySalary: null, ratePerSession: null, ratePerStudent: null, isActive: initialData?.isActive ?? true });
  }, [form, initialData]);

  return <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Academy</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("academyId")}><option value="">Pilih academy</option>{academies.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">User</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("userId")}><option value="">Pilih user</option>{users.map((user) => <option key={user.id} value={user.id}>{user.username ?? user.email ?? user.id}</option>)}</select></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Phone</span><input className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("phone")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Employment Type</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("employmentType")}><option value="FULL_TIME">FULL_TIME</option><option value="PART_TIME">PART_TIME</option><option value="CONTRACT">CONTRACT</option></select></label>
    </div>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Pay Type</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("payType")}><option value="SALARY">SALARY</option><option value="PER_SESSION">PER_SESSION</option><option value="HOURLY">HOURLY</option></select></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Monthly Salary</span><input type="number" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("monthlySalary")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Rate Per Session</span><input type="number" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("ratePerSession")} /></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Rate Per Student</span><input type="number" className="w-full rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("ratePerStudent")} /></label>
    </div>
    <label className="inline-flex items-center gap-2 text-sm text-zinc-700"><input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />Active</label>
    {canWrite ? <div className="flex gap-2"><button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50" disabled={isSubmitting}>{isSubmitting ? "Menyimpan..." : initialData ? "Update Coach" : "Tambah Coach"}</button>{initialData && onCancelEdit ? <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>Batal Edit</button> : null}</div> : null}
  </form>;
}
