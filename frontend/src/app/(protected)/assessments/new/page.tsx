"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import AssessmentForm from "@/features/assessments/components/assessment-form";
import { AssessmentFormValues } from "@/features/assessments/assessment.schema";
import { assessmentService } from "@/features/assessments/assessment.service";
import { assessmentSkillService } from "@/features/assessment-skills/assessment-skill.service";
import { classSessionService } from "@/features/class-sessions/class-session.service";
import { coachProfileService } from "@/features/coach-profiles/coach-profile.service";
import { studentService } from "@/features/students/student.service";
import { QUERY_KEYS } from "@/lib/query-keys";

export default function NewAssessmentPage() {
  const router = useRouter();
  const params = useSearchParams();
  const queryClient = useQueryClient();
  const assessmentId = params.get("id");

  const skillsQuery = useQuery({ queryKey: QUERY_KEYS.assessmentSkills.list(), queryFn: () => assessmentSkillService.list() });
  const sessionsQuery = useQuery({ queryKey: QUERY_KEYS.classSessions.list(), queryFn: () => classSessionService.getClassSessions() });
  const studentsQuery = useQuery({ queryKey: QUERY_KEYS.students.list(), queryFn: () => studentService.list(), retry: false });
  const coachesQuery = useQuery({ queryKey: QUERY_KEYS.coachProfiles.list(), queryFn: () => coachProfileService.list() });
  const detailQuery = useQuery({ queryKey: QUERY_KEYS.assessments.detail(assessmentId ?? "new"), queryFn: () => assessmentService.getAssessmentById(assessmentId!), enabled: Boolean(assessmentId) });

  const mutation = useMutation({
    mutationFn: async (values: AssessmentFormValues) => {
      if (assessmentId) return assessmentService.updateAssessment(assessmentId, values);
      return assessmentService.createAssessment(values);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessments.all });
      router.push("/assessments");
    },
  });

  if (skillsQuery.isLoading || sessionsQuery.isLoading || studentsQuery.isLoading || coachesQuery.isLoading || detailQuery.isLoading) {
    return <AppShell><LoadingSkeleton rows={8} /></AppShell>;
  }

  if (skillsQuery.isError || sessionsQuery.isError || studentsQuery.isError || coachesQuery.isError || detailQuery.isError) {
    return <AppShell><ErrorMessage message="Gagal memuat data form assessment." /></AppShell>;
  }

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">{assessmentId ? "Edit Assessment" : "Assessment Baru"}</h1>
      <div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4">
        <AssessmentForm
          initialData={detailQuery.data}
          skills={skillsQuery.data ?? []}
          classSessions={sessionsQuery.data ?? []}
          students={studentsQuery.data ?? []}
          coaches={coachesQuery.data ?? []}
          isSubmitting={mutation.isPending}
          onSubmit={(values) => mutation.mutate(values)}
        />
      </div>
    </AppShell>
  );
}
