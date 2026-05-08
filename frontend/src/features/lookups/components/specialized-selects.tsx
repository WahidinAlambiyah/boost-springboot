"use client";

import {
  useAcademyOptions,
  useAssessmentSkillOptions,
  useClassSessionOptions,
  useCoachOptions,
  useLocationOptions,
  useStudentOptions,
  useTrainingPackageOptions,
} from "@/features/lookups/lookup.hooks";
import { LookupParams } from "@/features/lookups/lookup.types";

import { LookupSelect, LookupSelectProps } from "./lookup-select";

type BaseSelectProps = Omit<LookupSelectProps, "options" | "isLoading" | "error">;

interface AcademySelectProps extends BaseSelectProps {
  params?: LookupParams;
}

interface AcademyScopedSelectProps extends BaseSelectProps {
  academyId?: string;
  params?: Omit<LookupParams, "academyId">;
}

interface StudentSelectProps extends AcademyScopedSelectProps {
  allowAll?: boolean;
}

const getErrorMessage = (error: unknown) => (error instanceof Error ? error.message : "Gagal memuat opsi.");

const mergeAcademyParams = (academyId?: string, params?: Omit<LookupParams, "academyId">): LookupParams | undefined => {
  if (!academyId && !params) {
    return undefined;
  }

  return {
    academyId,
    ...(params ?? {}),
  };
};

export function AcademySelect({ params, disabled, helperText, ...props }: AcademySelectProps) {
  const query = useAcademyOptions(params);

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || query.isLoading}
      helperText={helperText}
    />
  );
}

export function LocationSelect({ academyId, params, disabled, helperText, ...props }: AcademyScopedSelectProps) {
  const isEnabled = Boolean(academyId);
  const query = useLocationOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}

export function CoachSelect({ academyId, params, disabled, helperText, ...props }: AcademyScopedSelectProps) {
  const isEnabled = Boolean(academyId);
  const query = useCoachOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}

export function StudentSelect({ academyId, params, disabled, helperText, allowAll = false, ...props }: StudentSelectProps) {
  const isEnabled = allowAll || Boolean(academyId);
  const query = useStudentOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}

export function ClassSessionSelect({ academyId, params, disabled, helperText, ...props }: AcademyScopedSelectProps) {
  const isEnabled = Boolean(academyId);
  const query = useClassSessionOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}

export function AssessmentSkillSelect({ academyId, params, disabled, helperText, ...props }: AcademyScopedSelectProps) {
  const isEnabled = Boolean(academyId);
  const query = useAssessmentSkillOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}

export function TrainingPackageSelect({ academyId, params, disabled, helperText, ...props }: AcademyScopedSelectProps) {
  const isEnabled = Boolean(academyId);
  const query = useTrainingPackageOptions(mergeAcademyParams(academyId, params), { enabled: isEnabled });
  const fallbackHelperText = isEnabled ? helperText : "Pilih academy terlebih dahulu.";

  return (
    <LookupSelect
      {...props}
      options={query.data ?? []}
      isLoading={query.isLoading}
      error={query.error ? getErrorMessage(query.error) : null}
      disabled={disabled || !isEnabled}
      helperText={fallbackHelperText}
    />
  );
}
