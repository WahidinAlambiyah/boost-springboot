"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import AssessmentTable from "@/features/assessments/components/assessment-table";
import { assessmentService } from "@/features/assessments/assessment.service";
import { QUERY_KEYS } from "@/lib/query-keys";

export default function AssessmentsPage() {
  const router = useRouter();
  const assessmentsQuery = useQuery({ queryKey: QUERY_KEYS.assessments.list(), queryFn: () => assessmentService.getAssessments() });

  return (
    <AppShell>
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold text-zinc-900">Assessments</h1>
        <Link href="/assessments/new" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white">Tambah Assessment</Link>
      </div>

      <div className="mt-4">
        {assessmentsQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}
        {assessmentsQuery.isError ? <ErrorMessage message="Gagal memuat assessments." /> : null}
        {assessmentsQuery.data && assessmentsQuery.data.length === 0 ? <EmptyState title="Belum ada assessment" description="Klik tombol tambah assessment." /> : null}
        {assessmentsQuery.data && assessmentsQuery.data.length > 0 ? (
          <AssessmentTable assessments={assessmentsQuery.data} canWrite onEdit={(assessment) => router.push(`/assessments/new?id=${assessment.id}`)} />
        ) : null}
      </div>
    </AppShell>
  );
}
