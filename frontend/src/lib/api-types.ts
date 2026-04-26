export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface CatalogSummary {
  code: string;
  name: string;
  status: string;
}

export interface SchedulingSummary {
  code: string;
  name: string;
  status: string;
}

export interface ReschedulePayload {
  classGroupId: string;
  previousStartAt: string;
  newStartAt: string;
  reason?: string;
}

export interface ValidationErrorPayload {
  errors?: Record<string, string>;
}
