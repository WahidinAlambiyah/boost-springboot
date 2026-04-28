import { EnrollmentRegisterPayload, EnrollmentSummary } from "@/lib/api-types";
import { api } from "@/lib/api";
import { createIdempotencyKey } from "@/lib/idempotency";
import { ApiResponse } from "@/types/api";

export const enrollmentService = {
  async getSummaries(): Promise<EnrollmentSummary[]> {
    const response = await api.get<ApiResponse<EnrollmentSummary[]>>("/api/enrollment");
    return response.data.data;
  },

  async register(payload: EnrollmentRegisterPayload): Promise<ApiResponse<string>> {
    const response = await api.post<ApiResponse<string>>(
      "/api/enrollment/register",
      payload,
      {
        headers: {
          "Idempotency-Key": createIdempotencyKey(),
        },
      },
    );
    return response.data;
  },

  async cancel(enrollmentId: string): Promise<ApiResponse<string>> {
    const response = await api.post<ApiResponse<string>>(`/api/enrollment/${enrollmentId}/cancel`);
    return response.data;
  },
};
