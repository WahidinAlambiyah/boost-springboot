import { AssessmentSkill, AssessmentSkillCreateRequest, AssessmentSkillUpdateRequest } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const assessmentSkillService = {
  async list(): Promise<AssessmentSkill[]> {
    const response = await api.get<ApiResponse<AssessmentSkill[]>>("/api/assessment-skills");
    return response.data.data;
  },

  async create(payload: AssessmentSkillCreateRequest): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.post<ApiResponse<AssessmentSkill>>("/api/assessment-skills", payload);
    return response.data;
  },

  async getById(id: string): Promise<AssessmentSkill> {
    const response = await api.get<ApiResponse<AssessmentSkill>>(`/api/assessment-skills/${id}`);
    return response.data.data;
  },

  async update(id: string, payload: AssessmentSkillUpdateRequest): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.put<ApiResponse<AssessmentSkill>>(`/api/assessment-skills/${id}`, payload);
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.delete<ApiResponse<AssessmentSkill>>(`/api/assessment-skills/${id}`);
    return response.data;
  },
};
