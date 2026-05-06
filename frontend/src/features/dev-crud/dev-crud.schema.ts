import { z } from "zod";

import { TRAINING_CENTER_DEMO_STATUSES } from "./dev-crud.types";

export const devCrudFormSchema = z.object({
  code: z.string().trim().min(2, "Kode minimal 2 karakter").max(16, "Kode maksimal 16 karakter"),
  name: z.string().trim().min(3, "Nama training center minimal 3 karakter"),
  location: z.string().trim().min(3, "Lokasi minimal 3 karakter"),
  activeStudents: z.coerce
    .number({ message: "Jumlah murid aktif wajib diisi" })
    .int("Jumlah murid aktif harus bilangan bulat")
    .min(0, "Jumlah murid aktif tidak boleh negatif"),
  coachCount: z.coerce
    .number({ message: "Jumlah coach wajib diisi" })
    .int("Jumlah coach harus bilangan bulat")
    .min(0, "Jumlah coach tidak boleh negatif"),
  status: z.enum(TRAINING_CENTER_DEMO_STATUSES, { message: "Status wajib dipilih" }),
});

export type DevCrudFormInputValues = z.input<typeof devCrudFormSchema>;
export type DevCrudFormValues = z.output<typeof devCrudFormSchema>;
