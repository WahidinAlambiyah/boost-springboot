import { z } from "zod";

export const coachProfileFormSchema = z
  .object({
    academyId: z.string().trim().min(1, "Academy wajib dipilih"),
    userId: z.string().trim().min(1, "User wajib dipilih"),
    phone: z.string().trim().max(30, "Phone maksimal 30 karakter").optional().or(z.literal("")),
    employmentType: z.enum(["FULL_TIME", "PART_TIME", "CONTRACT"], {
      message: "Employment type wajib dipilih",
    }),
    payType: z.enum(["SALARY", "PER_SESSION", "HOURLY"], {
      message: "Pay type wajib dipilih",
    }),
    monthlySalary: z.coerce.number().min(0, "Monthly salary tidak boleh negatif").nullable(),
    ratePerSession: z.coerce.number().min(0, "Rate per session tidak boleh negatif").nullable(),
    ratePerStudent: z.coerce.number().min(0, "Rate per student tidak boleh negatif").nullable(),
    isActive: z.boolean().default(true),
  })
  .transform((value) => ({
    ...value,
    phone: value.phone?.trim() || undefined,
  }));

export type CoachProfileFormValues = z.input<typeof coachProfileFormSchema>;
export type CoachProfileFormSubmitValues = z.output<typeof coachProfileFormSchema>;
