"use client";
import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Student } from "@/lib/api-types";
import { StudentFormValues, studentFormSchema } from "@/features/students/student.schema";

interface StudentFormProps { initialData?: Student | null; canWrite: boolean; isSubmitting?: boolean; onSubmit: (values: StudentFormValues) => void; onCancelEdit?: () => void; }

export default function StudentForm({ initialData, canWrite, isSubmitting = false, onSubmit, onCancelEdit }: StudentFormProps) {
  const form = useForm<StudentFormValues>({ resolver: zodResolver(studentFormSchema), defaultValues: { studentNo: "", fullName: "", nickname: "", gender: "MALE", dateOfBirth: "", currentLevel: "", emergencyContactName: "", emergencyContactPhone: "", photoConsent: false, isActive: true } });
  useEffect(() => { form.reset({ studentNo: initialData?.studentNo ?? "", fullName: initialData?.fullName ?? "", nickname: initialData?.nickname ?? "", gender: "MALE", dateOfBirth: "", currentLevel: "", emergencyContactName: "", emergencyContactPhone: "", photoConsent: false, isActive: initialData?.status !== "INACTIVE" }); }, [form, initialData]);

  return (<form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="studentNo" disabled={!canWrite || isSubmitting || Boolean(initialData)} {...form.register("studentNo")} />
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="fullName" disabled={!canWrite || isSubmitting} {...form.register("fullName")} />
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="nickname" disabled={!canWrite || isSubmitting} {...form.register("nickname")} />
      <select className="rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("gender")}><option value="MALE">MALE</option><option value="FEMALE">FEMALE</option></select>
      <input type="date" className="rounded-md border border-zinc-300 px-3 py-2" disabled={!canWrite || isSubmitting} {...form.register("dateOfBirth")} />
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="currentLevel" disabled={!canWrite || isSubmitting} {...form.register("currentLevel")} />
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="emergencyContactName" disabled={!canWrite || isSubmitting} {...form.register("emergencyContactName")} />
      <input className="rounded-md border border-zinc-300 px-3 py-2" placeholder="emergencyContactPhone" disabled={!canWrite || isSubmitting} {...form.register("emergencyContactPhone")} />
    </div>
    <label className="inline-flex items-center gap-2"><input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("photoConsent")} />photoConsent</label>
    <label className="inline-flex items-center gap-2"><input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />isActive</label>
    {canWrite ? <div className="flex gap-2"><button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white" disabled={isSubmitting}>{initialData ? "Update Student" : "Tambah Student"}</button>{initialData && onCancelEdit ? <button type="button" className="rounded-md border border-zinc-300 px-4 py-2 text-sm" onClick={onCancelEdit}>Batal Edit</button> : null}</div> : null}
  </form>);
}
