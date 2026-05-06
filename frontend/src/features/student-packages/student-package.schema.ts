import { z } from "zod";

export const studentPackageStatusOptions = ["ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"] as const;

export const studentPackageFormSchema = z.object({
  trainingPackageId: z.string().trim().min(1, "Package wajib dipilih"),
  startDate: z.string().trim().min(1, "Start date wajib diisi"),
  endDate: z.string().trim().optional().or(z.literal("")),
  remainingSessions: z.coerce.number().int().min(0, "Remaining sessions minimal 0"),
  status: z.enum(studentPackageStatusOptions),
});

export type StudentPackageFormValues = z.input<typeof studentPackageFormSchema>;
export type StudentPackageFormSubmitValues = z.output<typeof studentPackageFormSchema>;
