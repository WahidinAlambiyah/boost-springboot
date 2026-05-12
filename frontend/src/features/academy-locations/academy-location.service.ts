import {
  AcademyLocation,
  AcademyLocationCreateRequest,
  AcademyLocationUpdateRequest,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { MasterSliceResponse } from "@/features/academies/academy.service";

export interface AcademyLocationListParams {
  academyId?: string;
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

type AcademyLocationPayload = (AcademyLocationCreateRequest | AcademyLocationUpdateRequest) & {
  academyId?: string;
  googleMapsUrl?: string;
  isActive?: boolean;
  active?: boolean;
};

const BASE_URL = "/api/master/academy-locations";

const normalizeSlice = (payload: AcademyLocation[] | PageResponse<AcademyLocation> | null | undefined, params?: AcademyLocationListParams): MasterSliceResponse<AcademyLocation> => {
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

const toApiPayload = (payload: AcademyLocationPayload) => {
  const { isActive, active, googleMapsUrl: _googleMapsUrl, ...rest } = payload;
  return {
    ...rest,
    active: typeof active === "boolean" ? active : Boolean(isActive),
  };
};

export const academyLocationService = {
  async list(params?: AcademyLocationListParams): Promise<AcademyLocation[]> {
    const response = await api.get<ApiResponse<AcademyLocation[] | PageResponse<AcademyLocation>>>(BASE_URL, {
      params: {
        academyId: params?.academyId || undefined,
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

  async slice(params?: AcademyLocationListParams): Promise<MasterSliceResponse<AcademyLocation>> {
    const response = await api.get<ApiResponse<AcademyLocation[] | PageResponse<AcademyLocation>>>(BASE_URL, {
      params: {
        academyId: params?.academyId || undefined,
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

  async create(payload: AcademyLocationCreateRequest & { googleMapsUrl?: string; isActive?: boolean }): Promise<ApiResponse<AcademyLocation>> {
    const response = await api.post<ApiResponse<AcademyLocation>>(BASE_URL, toApiPayload(payload));
    return response.data;
  },

  async update(
    id: string,
    payload: AcademyLocationUpdateRequest & { academyId?: string; googleMapsUrl?: string; isActive?: boolean },
  ): Promise<ApiResponse<AcademyLocation>> {
    const response = await api.put<ApiResponse<AcademyLocation>>(`${BASE_URL}/${id}`, toApiPayload(payload));
    return response.data;
  },

  async remove(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`${BASE_URL}/${id}`);
    return response.data;
  },
};
