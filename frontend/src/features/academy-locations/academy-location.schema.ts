import { z } from "zod";

export const academyLocationFormSchema = z.object({
  academyId: z.string().trim().min(1, "Academy wajib dipilih"),
  code: z.string().trim().min(2, "Code minimal 2 karakter").max(20, "Code maksimal 20 karakter"),
  name: z.string().trim().min(2, "Name minimal 2 karakter").max(120, "Name maksimal 120 karakter"),
  address: z.string().trim().min(1, "Address wajib diisi"),
  googleMapsUrl: z.string().trim().url("Google Maps URL tidak valid"),
  isActive: z.boolean().default(true),
});

export type AcademyLocationFormInputValues = z.input<typeof academyLocationFormSchema>;
export type AcademyLocationFormValues = z.output<typeof academyLocationFormSchema>;
