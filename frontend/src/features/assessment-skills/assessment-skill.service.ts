import { AssessmentSkill, AssessmentSkillCreateRequest, AssessmentSkillUpdateRequest } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { MasterSliceResponse } from "@/features/academies/academy.service";

export interface AssessmentSkillListParams {
  academyId?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
  active?: boolean | null;
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

type AssessmentSkillPayload = AssessmentSkillCreateRequest | AssessmentSkillUpdateRequest;

const BASE_URL = "/api/master/assessment-skills";

const normalizeSlice = (payload: AssessmentSkill[] | PageResponse<AssessmentSkill> | null | undefined, params?: AssessmentSkillListParams): MasterSliceResponse<AssessmentSkill> => {
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

const buildParams = (params?: AssessmentSkillListParams) => ({
  academyId: params?.academyId || undefined,
  search: params?.search || undefined,
  page: params?.page,
  size: params?.size,
  sort: params?.sort,
  direction: params?.direction,
  active: typeof params?.active === "boolean" ? params.active : undefined,
});

const toApiPayload = (payload: AssessmentSkillPayload) => ({
  academyId: payload.academyId || null,
  code: payload.code,
  name: payload.name,
  description: payload.description,
  orderNo: payload.orderNo ?? null,
  active: payload.active ?? true,
  maxScore: payload.maxScore ?? 5,
});

export const assessmentSkillService = {
  async slice(params?: AssessmentSkillListParams): Promise<MasterSliceResponse<AssessmentSkill>> {
    const response = await api.get<ApiResponse<AssessmentSkill[] | PageResponse<AssessmentSkill>>>(BASE_URL, {
      params: buildParams({ ...params, page: params?.page ?? 0, size: params?.size ?? 10 }),
    });
    return normalizeSlice(response.data.data, params);
  },

  async list(params?: AssessmentSkillListParams): Promise<AssessmentSkill[]> {
    const response = await this.slice(params);
    return response.items;
  },

  async create(payload: AssessmentSkillCreateRequest): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.post<ApiResponse<AssessmentSkill>>(BASE_URL, toApiPayload(payload));
    return response.data;
  },

  async getById(id: string): Promise<AssessmentSkill> {
    const response = await api.get<ApiResponse<AssessmentSkill>>(`${BASE_URL}/${id}`);
    return response.data.data;
  },

  async update(id: string, payload: AssessmentSkillUpdateRequest): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.put<ApiResponse<AssessmentSkill>>(`${BASE_URL}/${id}`, toApiPayload(payload));
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<AssessmentSkill>> {
    const response = await api.delete<ApiResponse<AssessmentSkill>>(`${BASE_URL}/${id}`);
    return response.data;
  },
};
