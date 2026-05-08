import type {
  CoachDashboardResponse,
  EventDashboardResponse,
  OwnerDashboardResponse,
  ParentDashboardResponse,
} from "@/lib/api-types";

const today = "2026-05-08";
const period = {
  from: "2026-05-01",
  to: "2026-05-31",
};

export const ownerDashboardMock: OwnerDashboardResponse = {
  academy: {
    id: "academy-demo-001",
    name: "Pushbike Academy Demo",
    code: "PBA-DEMO",
  },
  period,
  summaryCards: {
    activeStudents: 128,
    activeCoaches: 12,
    todaySessions: 4,
    attendanceRate: 92.5,
    pendingAssessments: 18,
    upcomingEvents: 0,
    unpaidInvoices: 6,
    currentMonthPayrollStatus: "NOT_GENERATED",
  },
  todaySessions: [
    {
      id: "session-demo-001",
      title: "Balance Bike Basics",
      sessionDate: today,
      startTime: "09:00:00",
      endTime: "10:00:00",
      locationName: "Main Track",
      classGroupName: "Beginner A",
      coachNames: "Coach Demo",
      status: "SCHEDULED",
      attendanceStatus: "NOT_SUBMITTED",
    },
  ],
  studentsNeedAttention: [
    {
      studentId: "student-demo-001",
      studentName: "Alya Demo",
      nickname: "Alya",
      currentLevel: "BEGINNER",
      reason: "NO_ASSESSMENT_IN_PERIOD",
      lastAttendanceDate: "2026-05-06",
      lastAssessmentDate: null,
    },
  ],
  upcomingEvents: [],
  recentAssessments: [],
};

export const coachDashboardMock: CoachDashboardResponse = {
  coach: {
    id: "coach-demo-001",
    userId: "user-demo-001",
    name: "Coach Demo",
    academyId: ownerDashboardMock.academy.id,
  },
  period,
  summaryCards: {
    todaySessions: 2,
    pendingAttendance: 1,
    pendingAssessments: 5,
    completedAssessmentsThisMonth: 12,
  },
  todaySessions: [
    {
      id: "session-demo-001",
      title: "Balance Bike Basics",
      sessionDate: today,
      startTime: "09:00:00",
      endTime: "10:00:00",
      locationName: "Main Track",
      classGroupName: "Beginner A",
      studentCount: 8,
      attendanceSubmitted: false,
      assessmentCompletedCount: 3,
      status: "SCHEDULED",
    },
  ],
  pendingAssessmentStudents: [
    {
      studentId: "student-demo-001",
      studentName: "Alya Demo",
      nickname: "Alya",
      classSessionId: "session-demo-001",
      sessionDate: today,
      currentLevel: "BEGINNER",
    },
  ],
};

export const parentDashboardMock: ParentDashboardResponse = {
  student: {
    id: "student-demo-001",
    studentNo: "STD-DEMO-001",
    fullName: "Alya Demo",
    nickname: "Alya",
    currentLevel: "BEGINNER",
    academyName: ownerDashboardMock.academy.name,
  },
  period,
  attendanceSummary: {
    totalSessions: 8,
    present: 7,
    absent: 1,
    permit: 0,
    sick: 0,
    late: 0,
    attendanceRate: 87.5,
  },
  latestProgress: {
    latestAssessmentDate: "2026-05-04",
    coachName: "Coach Demo",
    overallNotes: "Good balance improvement.",
    recommendation: null,
  },
  skillProgress: [
    {
      skillCode: "BALANCE",
      skillName: "Balance",
      latestScore: 4,
      averageScore: 3.75,
      previousScore: 3.5,
      trend: "UP",
    },
  ],
  upcomingSessions: [
    {
      sessionId: "session-demo-002",
      sessionDate: "2026-05-10",
      startTime: "09:00:00",
      endTime: "10:00:00",
      locationName: "Main Track",
      classGroupName: "Beginner A",
      status: "SCHEDULED",
    },
  ],
  upcomingEvents: [],
};

export const eventDashboardMock: EventDashboardResponse = {
  period,
  summaryCards: {
    totalEvents: 0,
    upcomingEvents: 0,
    completedEvents: 0,
    totalParticipants: 0,
  },
  upcomingEvents: [],
  recentEvents: [],
};
