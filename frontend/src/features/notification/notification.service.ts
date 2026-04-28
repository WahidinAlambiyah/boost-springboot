import { NotificationSummary } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const notificationService = {
  async getSummaries(): Promise<NotificationSummary[]> {
    const response = await api.get<ApiResponse<NotificationSummary[]>>("/api/notification");
    return response.data.data;
  },
};
