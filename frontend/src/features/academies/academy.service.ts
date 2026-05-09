import { Academy, AcademyCreateRequest, AcademyUpdateRequest } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface AcademyListParams {
  search?: string;
  page?: number;
}

interface PageResponse<T> {
  content?: T[];
}

const normalizeAcademyList = (payload: Academy[] | PageResponse<Academy> | null | undefined) => {
  if (Array.isArray(payload)) {
    return payload;
  }

  return payload?.content ?? [];
};

export const academyService = {
  async list(params?: AcademyListParams): Promise<Academy[]> {
    const response = await api.get<ApiResponse<Academy[] | PageResponse<Academy>>>("/api/academies", {
      params: {
        keyword: params?.search || undefined,
        page: params?.page,
      },
    });

    return normalizeAcademyList(response.data.data);
  },

  async create(payload: AcademyCreateRequest): Promise<ApiResponse<Academy>> {
    const response = await api.post<ApiResponse<Academy>>("/api/academies", payload);
    return response.data;
  },

  async update(id: string, payload: AcademyUpdateRequest): Promise<ApiResponse<Academy>> {
    const response = await api.put<ApiResponse<Academy>>(`/api/academies/${id}`, payload);
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/academies/${id}`);
    return response.data;
  },
};
