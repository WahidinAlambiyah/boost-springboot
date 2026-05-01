import { z } from "zod";

export const studentFormSchema = z.object({
  studentNo: z.string().trim().min(1, "Student No wajib diisi").max(50, "Maksimum 50 karakter"),
  fullName: z.string().trim().min(2, "Nama minimal 2 karakter").max(120, "Maksimum 120 karakter"),
  nickname: z.string().trim().max(120, "Maksimum 120 karakter").optional().or(z.literal("")),
  gender: z.enum(["MALE", "FEMALE"]),
  dateOfBirth: z.string().trim().min(1, "Tanggal lahir wajib diisi"),
  currentLevel: z.string().trim().max(100, "Maksimum 100 karakter").optional().or(z.literal("")),
  emergencyContactName: z.string().trim().max(120, "Maksimum 120 karakter").optional().or(z.literal("")),
  emergencyContactPhone: z.string().trim().max(30, "Maksimum 30 karakter").optional().or(z.literal("")),
  photoConsent: z.boolean().default(false),
  isActive: z.boolean().default(true),
});

export type StudentFormValues = z.infer<typeof studentFormSchema>;
