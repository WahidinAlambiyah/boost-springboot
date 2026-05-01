import {
  ClassSession,
  ClassSessionCoach,
  ClassSessionCoachCreateRequest,
  ClassSessionCoachUpdateRequest,
  ClassSessionConflict,
  ClassSessionCreateRequest,
  ClassSessionUpdateRequest,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

interface ClassSessionBaseQuery {
  academyId?: string;
  locationId?: string;
  startDate?: string;
  endDate?: string;
  sessionDate?: string;
}

export interface ClassSessionListParams extends ClassSessionBaseQuery {
  classGroupId?: string;
  status?: string;
}

export interface ClassSessionConflictQuery extends ClassSessionBaseQuery {
  classSessionId?: string;
  coachId?: string;
  startTime?: string;
  endTime?: string;
  excludeSessionId?: string;
}

const buildClassSessionQueryParams = (query?: ClassSessionBaseQuery) => {
  if (!query) {
    return undefined;
  }

  return {
    academyId: query.academyId,
    locationId: query.locationId,
    startDate: query.startDate,
    endDate: query.endDate,
    sessionDate: query.sessionDate,
  };
};

export const classSessionService = {
  async getClassSessions(params?: ClassSessionListParams): Promise<ClassSession[]> {
    const response = await api.get<ApiResponse<ClassSession[]>>("/api/class-sessions", {
      params: {
        ...buildClassSessionQueryParams(params),
        classGroupId: params?.classGroupId,
        status: params?.status,
      },
    });

    return response.data.data;
  },

  async createClassSession(payload: ClassSessionCreateRequest): Promise<ApiResponse<ClassSession>> {
    const response = await api.post<ApiResponse<ClassSession>>("/api/class-sessions", payload);
    return response.data;
  },

  async getClassSessionById(id: string): Promise<ClassSession> {
    const response = await api.get<ApiResponse<ClassSession>>(`/api/class-sessions/${id}`);
    return response.data.data;
  },

  async updateClassSession(id: string, payload: ClassSessionUpdateRequest): Promise<ApiResponse<ClassSession>> {
    const response = await api.put<ApiResponse<ClassSession>>(`/api/class-sessions/${id}`, payload);
    return response.data;
  },

  async deleteClassSession(id: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/class-sessions/${id}`);
    return response.data;
  },

  async getSessionConflicts(query?: ClassSessionConflictQuery): Promise<ClassSessionConflict[]> {
    const response = await api.get<ApiResponse<ClassSessionConflict[]>>("/api/class-sessions/conflicts", {
      params: {
        ...buildClassSessionQueryParams(query),
        classSessionId: query?.classSessionId,
        coachId: query?.coachId,
        startTime: query?.startTime,
        endTime: query?.endTime,
        excludeSessionId: query?.excludeSessionId,
      },
    });

    return response.data.data;
  },

  async getSessionCoaches(classSessionId: string): Promise<ClassSessionCoach[]> {
    const response = await api.get<ApiResponse<ClassSessionCoach[]>>(`/api/class-sessions/${classSessionId}/coaches`);
    return response.data.data;
  },

  async createSessionCoach(
    classSessionId: string,
    payload: ClassSessionCoachCreateRequest,
  ): Promise<ApiResponse<ClassSessionCoach>> {
    const response = await api.post<ApiResponse<ClassSessionCoach>>(
      `/api/class-sessions/${classSessionId}/coaches`,
      payload,
    );
    return response.data;
  },

  async updateSessionCoach(
    classSessionId: string,
    sessionCoachId: string,
    payload: ClassSessionCoachUpdateRequest,
  ): Promise<ApiResponse<ClassSessionCoach>> {
    const response = await api.put<ApiResponse<ClassSessionCoach>>(
      `/api/class-sessions/${classSessionId}/coaches/${sessionCoachId}`,
      payload,
    );
    return response.data;
  },

  async deleteSessionCoach(classSessionId: string, sessionCoachId: string): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(
      `/api/class-sessions/${classSessionId}/coaches/${sessionCoachId}`,
    );
    return response.data;
  },
};
