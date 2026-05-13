import { api } from "@/lib/api";
import { TrainingPackage, TrainingPackageCreateRequest, TrainingPackageUpdateRequest } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { MasterSliceResponse } from "@/features/academies/academy.service";

export interface TrainingPackageListParams {
  academyId?: string;
  search?: string;
  packageType?: string;
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

type TrainingPackagePayload = (TrainingPackageCreateRequest | TrainingPackageUpdateRequest) & {
  academyId?: string;
  active?: boolean;
  isActive?: boolean;
};

const BASE_URL = "/api/master/training-packages";

const normalizeSlice = (payload: TrainingPackage[] | PageResponse<TrainingPackage> | null | undefined, params?: TrainingPackageListParams): MasterSliceResponse<TrainingPackage> => {
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

const buildParams = (params?: TrainingPackageListParams) => ({
  academyId: params?.academyId || undefined,
  search: params?.search || undefined,
  packageType: params?.packageType || undefined,
  page: params?.page,
  size: params?.size,
  sort: params?.sort,
  direction: params?.direction,
  active: typeof params?.active === "boolean" ? params.active : undefined,
});

const toApiPayload = (payload: TrainingPackagePayload) => {
  const { isActive, active, sessionQuota, validityDays, ...rest } = payload;
  return {
    ...rest,
    sessionQuota: sessionQuota ?? 0,
    validityDays: validityDays ?? null,
    active: typeof active === "boolean" ? active : Boolean(isActive ?? true),
  };
};

export const trainingPackageService = {
  async slice(params?: TrainingPackageListParams): Promise<MasterSliceResponse<TrainingPackage>> {
    const response = await api.get<ApiResponse<TrainingPackage[] | PageResponse<TrainingPackage>>>(BASE_URL, {
      params: buildParams({ ...params, page: params?.page ?? 0, size: params?.size ?? 10 }),
    });
    return normalizeSlice(response.data.data, params);
  },

  async getTrainingPackages(params?: TrainingPackageListParams): Promise<TrainingPackage[]> {
    const response = await this.slice(params);
    return response.items;
  },

  async create(payload: TrainingPackageCreateRequest & { active?: boolean }): Promise<ApiResponse<TrainingPackage>> {
    const response = await api.post<ApiResponse<TrainingPackage>>(BASE_URL, toApiPayload(payload));
    return response.data;
  },

  async createTrainingPackage(payload: TrainingPackageCreateRequest & { active?: boolean }): Promise<ApiResponse<TrainingPackage>> {
    return this.create(payload);
  },

  async update(id: string, payload: TrainingPackageUpdateRequest & { academyId?: string; active?: boolean }): Promise<ApiResponse<TrainingPackage>> {
    const response = await api.put<ApiResponse<TrainingPackage>>(`${BASE_URL}/${id}`, toApiPayload(payload));
    return response.data;
  },

  async updateTrainingPackage(id: string, payload: TrainingPackageUpdateRequest & { academyId?: string; active?: boolean }): Promise<ApiResponse<TrainingPackage>> {
    return this.update(id, payload);
  },

  async remove(id: string): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`${BASE_URL}/${id}`);
    return response.data;
  },

  async deleteTrainingPackage(id: string): Promise<ApiResponse<void>> {
    return this.remove(id);
  },
};
