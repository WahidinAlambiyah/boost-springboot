import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface StudentProgressReportResponse {
  student: {
    id: string;
    fullName: string;
    nickname?: string;
    studentNo?: string;
    academyId?: string;
    status?: string;
  };
  attendanceSummary: {
    total: number;
    present: number;
    permit: number;
    sick: number;
    absent: number;
    alpha: number;
  };
  skillProgress: Array<{
    skillCode?: string;
    skillName: string;
    latest: number;
    average: number;
    trend: "UP" | "DOWN" | "STABLE" | string;
  }>;
  coachNotes?: string;
  recommendation?: string;
}

export interface StudentProgressReportFilter {
  studentId: string;
  from?: string;
  to?: string;
}

export const progressReportService = {
  async getStudentProgressReport(filter: StudentProgressReportFilter): Promise<StudentProgressReportResponse> {
    const params = new URLSearchParams();

    if (filter.from) params.set("from", filter.from);
    if (filter.to) params.set("to", filter.to);

    const query = params.toString();
    const response = await api.get<ApiResponse<StudentProgressReportResponse>>(
      `/api/reports/students/${filter.studentId}/progress${query ? `?${query}` : ""}`,
    );

    return response.data.data;
  },
};
