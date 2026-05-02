import { z } from "zod";

const packageTypes = ["TRIAL", "PER_SESSION", "MONTHLY", "SESSION_BUNDLE"] as const;

export const trainingPackageFormSchema = z
  .object({
    academyId: z.string().trim().min(1, "Academy wajib dipilih"),
    code: z.string().trim().min(1, "Code wajib diisi"),
    name: z.string().trim().min(1, "Name wajib diisi"),
    packageType: z.enum(packageTypes, { message: "Package type wajib dipilih" }),
    price: z.coerce.number().min(0, "Price tidak boleh negatif"),
    sessionQuota: z.coerce.number().int().min(1, "Session quota minimal 1").nullable(),
    validityDays: z.coerce.number().int().min(1, "Validity days minimal 1").nullable(),
    description: z.string().trim().max(255, "Description maksimal 255 karakter").optional().or(z.literal("")),
    isActive: z.boolean().default(true),
  })
  .superRefine((value, ctx) => {
    if (value.packageType === "PER_SESSION" || value.packageType === "SESSION_BUNDLE") {
      if (value.sessionQuota == null) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["sessionQuota"], message: "Session quota wajib untuk tipe paket ini" });
      }
    }

    if (value.packageType === "TRIAL" || value.packageType === "MONTHLY") {
      if (value.validityDays == null) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, path: ["validityDays"], message: "Validity days wajib untuk tipe paket ini" });
      }
    }
  })
  .transform((value) => ({
    ...value,
    description: value.description?.trim() || undefined,
    sessionQuota: value.packageType === "PER_SESSION" || value.packageType === "SESSION_BUNDLE" ? value.sessionQuota : null,
    validityDays: value.packageType === "TRIAL" || value.packageType === "MONTHLY" ? value.validityDays : null,
  }));

export type TrainingPackageFormValues = z.input<typeof trainingPackageFormSchema>;
export type TrainingPackageFormSubmitValues = z.output<typeof trainingPackageFormSchema>;
