import { z } from "zod";

export const assessmentScoreSchema = z.object({
  skillCode: z.string().trim().min(1, "skillCode wajib diisi"),
  score: z.coerce.number().min(1, "Score minimal 1").max(5, "Score maksimal 5"),
  notes: z.string().trim().max(500, "Notes maksimal 500 karakter").optional().or(z.literal("")),
});

export const assessmentFormSchema = z
  .object({
    classSessionId: z.string().trim().min(1, "classSessionId wajib diisi"),
    studentId: z.string().trim().min(1, "studentId wajib diisi"),
    coachId: z.string().trim().min(1, "coachId wajib diisi"),
    assessedAt: z.string().trim().min(1, "assessedAt wajib diisi"),
    notes: z.string().trim().max(1000, "Notes maksimal 1000 karakter").optional().or(z.literal("")),
    scores: z.array(assessmentScoreSchema).min(1, "Minimal ada 1 score"),
  })
  .superRefine((values, ctx) => {
    const seen = new Set<string>();

    values.scores.forEach((item, index) => {
      if (seen.has(item.skillCode)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["scores", index, "skillCode"],
          message: "skillCode tidak boleh duplikat",
        });
      }
      seen.add(item.skillCode);
    });
  });

export type AssessmentScoreFormInputValues = z.input<typeof assessmentScoreSchema>;
export type AssessmentScoreFormValues = z.output<typeof assessmentScoreSchema>;
export type AssessmentFormInputValues = z.input<typeof assessmentFormSchema>;
export type AssessmentFormValues = z.output<typeof assessmentFormSchema>;
