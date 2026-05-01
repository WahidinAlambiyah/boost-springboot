import { CoachProfile } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { CoachProfileFormSubmitValues } from "@/features/coach-profiles/coach-profile.schema";

export interface CoachProfileListParams {
  academyId?: string;
}

export interface UserOption {
  id: string;
  username?: string;
  email?: string;
}

export const coachProfileService = {
  async list(params?: CoachProfileListParams): Promise<CoachProfile[]> {
    const response = await api.get<ApiResponse<CoachProfile[]>>("/api/coach-profiles", { params });
    return response.data.data;
  },

  async listUsers(): Promise<UserOption[]> {
    const response = await api.get<ApiResponse<UserOption[]>>("/api/users");
    return response.data.data;
  },

  async create(payload: CoachProfileFormSubmitValues): Promise<ApiResponse<CoachProfile>> {
    const response = await api.post<ApiResponse<CoachProfile>>("/api/coach-profiles", payload);
    return response.data;
  },

  async update(id: string, payload: CoachProfileFormSubmitValues): Promise<ApiResponse<CoachProfile>> {
    const response = await api.put<ApiResponse<CoachProfile>>(`/api/coach-profiles/${id}`, payload);
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/coach-profiles/${id}`);
    return response.data;
  },
};
