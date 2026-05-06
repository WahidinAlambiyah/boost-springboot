import {
  AttendanceRecordRequest,
  AttendanceRecordUpdateRequest,
  AttendanceSubmitPayload,
  AttendanceSummary,
} from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export type AttendanceStatus = "PRESENT" | "ABSENT" | "PERMIT" | "SICK" | "LATE";

export interface SessionAttendanceStudent {
  studentId: string;
  attendanceStatus: AttendanceStatus;
  checkInAt?: string | null;
  remarks?: string;
  studentName?: string;
}

export const attendanceService = {
  // Legacy endpoint: tetap dipertahankan agar halaman lama tidak breaking.
  async getSummaries(): Promise<AttendanceSummary[]> {
    const response = await api.get<ApiResponse<AttendanceSummary[]>>("/api/attendance");
    return response.data.data;
  },

  // Legacy endpoint: tetap dipertahankan agar halaman lama tidak breaking.
  async submit(payload: AttendanceSubmitPayload): Promise<ApiResponse<string>> {
    const response = await api.post<ApiResponse<string>>("/api/attendance/submit", payload);
    return response.data;
  },

  async getAttendanceBySession(classSessionId: string): Promise<SessionAttendanceStudent[]> {
    const response = await api.get<ApiResponse<SessionAttendanceStudent[]>>(
      `/api/attendance/sessions/${classSessionId}`,
    );
    return response.data.data;
  },

  async submitAttendanceBulk(
    classSessionId: string,
    payload: AttendanceRecordRequest,
  ): Promise<ApiResponse<unknown>> {
    const response = await api.post<ApiResponse<unknown>>(
      `/api/attendance/sessions/${classSessionId}/batch-upsert`,
      payload,
    );
    return response.data;
  },

  async updateAttendanceByStudent(
    classSessionId: string,
    studentId: string,
    payload: AttendanceRecordUpdateRequest,
  ): Promise<ApiResponse<string>> {
    const response = await api.put<ApiResponse<string>>(
      `/api/attendance/sessions/${classSessionId}/students/${studentId}`,
      payload,
    );
    return response.data;
  },
};
