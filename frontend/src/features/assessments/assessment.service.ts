import {
  StudentAssessment,
  StudentAssessmentCreateRequest,
  StudentAssessmentUpdateRequest,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export type AssessmentListParams = {
  studentId?: string;
  classSessionId?: string;
  from?: string;
  to?: string;
};

export const assessmentService = {
  async getAssessments(params: AssessmentListParams = {}): Promise<StudentAssessment[]> {
    const response = await api.get<ApiResponse<StudentAssessment[]>>("/api/assessments", { params });
    return response.data.data;
  },

  async createAssessment(payload: StudentAssessmentCreateRequest): Promise<ApiResponse<StudentAssessment>> {
    const response = await api.post<ApiResponse<StudentAssessment>>("/api/assessments", payload);
    return response.data;
  },

  async getAssessmentById(id: string): Promise<StudentAssessment> {
    const response = await api.get<ApiResponse<StudentAssessment>>(`/api/assessments/${id}`);
    return response.data.data;
  },

  async updateAssessment(
    id: string,
    payload: StudentAssessmentUpdateRequest,
  ): Promise<ApiResponse<StudentAssessment>> {
    const response = await api.put<ApiResponse<StudentAssessment>>(`/api/assessments/${id}`, payload);
    return response.data;
  },

  async deleteAssessment(id: string): Promise<ApiResponse<StudentAssessment>> {
    const response = await api.delete<ApiResponse<StudentAssessment>>(`/api/assessments/${id}`);
    return response.data;
  },
};
