"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import AssessmentTable from "@/features/assessments/components/assessment-table";
import { assessmentService } from "@/features/assessments/assessment.service";
import { classSessionService } from "@/features/class-sessions/class-session.service";
import { studentService } from "@/features/students/student.service";
import { QUERY_KEYS } from "@/lib/query-keys";
import { can } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

export default function AssessmentsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "ASSESSMENT_WRITE"), [authorities]);

  const [studentId, setStudentId] = useState("");
  const [classSessionId, setClassSessionId] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  const filters = useMemo(
    () => ({ studentId: studentId || undefined, classSessionId: classSessionId || undefined, from: from || undefined, to: to || undefined }),
    [classSessionId, from, studentId, to],
  );

  const assessmentsQuery = useQuery({ queryKey: QUERY_KEYS.assessments.filter(filters), queryFn: () => assessmentService.getAssessments(filters) });
  const sessionsQuery = useQuery({ queryKey: QUERY_KEYS.classSessions.list(), queryFn: () => classSessionService.getClassSessions() });
  const studentsQuery = useQuery({ queryKey: QUERY_KEYS.students.list(), queryFn: () => studentService.list(), retry: false });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => assessmentService.deleteAssessment(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessments.all });
    },
  });

  return (
    <RequirePermission permissions="ASSESSMENT_READ">
      <AppShell>
        <div className="flex items-center justify-between gap-3">
          <h1 className="text-2xl font-semibold text-zinc-900">Assessments</h1>
          {canWrite ? <Link href="/assessments/new" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white">Tambah Assessment</Link> : null}
        </div>

        <div className="mt-4 grid grid-cols-1 gap-3 rounded-lg border border-zinc-200 bg-white p-4 md:grid-cols-4">
          <label className="text-sm"><span className="mb-1 block">Student</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" value={studentId} onChange={(event) => setStudentId(event.target.value)}><option value="">Semua Student</option>{studentsQuery.data?.map((student) => <option key={student.id} value={student.id}>{student.fullName}</option>)}</select></label>
          <label className="text-sm"><span className="mb-1 block">Class Session</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" value={classSessionId} onChange={(event) => setClassSessionId(event.target.value)}><option value="">Semua Session</option>{sessionsQuery.data?.map((session) => <option key={session.id} value={session.id}>{session.sessionDate} {session.startTime}-{session.endTime}</option>)}</select></label>
          <label className="text-sm"><span className="mb-1 block">Dari Tanggal</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" value={from} onChange={(event) => setFrom(event.target.value)} /></label>
          <label className="text-sm"><span className="mb-1 block">Sampai Tanggal</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" value={to} onChange={(event) => setTo(event.target.value)} /></label>
        </div>

        <div className="mt-4">
          {assessmentsQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}
          {assessmentsQuery.isError ? <ErrorMessage message="Gagal memuat assessments." /> : null}
          {deleteMutation.isError ? <ErrorMessage message="Gagal menghapus assessment." /> : null}
          {assessmentsQuery.data && assessmentsQuery.data.length === 0 ? <EmptyState title="Belum ada assessment" description="Belum ada data yang sesuai filter." /> : null}
          {assessmentsQuery.data && assessmentsQuery.data.length > 0 ? (
            <AssessmentTable
              assessments={assessmentsQuery.data}
              canWrite={canWrite}
              onEdit={(assessment) => router.push(`/assessments/${assessment.id}/edit`)}
              onDelete={(assessment) => deleteMutation.mutate(assessment.id)}
            />
          ) : null}
        </div>
      </AppShell>
    </RequirePermission>
  );
}
