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

export interface BillingSummary {
  code: string;
  name: string;
  status: string;
}

export interface PaymentPayload {
  invoiceId: string;
  amount: number;
}

export interface PaymentResponse {
  paymentId: string;
  invoiceId: string;
  amount: number;
  status: string;
}

export interface NotificationSummary {
  code: string;
  name: string;
  status: string;
}

export interface ValidationErrorPayload {
  errors?: Record<string, string>;
}

export interface Academy {
  id: string;
  code: string;
  name: string;
  description?: string;
  phone?: string;
  email?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AcademyCreateRequest {
  code: string;
  name: string;
  description?: string;
  phone?: string;
  email?: string;
}

export interface AcademyUpdateRequest {
  name: string;
  description?: string;
  phone?: string;
  email?: string;
}

export interface AcademyLocation {
  id: string;
  academyId: string;
  code: string;
  name: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  timezone?: string;
  phone?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AcademyLocationCreateRequest {
  academyId: string;
  code: string;
  name: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  timezone?: string;
  phone?: string;
}

export interface AcademyLocationUpdateRequest {
  name: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  timezone?: string;
  phone?: string;
}

export interface CoachProfile {
  id: string;
  academyId: string;
  userId: string;
  coachNo?: string;
  fullName: string;
  phone?: string;
  specialties?: string;
  bio?: string;
  employmentType: "FULL_TIME" | "PART_TIME" | "CONTRACT";
  payType: "SALARY" | "HOURLY" | "PER_SESSION";
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CoachProfileCreateRequest {
  academyId: string;
  userId: string;
  coachNo?: string;
  fullName: string;
  phone?: string;
  specialties?: string;
  bio?: string;
  employmentType: "FULL_TIME" | "PART_TIME" | "CONTRACT";
  payType: "SALARY" | "HOURLY" | "PER_SESSION";
}

export interface CoachProfileUpdateRequest {
  fullName: string;
  phone?: string;
  specialties?: string;
  bio?: string;
  employmentType: "FULL_TIME" | "PART_TIME" | "CONTRACT";
  payType: "SALARY" | "HOURLY" | "PER_SESSION";
}

export interface Student {
  id: string;
  academyId: string;
  studentNo?: string;
  fullName: string;
  nickname?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StudentUpdateRequest {
  fullName?: string;
  nickname?: string;
  status?: string;
}

export interface ClassSession {
  id: string;
  classGroupId: string;
  academyId: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  locationId?: string;
  status: string;
  notes?: string;
  coachIds: string[];
  coaches?: ClassSessionCoach[];
  createdAt: string;
  updatedAt: string;
}

export interface ClassSessionCreateRequest {
  classGroupId: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  locationId?: string;
  coachIds?: string[];
  status?: string;
}

export type ClassSessionUpdateRequest = ClassSessionCreateRequest;

export interface ClassSessionConflict {
  type: "TIME_OVERLAP" | "LOCATION_OVERLAP" | "COACH_OVERLAP";
  sessionId: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  locationId?: string;
  coachId?: string;
  message: string;
}

export interface ClassSessionCoach {
  id: string;
  classSessionId: string;
  academyId: string;
  coachId: string;
  coachRole?: string;
  attendanceStatus?: string;
  checkInAt?: string;
  checkOutAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClassSessionCoachCreateRequest {
  coachId: string;
  coachRole?: string;
  attendanceStatus?: string;
  checkInAt?: string;
  checkOutAt?: string;
  notes?: string;
}

export type ClassSessionCoachUpdateRequest = ClassSessionCoachCreateRequest;

export interface AttendanceRecord {
  studentId: string;
  attendance: {
    attendanceStatus: "PRESENT" | "ABSENT" | "PERMIT" | "SICK" | "LATE";
    checkInAt?: string | null;
    remarks?: string;
  };
}

export interface AttendanceRecordRequest {
  records: AttendanceRecord[];
}

export interface AttendanceRecordUpdateRequest {
  attendanceStatus: "PRESENT" | "ABSENT" | "PERMIT" | "SICK" | "LATE";
  checkInAt?: string | null;
  remarks?: string;
}

export interface AssessmentSkill {
  id: string;
  academyId: string;
  code: string;
  name: string;
  description?: string;
  orderNo?: number;
  active: boolean;
  maxScore: number;
  createdAt: string;
  updatedAt: string;
}

export interface AssessmentSkillRequestDto {
  academyId?: string;
  code: string;
  name: string;
  description?: string;
  orderNo?: number;
  active?: boolean;
  maxScore?: number;
}

export type AssessmentSkillCreateRequest = AssessmentSkillRequestDto;

export interface AssessmentSkillUpdateRequest {
  academyId?: string;
  code?: string;
  name?: string;
  description?: string;
  orderNo?: number;
  active?: boolean;
  maxScore?: number;
}

export interface StudentAssessmentSkillScore {
  skillCode: string;
  score: number;
  notes?: string;
}

export interface StudentAssessment {
  id: string;
  classSessionId: string;
  studentId: string;
  coachId: string;
  assessedAt: string;
  notes?: string;
  scores: StudentAssessmentSkillScore[];
}

export interface StudentAssessmentRequestDto {
  classSessionId: string;
  studentId: string;
  coachId: string;
  assessedAt: string;
  notes?: string;
  scores: StudentAssessmentSkillScore[];
}

export type StudentAssessmentCreateRequest = StudentAssessmentRequestDto;

export type StudentAssessmentUpdateRequest = Partial<StudentAssessmentRequestDto>;

export interface StudentProgressAttendance {
  totalSessions: number;
  present: number;
  permit: number;
  absent: number;
}

export interface StudentProgressSkillProgress {
  skillCode?: string;
  skillName?: string;
  latestScore?: number;
  averageScore?: number;
  previousScore?: number;
  trend: "UP" | "DOWN" | "FLAT";
}

export interface StudentProgressCoachNote {
  sessionId?: string;
  sessionDate: string;
  coachId?: string;
  coachName?: string;
  note?: string;
}

export interface StudentProgressReport {
  student: {
    id: string;
    studentNo?: string;
    fullName: string;
    nickname?: string;
    currentLevel?: string;
  };
  period: {
    from: string;
    to: string;
  };
  attendance: StudentProgressAttendance;
  skillProgress: StudentProgressSkillProgress[];
  coachNotes: StudentProgressCoachNote[];
}

export interface TrainingPackage {
  id: string;
  academyId: string;
  code: string;
  name: string;
  packageType: "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";
  price: number;
  sessionQuota?: number;
  description?: string;
  isActive?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TrainingPackageCreateRequest {
  academyId: string;
  code: string;
  name: string;
  packageType: "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";
  price: number;
  sessionQuota?: number;
  description?: string;
  isActive?: boolean;
}

export interface TrainingPackageUpdateRequest {
  code: string;
  name: string;
  packageType: "TRIAL" | "PER_SESSION" | "MONTHLY" | "SESSION_BUNDLE";
  price?: number;
  sessionQuota?: number;
  description?: string;
  isActive?: boolean;
}

export interface StudentPackageSubscription {
  id: string;
  academyId: string;
  studentId: string;
  packageId: string;
  packageType: string;
  status: string;
  startDate: string;
  endDate?: string;
  remainingSessions?: number;
  createdAt: string;
  updatedAt: string;
}

export interface StudentPackageSubscriptionCreateRequest {
  packageId: string;
  startDate: string;
  endDate?: string;
}

export interface CoachPayrollItem {
  id: string;
  coachId: string;
  baseAmount: number;
  bonusAmount: number;
  deductionAmount: number;
  totalAmount: number;
}

export interface CoachPayrollPeriod {
  id: string;
  academyId: string;
  periodMonth: number;
  periodYear: number;
  status: string;
  generatedAt?: string;
  approvedAt?: string;
  paidAt?: string;
  items: CoachPayrollItem[];
}

export interface CoachPayrollGenerateRequest {
  academyId: string;
  periodMonth: number;
  periodYear: number;
}

export interface CoachPayrollItemUpdateRequest {
  bonusAmount: number;
  deductionAmount: number;
}
