import { api } from "@/lib/api";
import {
  TrainingPackage,
} from "@/lib/api-types";
import { ApiResponse } from "@/types/api";

export interface TrainingPackageListParams {
  academyId?: string;
}

export interface TrainingPackageCreatePayload {
  academyId: string;
  code: string;
  name: string;
  packageType: "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";
  price: number;
  sessionQuota?: number | null;
  validityDays?: number | null;
  description?: string;
  isActive?: boolean;
}

export interface TrainingPackageUpdatePayload {
  code: string;
  name: string;
  packageType: "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";
  price: number;
  sessionQuota?: number | null;
  validityDays?: number | null;
  description?: string;
  isActive?: boolean;
}

export const trainingPackageService = {
  async getTrainingPackages(params?: TrainingPackageListParams): Promise<TrainingPackage[]> {
    const response = await api.get<ApiResponse<TrainingPackage[]>>("/api/training-packages", { params });
    return response.data.data;
  },

  async createTrainingPackage(payload: TrainingPackageCreatePayload): Promise<ApiResponse<TrainingPackage>> {
    const response = await api.post<ApiResponse<TrainingPackage>>("/api/training-packages", payload);
    return response.data;
  },

  async getTrainingPackageById(id: string): Promise<TrainingPackage> {
    const response = await api.get<ApiResponse<TrainingPackage>>(`/api/training-packages/${id}`);
    return response.data.data;
  },

  async updateTrainingPackage(id: string, payload: TrainingPackageUpdatePayload): Promise<ApiResponse<TrainingPackage>> {
    const response = await api.put<ApiResponse<TrainingPackage>>(`/api/training-packages/${id}`, payload);
    return response.data;
  },

  async deleteTrainingPackage(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/training-packages/${id}`);
    return response.data;
  },
};
