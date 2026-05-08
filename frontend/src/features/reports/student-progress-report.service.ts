import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

import {
  StudentProgressReportParams,
  StudentProgressReportResponse,
} from "./student-progress-report.types";

export const studentProgressReportService = {
  async getStudentProgressReport(
    studentId: string,
    params: StudentProgressReportParams = {},
  ): Promise<StudentProgressReportResponse> {
    const searchParams = new URLSearchParams();

    if (params.from) searchParams.set("from", params.from);
    if (params.to) searchParams.set("to", params.to);

    const query = searchParams.toString();
    const response = await api.get<ApiResponse<StudentProgressReportResponse>>(
      `/api/reports/students/${studentId}/progress${query ? `?${query}` : ""}`,
    );

    return response.data.data;
  },
};
