import { useQuery } from "@tanstack/react-query";

import { lookupService } from "@/features/lookups/lookup.service";
import { LookupParams } from "@/features/lookups/lookup.types";
import { QUERY_KEYS } from "@/lib/query-keys";

interface LookupHookOptions {
  enabled?: boolean;
  staleTime?: number;
}

const DEFAULT_STALE_TIME = 1000 * 60 * 5;

const buildLookupQueryOptions = (options?: LookupHookOptions) => ({
  enabled: options?.enabled,
  staleTime: options?.staleTime ?? DEFAULT_STALE_TIME,
});

export const useAcademyOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.academies(params),
    queryFn: () => lookupService.getAcademies(params),
    ...buildLookupQueryOptions(options),
  });

export const useLocationOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.locations(params),
    queryFn: () => lookupService.getAcademyLocations(params),
    ...buildLookupQueryOptions(options),
  });

export const useCoachOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.coaches(params),
    queryFn: () => lookupService.getCoaches(params),
    ...buildLookupQueryOptions(options),
  });

export const useStudentOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.students(params),
    queryFn: () => lookupService.getStudents(params),
    ...buildLookupQueryOptions(options),
  });

export const useClassSessionOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.classSessions(params),
    queryFn: () => lookupService.getClassSessions(params),
    ...buildLookupQueryOptions(options),
  });

export const useAssessmentSkillOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.assessmentSkills(params),
    queryFn: () => lookupService.getAssessmentSkills(params),
    ...buildLookupQueryOptions(options),
  });

export const useTrainingPackageOptions = (params?: LookupParams, options?: LookupHookOptions) =>
  useQuery({
    queryKey: QUERY_KEYS.lookups.trainingPackages(params),
    queryFn: () => lookupService.getTrainingPackages(params),
    ...buildLookupQueryOptions(options),
  });
