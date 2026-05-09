export type StudentProgressTrend = "UP" | "DOWN" | "FLAT" | "STABLE" | string;

export interface StudentProgressReportParams {
  from?: string;
  to?: string;
}

export interface StudentProgressReportStudent {
  id: string;
  studentNo?: string | null;
  fullName: string;
  nickname?: string | null;
  dateOfBirth?: string | null;
  ageText?: string | null;
  currentLevel?: string | null;
  academyName?: string | null;
  guardianNames?: string[] | null;
  status?: string | null;
}

export interface StudentProgressReportPeriod {
  from?: string | null;
  to?: string | null;
}

export interface StudentProgressAttendanceSummary {
  totalSessions?: number | null;
  total?: number | null;
  present?: number | null;
  absent?: number | null;
  permit?: number | null;
  sick?: number | null;
  late?: number | null;
  alpha?: number | null;
  attendanceRate?: number | string | null;
}

export interface StudentProgressAttendanceTimelineItem {
  sessionId: string;
  sessionDate: string;
  startTime?: string | null;
  endTime?: string | null;
  classGroupName?: string | null;
  locationName?: string | null;
  status?: string | null;
  remarks?: string | null;
}

export interface StudentProgressSkillProgress {
  skillCode?: string | null;
  skillName: string;
  latestScore?: number | string | null;
  latest?: number | string | null;
  averageScore?: number | string | null;
  average?: number | string | null;
  previousScore?: number | string | null;
  trend?: StudentProgressTrend | null;
  maxScore?: number | null;
  latestNotes?: string | null;
}

export interface StudentProgressCoachNote {
  assessmentId?: string | null;
  sessionDate?: string | null;
  coachName?: string | null;
  overallNotes?: string | null;
  recommendation?: string | null;
}

export interface StudentProgressNextRecommendation {
  title: string;
  description?: string | null;
  priority?: string | null;
}

export interface StudentProgressUpcomingSession {
  sessionId: string;
  sessionDate: string;
  startTime?: string | null;
  endTime?: string | null;
  classGroupName?: string | null;
  locationName?: string | null;
}

export interface StudentProgressReportResponse {
  student: StudentProgressReportStudent;
  period?: StudentProgressReportPeriod | null;
  attendanceSummary: StudentProgressAttendanceSummary;
  attendanceTimeline?: StudentProgressAttendanceTimelineItem[] | null;
  skillProgress: StudentProgressSkillProgress[];
  coachNotes?: StudentProgressCoachNote[] | string | null;
  nextRecommendations?: StudentProgressNextRecommendation[] | null;
  upcomingSessions?: StudentProgressUpcomingSession[] | null;
  whatsappSummary?: string | null;
  recommendation?: string | null;
}
