import { api } from "@/lib/api";
import type {
  CoachDashboardResponse,
  EventDashboardResponse,
  OwnerDashboardResponse,
  ParentDashboardResponse,
} from "@/lib/api-types";
import type { ApiResponse } from "@/types/api";

import {
  coachDashboardMock,
  eventDashboardMock,
  ownerDashboardMock,
  parentDashboardMock,
} from "./dashboard.mock";

export interface DashboardDateRangeParams {
  from?: string;
  to?: string;
}

export interface AcademyDashboardParams extends DashboardDateRangeParams {
  academyId?: string;
}

export interface ParentDashboardParams extends DashboardDateRangeParams {
  studentId?: string;
}

const useDummyDashboard = process.env.NEXT_PUBLIC_USE_DUMMY_DASHBOARD === "true";

const cloneDashboard = <T>(value: T): T => structuredClone(value);

export const dashboardService = {
  async getOwnerDashboard(params: AcademyDashboardParams): Promise<OwnerDashboardResponse> {
    if (useDummyDashboard) {
      return cloneDashboard(ownerDashboardMock);
    }

    const response = await api.get<ApiResponse<OwnerDashboardResponse>>("/api/dashboard/owner", { params });
    return response.data.data;
  },

  async getCoachDashboard(params: AcademyDashboardParams): Promise<CoachDashboardResponse> {
    if (useDummyDashboard) {
      return cloneDashboard(coachDashboardMock);
    }

    const response = await api.get<ApiResponse<CoachDashboardResponse>>("/api/dashboard/coach", { params });
    return response.data.data;
  },

  async getParentDashboard(params: ParentDashboardParams): Promise<ParentDashboardResponse> {
    if (useDummyDashboard) {
      return cloneDashboard(parentDashboardMock);
    }

    const response = await api.get<ApiResponse<ParentDashboardResponse>>("/api/dashboard/parent", { params });
    return response.data.data;
  },

  async getEventDashboard(params: AcademyDashboardParams): Promise<EventDashboardResponse> {
    if (useDummyDashboard) {
      return cloneDashboard(eventDashboardMock);
    }

    const response = await api.get<ApiResponse<EventDashboardResponse>>("/api/dashboard/event", { params });
    return response.data.data;
  },
};
