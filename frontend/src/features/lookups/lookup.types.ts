export interface LookupOption {
  id: string;
  code?: string;
  label: string;
  description?: string | null;
  metadata?: Record<string, unknown> | null;
}

export interface LookupParams {
  academyId?: string;
  status?: string;
  level?: string;
  date?: string;
  search?: string;
  limit?: number;
  [key: string]: string | number | boolean | undefined;
}
