"use client";

import { useMemo, useState } from "react";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import { PageHeader } from "@/app/components/page-header";
import { SectionCard } from "@/app/components/section-card";
import { DEV_TOOLS_READ_PERMISSIONS } from "@/app/dev/permissions";
import {
  AcademySelect,
  AssessmentSkillSelect,
  ClassSessionSelect,
  CoachSelect,
  LocationSelect,
  StudentSelect,
  TrainingPackageSelect,
} from "@/features/lookups/components/specialized-selects";
import { LookupParams } from "@/features/lookups/lookup.types";

const inputClassName =
  "min-h-12 w-full rounded-md border border-zinc-300 bg-white px-3 py-3 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900";

const EMPTY_VALUE = "";

export default function LookupDemoPage() {
  const [academyId, setAcademyId] = useState(EMPTY_VALUE);
  const [locationId, setLocationId] = useState(EMPTY_VALUE);
  const [coachId, setCoachId] = useState(EMPTY_VALUE);
  const [studentId, setStudentId] = useState(EMPTY_VALUE);
  const [classSessionId, setClassSessionId] = useState(EMPTY_VALUE);
  const [assessmentSkillId, setAssessmentSkillId] = useState(EMPTY_VALUE);
  const [trainingPackageId, setTrainingPackageId] = useState(EMPTY_VALUE);

  const [search, setSearch] = useState("");
  const [studentStatus, setStudentStatus] = useState(EMPTY_VALUE);
  const [studentLevel, setStudentLevel] = useState(EMPTY_VALUE);
  const [sessionDate, setSessionDate] = useState(EMPTY_VALUE);
  const [sessionStatus, setSessionStatus] = useState(EMPTY_VALUE);

  const normalizedSearch = search.trim();

  const academyParams = useMemo<LookupParams>(
    () => ({
      search: normalizedSearch || undefined,
      limit: 100,
    }),
    [normalizedSearch],
  );

  const academyScopedParams = useMemo<LookupParams>(
    () => ({
      search: normalizedSearch || undefined,
      limit: 100,
    }),
    [normalizedSearch],
  );

  const studentParams = useMemo<LookupParams>(
    () => ({
      ...academyScopedParams,
      status: studentStatus || undefined,
      level: studentLevel || undefined,
    }),
    [academyScopedParams, studentLevel, studentStatus],
  );

  const classSessionParams = useMemo<LookupParams>(
    () => ({
      ...academyScopedParams,
      date: sessionDate || undefined,
      status: sessionStatus || undefined,
    }),
    [academyScopedParams, sessionDate, sessionStatus],
  );

  const selectedPayload = useMemo(
    () => ({
      academyId: academyId || null,
      locationId: locationId || null,
      coachId: coachId || null,
      studentId: studentId || null,
      classSessionId: classSessionId || null,
      assessmentSkillId: assessmentSkillId || null,
      trainingPackageId: trainingPackageId || null,
      filters: {
        search: normalizedSearch || null,
        studentStatus: studentStatus || null,
        studentLevel: studentLevel || null,
        sessionDate: sessionDate || null,
        sessionStatus: sessionStatus || null,
      },
    }),
    [
      academyId,
      assessmentSkillId,
      classSessionId,
      coachId,
      locationId,
      normalizedSearch,
      sessionDate,
      sessionStatus,
      studentId,
      studentLevel,
      studentStatus,
      trainingPackageId,
    ],
  );

  const resetDependentSelections = () => {
    setLocationId(EMPTY_VALUE);
    setCoachId(EMPTY_VALUE);
    setStudentId(EMPTY_VALUE);
    setClassSessionId(EMPTY_VALUE);
    setAssessmentSkillId(EMPTY_VALUE);
    setTrainingPackageId(EMPTY_VALUE);
  };

  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev Lookup Demo"
          description="Playground internal untuk uji endpoint lookup, React Query hook, dan reusable select sebelum dipakai di form CRUD/report."
        />

        <div className="space-y-6">
          <SectionCard
            title="Filter Query"
            description="Filter ini dipakai untuk request lookup agar kita bisa verifikasi kombinasi academy/search/status/date."
          >
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
              <label className="block space-y-1.5">
                <span className="text-sm font-medium text-zinc-800">Search</span>
                <input
                  type="text"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Cari label..."
                  className={inputClassName}
                />
              </label>

              <label className="block space-y-1.5">
                <span className="text-sm font-medium text-zinc-800">Student Status</span>
                <select
                  value={studentStatus}
                  onChange={(event) => setStudentStatus(event.target.value)}
                  className={inputClassName}
                >
                  <option value="">Semua status</option>
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                  <option value="SUSPENDED">SUSPENDED</option>
                </select>
              </label>

              <label className="block space-y-1.5">
                <span className="text-sm font-medium text-zinc-800">Student Level</span>
                <input
                  type="text"
                  value={studentLevel}
                  onChange={(event) => setStudentLevel(event.target.value)}
                  placeholder="BEGINNER / INTERMEDIATE"
                  className={inputClassName}
                />
              </label>

              <label className="block space-y-1.5">
                <span className="text-sm font-medium text-zinc-800">Session Date</span>
                <input
                  type="date"
                  value={sessionDate}
                  onChange={(event) => setSessionDate(event.target.value)}
                  className={inputClassName}
                />
              </label>

              <label className="block space-y-1.5">
                <span className="text-sm font-medium text-zinc-800">Session Status</span>
                <input
                  type="text"
                  value={sessionStatus}
                  onChange={(event) => setSessionStatus(event.target.value)}
                  placeholder="SCHEDULED / DONE"
                  className={inputClassName}
                />
              </label>
            </div>
          </SectionCard>

          <SectionCard
            title="Lookup Selects"
            description="Semua select ini memakai endpoint `/api/lookups/*` via service + hook React Query."
          >
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <AcademySelect
                label="Academy"
                value={academyId}
                onChange={(value) => {
                  setAcademyId(value);
                  resetDependentSelections();
                }}
                params={academyParams}
                placeholder="Pilih academy"
                required
              />

              <LocationSelect
                label="Location"
                value={locationId}
                onChange={setLocationId}
                academyId={academyId || undefined}
                params={academyScopedParams}
                placeholder="Pilih location"
              />

              <CoachSelect
                label="Coach"
                value={coachId}
                onChange={setCoachId}
                academyId={academyId || undefined}
                params={academyScopedParams}
                placeholder="Pilih coach"
              />

              <StudentSelect
                label="Student"
                value={studentId}
                onChange={setStudentId}
                academyId={academyId || undefined}
                params={studentParams}
                placeholder="Pilih student"
              />

              <ClassSessionSelect
                label="Class Session"
                value={classSessionId}
                onChange={setClassSessionId}
                academyId={academyId || undefined}
                params={classSessionParams}
                placeholder="Pilih class session"
              />

              <AssessmentSkillSelect
                label="Assessment Skill"
                value={assessmentSkillId}
                onChange={setAssessmentSkillId}
                academyId={academyId || undefined}
                params={academyScopedParams}
                placeholder="Pilih assessment skill"
              />

              <TrainingPackageSelect
                label="Training Package"
                value={trainingPackageId}
                onChange={setTrainingPackageId}
                academyId={academyId || undefined}
                params={academyScopedParams}
                placeholder="Pilih training package"
              />
            </div>
          </SectionCard>

          <SectionCard title="Selected Value Preview" description="Payload dummy untuk memudahkan copy referensi saat implementasi form CRUD/report.">
            <pre className="overflow-auto rounded-md bg-zinc-950 p-4 text-xs text-zinc-100">
              {JSON.stringify(selectedPayload, null, 2)}
            </pre>
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
