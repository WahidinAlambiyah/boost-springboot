import { z } from "zod";

const currentYear = new Date().getUTCFullYear();

export const payrollPeriodSchema = z.object({
  month: z.coerce.number().int().min(1, "Bulan minimal 1").max(12, "Bulan maksimal 12"),
  year: z.coerce
    .number()
    .int()
    .min(2000, "Tahun minimal 2000")
    .max(currentYear + 1, `Tahun maksimal ${currentYear + 1}`),
});

export const coachPayrollItemUpdateSchema = z.object({
  bonusAmount: z.coerce.number().min(0, "Bonus tidak boleh negatif"),
  deductionAmount: z.coerce.number().min(0, "Potongan tidak boleh negatif"),
  notes: z.string().trim().max(500, "Catatan maksimal 500 karakter").optional(),
});

export const coachPayrollGenerateSchema = payrollPeriodSchema.extend({
  academyId: z.string().trim().min(1, "Academy wajib dipilih"),
});

export type PayrollPeriodFilterValues = z.infer<typeof payrollPeriodSchema>;
export type CoachPayrollGenerateValues = z.infer<typeof coachPayrollGenerateSchema>;
export type CoachPayrollItemUpdateValues = z.infer<typeof coachPayrollItemUpdateSchema>;
