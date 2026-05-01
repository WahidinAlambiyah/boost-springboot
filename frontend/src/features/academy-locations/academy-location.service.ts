import {
  AcademyLocation,
  AcademyLocationCreateRequest,
  AcademyLocationUpdateRequest,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface AcademyLocationListParams {
  academyId?: string;
}

export const academyLocationService = {
  async list(params?: AcademyLocationListParams): Promise<AcademyLocation[]> {
    const response = await api.get<ApiResponse<AcademyLocation[]>>("/api/academy-locations", { params });
    return response.data.data;
  },

  async create(payload: AcademyLocationCreateRequest & { googleMapsUrl: string; isActive: boolean }): Promise<ApiResponse<AcademyLocation>> {
    const response = await api.post<ApiResponse<AcademyLocation>>("/api/academy-locations", payload);
    return response.data;
  },

  async update(
    id: string,
    payload: AcademyLocationUpdateRequest & { academyId: string; googleMapsUrl: string; isActive: boolean },
  ): Promise<ApiResponse<AcademyLocation>> {
    const response = await api.put<ApiResponse<AcademyLocation>>(`/api/academy-locations/${id}`, payload);
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/academy-locations/${id}`);
    return response.data;
  },
};
