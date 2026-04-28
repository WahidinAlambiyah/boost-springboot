import { ReschedulePayload, SchedulingSummary } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const schedulingService = {
  async getSummaries(): Promise<SchedulingSummary[]> {
    const response = await api.get<ApiResponse<SchedulingSummary[]>>("/api/scheduling");
    return response.data.data;
  },

  async reschedule(payload: ReschedulePayload): Promise<ApiResponse<string>> {
    const response = await api.post<ApiResponse<string>>("/api/scheduling/reschedule", payload);
    return response.data;
  },
};
