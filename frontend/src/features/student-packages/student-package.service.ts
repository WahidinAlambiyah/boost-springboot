import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface StudentPackage {
  id: string;
  studentId: string;
  trainingPackageId: string;
  packageName: string;
  startDate: string;
  endDate?: string | null;
  remainingSessions: number;
  status: "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
}

export interface StudentPackagePayload {
  trainingPackageId: string;
  startDate: string;
  endDate?: string;
  remainingSessions: number;
  status: "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
}

export const studentPackageService = {
  async getStudentPackages(studentId: string): Promise<StudentPackage[]> {
    const response = await api.get<ApiResponse<StudentPackage[]>>(`/api/students/${studentId}/packages`);
    return response.data.data;
  },

  async createStudentPackage(studentId: string, payload: StudentPackagePayload): Promise<ApiResponse<StudentPackage>> {
    const response = await api.post<ApiResponse<StudentPackage>>(`/api/students/${studentId}/packages`, payload);
    return response.data;
  },

  async updateStudentPackage(studentId: string, subscriptionId: string, payload: StudentPackagePayload): Promise<ApiResponse<StudentPackage>> {
    const response = await api.put<ApiResponse<StudentPackage>>(`/api/students/${studentId}/packages/${subscriptionId}`, payload);
    return response.data;
  },
};
