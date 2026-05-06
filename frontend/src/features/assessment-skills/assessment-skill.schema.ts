import { z } from "zod";

export const assessmentSkillFormSchema = z.object({
  code: z.string().trim().min(1, "Code wajib diisi").max(50, "Maksimum 50 karakter"),
  name: z.string().trim().min(1, "Nama wajib diisi").max(120, "Maksimum 120 karakter"),
  description: z.string().trim().max(255, "Maksimum 255 karakter").optional().or(z.literal("")),
  maxScore: z.coerce.number().min(1, "Max score minimal 1"),
  orderNo: z.coerce.number().int("Order harus bilangan bulat").min(0, "Order minimal 0"),
  isActive: z.boolean().default(true),
});

export type AssessmentSkillFormInputValues = z.input<typeof assessmentSkillFormSchema>;
export type AssessmentSkillFormValues = z.output<typeof assessmentSkillFormSchema>;
