import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

import { LookupOption, LookupParams } from "@/features/lookups/lookup.types";

const LOOKUP_BASE_PATH = "/api/lookups";

const normalizeLookupParams = (params?: LookupParams): LookupParams | undefined => {
  if (!params) {
    return undefined;
  }

  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== "");
  if (entries.length === 0) {
    return undefined;
  }

  return Object.fromEntries(entries) as LookupParams;
};

const fetchLookupOptions = async (endpoint: string, params?: LookupParams): Promise<LookupOption[]> => {
  const response = await api.get<ApiResponse<LookupOption[]>>(`${LOOKUP_BASE_PATH}/${endpoint}`, {
    params: normalizeLookupParams(params),
  });
  return response.data.data;
};

export const lookupService = {
  getAcademies(params?: LookupParams) {
    return fetchLookupOptions("academies", params);
  },

  getAcademyLocations(params?: LookupParams) {
    return fetchLookupOptions("academy-locations", params);
  },

  getCoaches(params?: LookupParams) {
    return fetchLookupOptions("coaches", params);
  },

  getStudents(params?: LookupParams) {
    return fetchLookupOptions("students", params);
  },

  getClassGroups(params?: LookupParams) {
    return fetchLookupOptions("class-groups", params);
  },

  getClassSessions(params?: LookupParams) {
    return fetchLookupOptions("class-sessions", params);
  },

  getAssessmentSkills(params?: LookupParams) {
    return fetchLookupOptions("assessment-skills", params);
  },

  getTrainingPackages(params?: LookupParams) {
    return fetchLookupOptions("training-packages", params);
  },

  getEvents(params?: LookupParams) {
    return fetchLookupOptions("events", params);
  },
};
