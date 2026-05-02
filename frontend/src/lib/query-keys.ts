type DateRange = {
  from?: string;
  to?: string;
};

type MonthYear = {
  month?: number;
  year?: number;
};

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
  assessmentSkills: createEntityKeys<{ academyId?: string | number }>("assessmentSkills"),
  assessments: createEntityKeys<{ academyId?: string | number; studentId?: string | number }>(
    "assessments",
  ),
  studentProgressReport: createEntityKeys<{
    academyId?: string | number;
    studentId?: string | number;
  } & DateRange>("studentProgressReport"),
  trainingPackages: createEntityKeys<{ academyId?: string | number }>("trainingPackages"),
  studentPackages: createEntityKeys<{
    academyId?: string | number;
    studentId?: string | number;
  }>("studentPackages"),
  coachPayroll: createEntityKeys<{
    academyId?: string | number;
    coachId?: string | number;
  } & MonthYear>("coachPayroll"),
};
