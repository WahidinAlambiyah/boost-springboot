import { z } from "zod";

const htmlDateSchema = z.string().trim().regex(/^\d{4}-\d{2}-\d{2}$/, "Format tanggal harus YYYY-MM-DD");
const htmlTimeSchema = z.string().trim().regex(/^([01]\d|2[0-3]):([0-5]\d)$/, "Format jam harus HH:mm");

const classSessionTimeRangeSchema = z
  .object({
    startTime: htmlTimeSchema,
    endTime: htmlTimeSchema,
  })
  .refine(({ startTime, endTime }) => endTime > startTime, {
    path: ["endTime"],
    message: "Jam selesai harus lebih besar dari jam mulai",
  });

export const classSessionFormSchema = z
  .object({
    academyId: z.string().trim().min(1, "Academy wajib dipilih"),
    locationId: z.string().trim().min(1, "Lokasi wajib dipilih"),
    sessionDate: htmlDateSchema,
    startTime: htmlTimeSchema,
    endTime: htmlTimeSchema,
    notes: z.string().trim().max(1000, "Catatan maksimal 1000 karakter").optional().or(z.literal("")),
    status: z.string().trim().min(1, "Status wajib dipilih"),
  })
  .and(classSessionTimeRangeSchema);

export const classSessionConflictQuerySchema = z
  .object({
    academyId: z.string().trim().min(1, "academyId wajib diisi"),
    sessionDate: htmlDateSchema,
    startTime: htmlTimeSchema,
    endTime: htmlTimeSchema,
    locationId: z.string().trim().min(1, "locationId wajib diisi").optional(),
    coachId: z.string().trim().min(1, "coachId wajib diisi").optional(),
  })
  .and(classSessionTimeRangeSchema)
  .refine(({ locationId, coachId }) => Boolean(locationId || coachId), {
    path: ["locationId"],
    message: "Minimal isi locationId atau coachId",
  });

export type ClassSessionFormValues = z.infer<typeof classSessionFormSchema>;
export type ClassSessionConflictQueryValues = z.infer<typeof classSessionConflictQuerySchema>;
