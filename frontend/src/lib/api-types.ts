export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface CatalogSummary {
  code: string;
  name: string;
  status: string;
}

export interface SchedulingSummary {
  code: string;
  name: string;
  status: string;
}

export interface EnrollmentSummary {
  id: string;
  studentName: string;
  classCode: string;
  status: string;
}

export interface EnrollmentRegisterPayload {
  studentId: string;
  classCode: string;
}

export interface AttendanceSummary {
  id: string;
  enrollmentId: string;
  sessionDate: string;
  status: string;
}

export interface AttendanceSubmitPayload {
  enrollmentId: string;
  sessionDate: string;
  status: string;
  note?: string;
}

export interface ReschedulePayload {
  classGroupId: string;
  previousStartAt: string;
  newStartAt: string;
  reason?: string;
}

export interface ValidationErrorPayload {
  errors?: Record<string, string>;
}
