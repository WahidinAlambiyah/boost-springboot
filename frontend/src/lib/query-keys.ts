type DateRange = {
  from?: string;
  to?: string;
};

type MonthYear = {
  month?: number;
  year?: number;
};

type AssessmentSkillFilter = {
  academyId?: string | number;
  active?: boolean;
  search?: string;
};

type AssessmentFilter = {
  academyId?: string | number;
  studentId?: string | number;
  classSessionId?: string | number;
} & DateRange;

type StudentProgressReportFilter = {
  academyId?: string | number;
  studentId?: string | number;
} & DateRange;

const normalizeFilter = <T extends Record<string, unknown>>(filter: T) => {
  const entries = Object.entries(filter).filter(([, value]) => value !== undefined);
  entries.sort(([a], [b]) => a.localeCompare(b));

  return Object.fromEntries(entries) as Partial<T>;
};

const createEntityKeys = <TFilter extends Record<string, unknown>>(entity: string) => ({
  all: [entity] as const,
  list: () => [entity, "list"] as const,
  detail: (id: string | number) => [entity, "detail", id] as const,
  filter: (filter: TFilter) => [entity, "filter", normalizeFilter(filter)] as const,
});

export const QUERY_KEYS = {
  catalog: ["catalog"] as const,
  scheduling: ["scheduling"] as const,
  enrollment: ["enrollment"] as const,
  attendance: ["attendance"] as const,
  billing: ["billing"] as const,
  notification: ["notification"] as const,

  devCrudTrainingCenters: createEntityKeys<{ search?: string; status?: string }>("devCrudTrainingCenters"),

  academies: createEntityKeys<{ academyId?: string | number; search?: string }>("academies"),
  academyLocations: {
    ...createEntityKeys<{ academyId?: string | number }>("academyLocations"),
    byAcademy: (academyId?: string | number) =>
      academyId
        ? (["academyLocations", "byAcademy", academyId] as const)
        : (["academyLocations", "byAcademy", "all"] as const),
  },
  coachProfiles: {
    ...createEntityKeys<{ academyId?: string | number }>("coachProfiles"),
    byAcademy: (academyId?: string | number) =>
      academyId
        ? (["coachProfiles", "byAcademy", academyId] as const)
        : (["coachProfiles", "byAcademy", "all"] as const),
  },
  students: createEntityKeys<{ academyId?: string | number }>("students"),
  classSessions: {
    ...createEntityKeys<{ academyId?: string | number } & DateRange>("classSessions"),
    conflicts: (filter?: { academyId?: string | number } & DateRange) =>
      ["classSessions", "conflicts", normalizeFilter(filter ?? {})] as const,
  },
  classSessionCoaches: {
    ...createEntityKeys<{
      academyId?: string | number;
      sessionId?: string | number;
    }>("classSessionCoaches"),
    bySession: (sessionId: string | number) => ["classSessionCoaches", "bySession", sessionId] as const,
  },
  attendanceSessions: {
    ...createEntityKeys<{
      academyId?: string | number;
      studentId?: string | number;
      sessionId?: string | number;
    }>("attendanceSessions"),
    byClassSession: (classSessionId: string | number) =>
      ["attendanceSessions", "byClassSession", classSessionId] as const,
  },
  assessmentSkills: {
    ...createEntityKeys<AssessmentSkillFilter>("assessmentSkills"),
    byAcademy: (academyId: string | number) => ["assessmentSkills", "byAcademy", academyId] as const,
  },
  assessments: {
    ...createEntityKeys<AssessmentFilter>("assessments"),
    byStudent: (studentId: string | number) => ["assessments", "byStudent", studentId] as const,
    byClassSession: (classSessionId: string | number) =>
      ["assessments", "byClassSession", classSessionId] as const,
    byAcademy: (academyId: string | number) => ["assessments", "byAcademy", academyId] as const,
  },
  studentProgressReport: {
    ...createEntityKeys<StudentProgressReportFilter>("studentProgressReport"),
    byStudent: (studentId: string | number, filter?: Omit<StudentProgressReportFilter, "studentId">) =>
      ["studentProgressReport", "byStudent", studentId, normalizeFilter(filter ?? {})] as const,
  },
  trainingPackages: createEntityKeys<{ academyId?: string | number }>("trainingPackages"),
  studentPackages: {
    ...createEntityKeys<{
      academyId?: string | number;
      studentId?: string | number;
    }>("studentPackages"),
    byStudent: (studentId: string | number) => ["studentPackages", "byStudent", studentId] as const,
  },
  coachPayroll: {
    ...createEntityKeys<{
      academyId?: string | number;
      coachId?: string | number;
    } & MonthYear>("coachPayroll"),
    periodList: (filter?: { academyId?: string | number } & MonthYear) =>
      ["coachPayroll", "periodList", normalizeFilter(filter ?? {})] as const,
    periodDetail: (periodId: string | number) => ["coachPayroll", "periodDetail", periodId] as const,
  },
};
