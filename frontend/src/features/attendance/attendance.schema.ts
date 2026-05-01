import { z } from "zod";

export const attendanceStatuses = ["PRESENT", "ABSENT", "PERMIT", "SICK", "LATE"] as const;

export const attendanceStatusSchema = z.enum(attendanceStatuses);

export const attendanceRowInputSchema = z.object({
  studentId: z.string().trim().min(1, "studentId wajib diisi"),
  attendanceStatus: attendanceStatusSchema.optional(),
  remarks: z.string().trim().max(500, "Remarks maksimal 500 karakter").optional().or(z.literal("")),
});

export const attendanceBulkFormSchema = z.object({
  classSessionId: z.string().trim().min(1, "Sesi kelas wajib dipilih"),
  records: z
    .array(attendanceRowInputSchema)
    .min(1, "Minimal ada 1 murid")
    .refine((records) => records.some((record) => Boolean(record.attendanceStatus)), {
      message: "Isi minimal 1 status absensi",
      path: ["records"],
    }),
});

export type AttendanceStatusOption = (typeof attendanceStatuses)[number];
export type AttendanceRowInput = z.infer<typeof attendanceRowInputSchema>;
export type AttendanceBulkFormValues = z.infer<typeof attendanceBulkFormSchema>;
