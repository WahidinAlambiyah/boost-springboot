import {
  CoachPayrollGenerateRequest,
  CoachPayrollItemUpdateRequest,
  CoachPayrollPeriod,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface UpdateCoachPayrollItemPayload extends CoachPayrollItemUpdateRequest {
  notes?: string;
}

export const coachPayrollService = {
  async generateCoachPayroll(payload: CoachPayrollGenerateRequest): Promise<ApiResponse<CoachPayrollPeriod>> {
    const response = await api.post<ApiResponse<CoachPayrollPeriod>>("/api/payroll/coach/generate", payload);
    return response.data;
  },

  async getCoachPayrollByPeriod(month: number, year: number): Promise<CoachPayrollPeriod[]> {
    const response = await api.get<ApiResponse<CoachPayrollPeriod[]>>("/api/payroll/coach", {
      params: {
        month,
        year,
      },
    });

    return response.data.data;
  },

  async getCoachPayrollDetail(payrollPeriodId: string): Promise<CoachPayrollPeriod> {
    const response = await api.get<ApiResponse<CoachPayrollPeriod>>(`/api/payroll/coach/${payrollPeriodId}`);
    return response.data.data;
  },

  async updateCoachPayrollItem(
    payrollPeriodId: string,
    itemId: string,
    payload: UpdateCoachPayrollItemPayload,
  ): Promise<ApiResponse<CoachPayrollPeriod>> {
    const response = await api.put<ApiResponse<CoachPayrollPeriod>>(
      `/api/payroll/coach/${payrollPeriodId}/items/${itemId}`,
      payload,
    );

    return response.data;
  },

  async approveCoachPayroll(payrollPeriodId: string): Promise<ApiResponse<CoachPayrollPeriod>> {
    const response = await api.post<ApiResponse<CoachPayrollPeriod>>(`/api/payroll/coach/${payrollPeriodId}/approve`);
    return response.data;
  },

  async markCoachPayrollPaid(payrollPeriodId: string): Promise<ApiResponse<CoachPayrollPeriod>> {
    const response = await api.post<ApiResponse<CoachPayrollPeriod>>(`/api/payroll/coach/${payrollPeriodId}/mark-paid`);
    return response.data;
  },
};
