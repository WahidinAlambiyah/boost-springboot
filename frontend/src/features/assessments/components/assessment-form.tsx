"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { AssessmentSkill, ClassSession, CoachProfile, Student, StudentAssessment } from "@/lib/api-types";
import { AssessmentFormValues, assessmentFormSchema } from "@/features/assessments/assessment.schema";
import SkillScoreInput from "@/features/assessments/components/skill-score-input";

interface AssessmentFormProps {
  initialData?: StudentAssessment;
  skills: AssessmentSkill[];
  classSessions: ClassSession[];
  students: Student[];
  coaches: CoachProfile[];
  isSubmitting?: boolean;
  canWrite?: boolean;
  onSubmit: (values: AssessmentFormValues) => void;
}

export default function AssessmentForm({
  initialData,
  skills,
  classSessions,
  students,
  coaches,
  isSubmitting,
  canWrite = false,
  onSubmit,
}: AssessmentFormProps) {
  const form = useForm<AssessmentFormValues>({
    resolver: zodResolver(assessmentFormSchema),
    defaultValues: {
      classSessionId: "",
      studentId: "",
      coachId: "",
      assessedAt: new Date().toISOString(),
      notes: "",
      scores: [],
    },
  });

  useEffect(() => {
    const baseScores = skills.map((skill) => {
      const found = initialData?.scores.find((score) => score.skillCode === skill.code);
      return { skillCode: skill.code, score: found?.score ?? 1, notes: found?.notes ?? "" };
    });

    form.reset({
      classSessionId: initialData?.classSessionId ?? "",
      studentId: initialData?.studentId ?? "",
      coachId: initialData?.coachId ?? "",
      assessedAt: initialData?.assessedAt ?? new Date().toISOString(),
      notes: initialData?.notes ?? "",
      scores: baseScores,
    });
  }, [form, initialData, skills]);

  const scores = form.watch("scores");

  return (
    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
        <label className="text-sm"><span className="mb-1 block">Class Session</span><select disabled={!canWrite || isSubmitting} className="w-full rounded-md border border-zinc-300 px-3 py-2" {...form.register("classSessionId")}><option value="">Pilih Session</option>{classSessions.map((session) => <option key={session.id} value={session.id}>{session.sessionDate} {session.startTime}-{session.endTime}</option>)}</select></label>
        <label className="text-sm"><span className="mb-1 block">Student</span><select disabled={!canWrite || isSubmitting} className="w-full rounded-md border border-zinc-300 px-3 py-2" {...form.register("studentId")}><option value="">Pilih Student</option>{students.map((student) => <option key={student.id} value={student.id}>{student.fullName}</option>)}</select></label>
        <label className="text-sm"><span className="mb-1 block">Coach</span><select disabled={!canWrite || isSubmitting} className="w-full rounded-md border border-zinc-300 px-3 py-2" {...form.register("coachId")}><option value="">Pilih Coach</option>{coaches.map((coach) => <option key={coach.id} value={coach.id}>{coach.fullName}</option>)}</select></label>
      </div>

      <label className="block text-sm"><span className="mb-1 block">Overall Notes / Recommendation</span><textarea disabled={!canWrite || isSubmitting} rows={3} className="w-full rounded-md border border-zinc-300 px-3 py-2" placeholder="Isi overall notes dan recommendation" {...form.register("notes")} /></label>

      <div className="space-y-3">
        {skills.map((skill, index) => (
          <SkillScoreInput
            key={skill.id}
            label={`${skill.name} (${skill.code})`}
            maxScore={skill.maxScore}
            value={scores?.[index]?.score ?? 1}
            notes={scores?.[index]?.notes ?? ""}
            disabled={!canWrite || isSubmitting}
            onChange={(score) => form.setValue(`scores.${index}.score`, score, { shouldValidate: true })}
            onNotesChange={(notes) => form.setValue(`scores.${index}.notes`, notes)}
          />
        ))}
      </div>

      {canWrite ? <button type="submit" disabled={isSubmitting} className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50">
        {isSubmitting ? "Menyimpan..." : initialData ? "Update Assessment" : "Simpan Assessment"}
      </button> : null}
    </form>
  );
}
