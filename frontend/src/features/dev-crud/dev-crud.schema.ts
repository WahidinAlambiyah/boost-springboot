import { z } from "zod";

import { DEV_CRUD_PRIORITIES, DEV_CRUD_STATUSES } from "./dev-crud.types";

export const devCrudFormSchema = z.object({
  title: z.string().trim().min(3, "Judul minimal 3 karakter"),
  owner: z.string().trim().min(2, "Owner minimal 2 karakter"),
  status: z.enum(DEV_CRUD_STATUSES, { message: "Status wajib dipilih" }),
  priority: z.enum(DEV_CRUD_PRIORITIES, { message: "Prioritas wajib dipilih" }),
  dueDate: z.string().trim().min(1, "Tanggal jatuh tempo wajib diisi"),
});

export type DevCrudFormValues = z.input<typeof devCrudFormSchema>;
