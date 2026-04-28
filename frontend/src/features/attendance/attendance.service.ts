import { AttendanceSubmitPayload, AttendanceSummary } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const attendanceService = {
  async getSummaries(): Promise<AttendanceSummary[]> {
    const response = await api.get<ApiResponse<AttendanceSummary[]>>("/api/attendance");
    return response.data.data;
  },

  async submit(payload: AttendanceSubmitPayload): Promise<ApiResponse<string>> {
    const response = await api.post<ApiResponse<string>>("/api/attendance/submit", payload);
    return response.data;
  },
};
