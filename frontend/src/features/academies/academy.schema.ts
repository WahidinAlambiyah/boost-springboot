import { z } from "zod";

const optionalContact = z
  .string()
  .trim()
  .max(100, "Maksimum 100 karakter")
  .optional()
  .or(z.literal(""));

export const academyFormSchema = z.object({
  code: z.string().trim().min(2, "Code minimal 2 karakter").max(20, "Code maksimal 20 karakter"),
  name: z.string().trim().min(2, "Name minimal 2 karakter").max(120, "Name maksimal 120 karakter"),
  description: z.string().trim().max(255, "Description maksimal 255 karakter").optional().or(z.literal("")),
  phone: optionalContact,
  email: z.string().trim().email("Format email tidak valid").optional().or(z.literal("")),
  isActive: z.boolean().default(true),
});

export type AcademyFormValues = z.infer<typeof academyFormSchema>;
