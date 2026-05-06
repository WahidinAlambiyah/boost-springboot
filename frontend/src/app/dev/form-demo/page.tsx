"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMemo, useState } from "react";
import type { FieldErrors } from "react-hook-form";
import { useForm } from "react-hook-form";
import { z } from "zod";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import { FormField } from "@/app/components/form-field";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import { DEV_TOOLS_WRITE_PERMISSIONS, TRAINING_CENTER_DEMO_STATUSES } from "@/features/dev-crud/dev-crud.types";
import { canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

const formDemoSchema = z.object({
  title: z.string().trim().min(3, "Judul minimal 3 karakter"),
  participantCount: z.coerce
    .number({ message: "Jumlah peserta wajib diisi" })
    .int("Jumlah peserta harus bilangan bulat")
    .min(1, "Jumlah peserta minimal 1")
    .max(200, "Jumlah peserta maksimal 200"),
  status: z.enum(TRAINING_CENTER_DEMO_STATUSES, { message: "Status wajib dipilih" }),
  scheduledDate: z.string().min(1, "Tanggal wajib diisi"),
  notes: z.string().trim().min(10, "Catatan minimal 10 karakter").max(500, "Catatan maksimal 500 karakter"),
  confirmed: z.boolean().refine((value) => value, "Konfirmasi wajib dicentang"),
});

type FormDemoInputValues = z.input<typeof formDemoSchema>;
type FormDemoValues = z.output<typeof formDemoSchema>;

const defaultValues: FormDemoInputValues = {
  title: "",
  participantCount: 1,
  status: "ACTIVE",
  scheduledDate: "",
  notes: "",
  confirmed: false,
};

const inputClassName = "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 disabled:bg-zinc-100 disabled:text-zinc-500";

export default function DevFormDemoPage() {
  const authorities = useAuthStore((state) => state.authorities);
  // TODO: replace dummy create/update/delete checks with DEV_TOOLS_WRITE when dev tools write permission is available.
  const canWrite = useMemo(() => canAny(authorities, [...DEV_TOOLS_WRITE_PERMISSIONS]), [authorities]);
  const [lastSubmit, setLastSubmit] = useState<FormDemoValues | null>(null);
  const [submitFeedback, setSubmitFeedback] = useState<string | null>(null);

  const form = useForm<FormDemoInputValues, unknown, FormDemoValues>({
    resolver: zodResolver(formDemoSchema),
    defaultValues,
  });

  const handleValidSubmit = (values: FormDemoValues) => {
    setSubmitFeedback(null);
    setLastSubmit(values);
  };

  const handleInvalidSubmit = (errors: FieldErrors<FormDemoInputValues>) => {
    setLastSubmit(null);
    const fieldCount = Object.keys(errors).length;
    setSubmitFeedback(`Form belum valid. Periksa ${fieldCount} field yang masih bermasalah sebelum submit ulang.`);
  };

  return (
    // TODO: replace USER_READ with DEV_TOOLS_READ when dev tools permission is available.
    <RequirePermission permissions="USER_READ">
      <AppShell>
        <PageHeader
          title="Dev Form Demo"
          description="Halaman protected untuk mengecek React Hook Form, Zod validation, field wrapper, dan preview payload tanpa backend."
        />

        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
          <SectionCard title="Form Playground" description="Submit tidak memanggil API; payload ditampilkan di panel preview.">
            <form className="space-y-4" onSubmit={form.handleSubmit(handleValidSubmit, handleInvalidSubmit)} noValidate>
              {submitFeedback ? <ErrorMessage title="Submit gagal" message={submitFeedback} /> : null}

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <FormField label="Judul demo" required error={form.formState.errors.title?.message} htmlFor="form-demo-title">
                  <input
                    id="form-demo-title"
                    className={inputClassName}
                    disabled={!canWrite || form.formState.isSubmitting}
                    placeholder="Contoh form demo"
                    {...form.register("title")}
                  />
                </FormField>

                <FormField label="Jumlah peserta" required error={form.formState.errors.participantCount?.message} htmlFor="form-demo-participant-count">
                  <input
                    id="form-demo-participant-count"
                    type="number"
                    min={1}
                    max={200}
                    className={inputClassName}
                    disabled={!canWrite || form.formState.isSubmitting}
                    {...form.register("participantCount")}
                  />
                </FormField>

                <FormField label="Status" required error={form.formState.errors.status?.message} htmlFor="form-demo-status">
                  <select id="form-demo-status" className={inputClassName} disabled={!canWrite || form.formState.isSubmitting} {...form.register("status")}>
                    {TRAINING_CENTER_DEMO_STATUSES.map((status) => (
                      <option key={status} value={status}>{status}</option>
                    ))}
                  </select>
                </FormField>

                <FormField label="Tanggal" required error={form.formState.errors.scheduledDate?.message} htmlFor="form-demo-scheduled-date">
                  <input
                    id="form-demo-scheduled-date"
                    type="date"
                    className={inputClassName}
                    disabled={!canWrite || form.formState.isSubmitting}
                    {...form.register("scheduledDate")}
                  />
                </FormField>

                <FormField label="Catatan" required error={form.formState.errors.notes?.message} htmlFor="form-demo-notes" className="md:col-span-2">
                  <textarea
                    id="form-demo-notes"
                    className={`${inputClassName} min-h-28 resize-y`}
                    disabled={!canWrite || form.formState.isSubmitting}
                    placeholder="Tuliskan catatan demo minimal 10 karakter."
                    {...form.register("notes")}
                  />
                </FormField>

                <FormField label="Konfirmasi" required error={form.formState.errors.confirmed?.message} htmlFor="form-demo-confirmed" className="md:col-span-2">
                  <div className="flex items-start gap-3 rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-700">
                    <input
                      id="form-demo-confirmed"
                      type="checkbox"
                      className="mt-0.5 h-4 w-4 rounded border-zinc-300 text-zinc-900 focus:ring-zinc-900 disabled:opacity-60"
                      disabled={!canWrite || form.formState.isSubmitting}
                      {...form.register("confirmed")}
                    />
                    <span>Saya memahami bahwa submit hanya menampilkan preview JSON lokal dan tidak mengirim data ke backend.</span>
                  </div>
                </FormField>
              </div>

              {canWrite ? (
                <div className="flex flex-wrap gap-2">
                  <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60" disabled={form.formState.isSubmitting}>
                    Tampilkan preview JSON
                  </button>
                  <button
                    type="button"
                    className="rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 disabled:opacity-60"
                    disabled={form.formState.isSubmitting}
                    onClick={() => {
                      form.reset(defaultValues);
                      setLastSubmit(null);
                      setSubmitFeedback(null);
                    }}
                  >
                    Reset
                  </button>
                </div>
              ) : (
                <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
                  Mode read-only. Permission write dev tools belum tersedia untuk akun ini.
                </p>
              )}
            </form>
          </SectionCard>

          <SectionCard title="Payload Preview" description="Hasil submit terakhir dari form demo.">
            <pre className="overflow-auto rounded-md bg-zinc-950 p-4 text-xs text-zinc-100">
              {lastSubmit ? JSON.stringify(lastSubmit, null, 2) : "Belum ada submit."}
            </pre>
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
