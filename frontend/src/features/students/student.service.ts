import { api } from "@/lib/api";
import { Student } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { MasterSliceResponse } from "@/features/academies/academy.service";

export interface StudentListParams {
  academyId?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
  active?: boolean | null;
}

export interface StudentRequest {
  academyId: string;
  studentNo: string;
  fullName: string;
  nickname?: string;
  currentLevel?: string;
  active: boolean;
}

interface PageResponse<T> {
  items?: T[];
  content?: T[];
  page?: number;
  size?: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
  sort?: string;
  direction?: string;
}

const BASE_URL = "/api/master/students";

const normalizeSlice = (payload: Student[] | PageResponse<Student> | null | undefined, params?: StudentListParams): MasterSliceResponse<Student> => {
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

const buildParams = (params?: StudentListParams) => ({
  academyId: params?.academyId || undefined,
  search: params?.search || undefined,
  page: params?.page,
  size: params?.size,
  sort: params?.sort,
  direction: params?.direction,
  active: typeof params?.active === "boolean" ? params.active : undefined,
});

export const studentService = {
  isEndpointEnabled: true,

  async list(params?: StudentListParams): Promise<Student[]> {
    const response = await api.get<ApiResponse<Student[] | PageResponse<Student>>>(BASE_URL, {
      params: buildParams(params),
    });
    return normalizeSlice(response.data.data, params).items;
  },

  async slice(params?: StudentListParams): Promise<MasterSliceResponse<Student>> {
    const response = await api.get<ApiResponse<Student[] | PageResponse<Student>>>(BASE_URL, {
      params: buildParams({ ...params, page: params?.page ?? 0, size: params?.size ?? 10 }),
    });
    return normalizeSlice(response.data.data, params);
  },

  async create(payload: StudentRequest): Promise<ApiResponse<Student>> {
    const response = await api.post<ApiResponse<Student>>(BASE_URL, payload);
    return response.data;
  },

  async update(id: string, payload: StudentRequest): Promise<ApiResponse<Student>> {
    const response = await api.put<ApiResponse<Student>>(`${BASE_URL}/${id}`, payload);
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`${BASE_URL}/${id}`);
    return response.data;
  },
};
