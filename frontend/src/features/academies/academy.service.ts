import { Academy, AcademyCreateRequest, AcademyUpdateRequest } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface MasterSliceResponse<T> {
  items: T[];
  content?: T[];
  page: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
  sort?: string;
  direction?: string;
}

export interface AcademyListParams {
  search?: string;
  keyword?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
  active?: boolean | null;
}

interface PageResponse<T> {
  content?: T[];
  items?: T[];
  page?: number;
  size?: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
  sort?: string;
  direction?: string;
}

const normalizeSlice = (payload: Academy[] | PageResponse<Academy> | null | undefined, params?: AcademyListParams): MasterSliceResponse<Academy> => {
  const items = Array.isArray(payload) ? payload : payload?.items ?? payload?.content ?? [];
  const page = Array.isArray(payload) ? params?.page ?? 0 : payload?.page ?? params?.page ?? 0;
  const size = Array.isArray(payload) ? params?.size ?? items.length : payload?.size ?? params?.size ?? items.length;
  return {
    items,
    content: Array.isArray(payload) ? items : payload?.content,
    page,
    size,
    hasNext: Array.isArray(payload) ? false : Boolean(payload?.hasNext),
    hasPrevious: Array.isArray(payload) ? page > 0 : Boolean(payload?.hasPrevious ?? page > 0),
    sort: Array.isArray(payload) ? params?.sort : payload?.sort,
    direction: Array.isArray(payload) ? params?.direction : payload?.direction,
  };
};

export const academyService = {
  async list(params?: AcademyListParams): Promise<Academy[]> {
    const response = await api.get<ApiResponse<Academy[] | PageResponse<Academy>>>("/api/academies", {
      params: {
        keyword: params?.keyword ?? params?.search || undefined,
        search: params?.search || undefined,
        page: params?.page,
        size: params?.size,
        sort: params?.sort,
        direction: params?.direction,
        active: typeof params?.active === "boolean" ? params.active : undefined,
      },
    });
    return normalizeSlice(response.data.data, params).items;
  },

  async slice(params?: AcademyListParams): Promise<MasterSliceResponse<Academy>> {
    const response = await api.get<ApiResponse<Academy[] | PageResponse<Academy>>>("/api/academies", {
      params: {
        keyword: params?.keyword ?? params?.search || undefined,
        search: params?.search || undefined,
        page: params?.page ?? 0,
        size: params?.size ?? 10,
        sort: params?.sort,
        direction: params?.direction,
        active: typeof params?.active === "boolean" ? params.active : undefined,
      },
    });
    return normalizeSlice(response.data.data, params);
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
