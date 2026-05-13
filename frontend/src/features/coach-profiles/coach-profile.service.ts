import { CoachProfile, CoachProfileCreateRequest, CoachProfileUpdateRequest } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { MasterSliceResponse } from "@/features/academies/academy.service";

export interface CoachProfileListParams {
  academyId?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
  active?: boolean | null;
}

export interface UserOption {
  id: string;
  username?: string;
  email?: string;
  fullName?: string;
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

type CoachPayload = (CoachProfileCreateRequest | CoachProfileUpdateRequest) & {
  academyId?: string;
  userId?: string;
  coachNo?: string;
  isActive?: boolean;
  active?: boolean;
};

const BASE_URL = "/api/master/coaches";

const normalizeSlice = (payload: CoachProfile[] | PageResponse<CoachProfile> | null | undefined, params?: CoachProfileListParams): MasterSliceResponse<CoachProfile> => {
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

const buildParams = (params?: CoachProfileListParams) => ({
  academyId: params?.academyId || undefined,
  search: params?.search || undefined,
  page: params?.page,
  size: params?.size,
  sort: params?.sort,
  direction: params?.direction,
  active: typeof params?.active === "boolean" ? params.active : undefined,
});

const toApiPayload = (payload: CoachPayload) => {
  const { isActive, active, ...rest } = payload;
  return {
    ...rest,
    active: typeof active === "boolean" ? active : Boolean(isActive),
  };
};

export const coachProfileService = {
  async list(params?: CoachProfileListParams): Promise<CoachProfile[]> {
    const response = await api.get<ApiResponse<CoachProfile[] | PageResponse<CoachProfile>>>(BASE_URL, {
      params: buildParams(params),
    });
    return normalizeSlice(response.data.data, params).items;
  },

  async slice(params?: CoachProfileListParams): Promise<MasterSliceResponse<CoachProfile>> {
    const response = await api.get<ApiResponse<CoachProfile[] | PageResponse<CoachProfile>>>(BASE_URL, {
      params: buildParams({ ...params, page: params?.page ?? 0, size: params?.size ?? 10 }),
    });
    return normalizeSlice(response.data.data, params);
  },

  async listUsers(search?: string): Promise<UserOption[]> {
    const response = await api.get<ApiResponse<UserOption[]>>("/api/master/user-options", {
      params: { search: search || undefined, size: 100 },
    });
    return response.data.data;
  },

  async create(payload: CoachProfileCreateRequest & { active?: boolean }): Promise<ApiResponse<CoachProfile>> {
    const response = await api.post<ApiResponse<CoachProfile>>(BASE_URL, toApiPayload(payload));
    return response.data;
  },

  async update(id: string, payload: CoachProfileUpdateRequest & { academyId?: string; userId?: string; coachNo?: string; active?: boolean }): Promise<ApiResponse<CoachProfile>> {
    const response = await api.put<ApiResponse<CoachProfile>>(`${BASE_URL}/${id}`, toApiPayload(payload));
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`${BASE_URL}/${id}`);
    return response.data;
  },
};
