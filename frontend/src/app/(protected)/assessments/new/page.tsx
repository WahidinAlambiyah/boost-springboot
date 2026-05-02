"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
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
  const queryClient = useQueryClient();

  const skillsQuery = useQuery({ queryKey: QUERY_KEYS.assessmentSkills.list(), queryFn: () => assessmentSkillService.list() });
  const sessionsQuery = useQuery({ queryKey: QUERY_KEYS.classSessions.list(), queryFn: () => classSessionService.getClassSessions() });
  const studentsQuery = useQuery({ queryKey: QUERY_KEYS.students.list(), queryFn: () => studentService.list(), retry: false });
  const coachesQuery = useQuery({ queryKey: QUERY_KEYS.coachProfiles.list(), queryFn: () => coachProfileService.list() });

  const mutation = useMutation({
    mutationFn: (values: AssessmentFormValues) => assessmentService.createAssessment(values),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessments.all });
      router.push("/assessments");
    },
  });

  if (skillsQuery.isLoading || sessionsQuery.isLoading || studentsQuery.isLoading || coachesQuery.isLoading) {
    return <AppShell><LoadingSkeleton rows={8} /></AppShell>;
  }

  if (skillsQuery.isError || sessionsQuery.isError || studentsQuery.isError || coachesQuery.isError) {
    return <AppShell><ErrorMessage message="Gagal memuat data form assessment." /></AppShell>;
  }

  return (
    <RequirePermission permissions="ASSESSMENT_WRITE">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Assessment Baru</h1>
        <div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4">
          {mutation.isError ? <ErrorMessage message="Gagal menyimpan assessment. Coba lagi." /> : null}
          <AssessmentForm
            skills={skillsQuery.data ?? []}
            classSessions={sessionsQuery.data ?? []}
            students={studentsQuery.data ?? []}
            coaches={coachesQuery.data ?? []}
            isSubmitting={mutation.isPending}
            canWrite
            onSubmit={(values) => mutation.mutate(values)}
          />
        </div>
      </AppShell>
    </RequirePermission>
  );
}
