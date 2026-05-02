"use client";

import { useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import StudentPackageForm from "@/features/student-packages/components/student-package-form";
import StudentPackageTable from "@/features/student-packages/components/student-package-table";
import { StudentPackageFormSubmitValues } from "@/features/student-packages/student-package.schema";
import { studentPackageService, StudentPackage } from "@/features/student-packages/student-package.service";
import { trainingPackageService } from "@/features/training-packages/training-package.service";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function StudentPackagesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "PACKAGE_WRITE"), [authorities]);
  const [selectedSubscription, setSelectedSubscription] = useState<StudentPackage | null>(null);

  const params = useParams<{ studentId: string }>();
  const studentId = params.studentId;

  const packagesQuery = useQuery({
    queryKey: QUERY_KEYS.studentPackages.filter({ studentId }),
    queryFn: () => studentPackageService.getStudentPackages(studentId),
    enabled: Boolean(studentId),
  });

  const trainingPackagesQuery = useQuery({
    queryKey: QUERY_KEYS.trainingPackages.list(),
    queryFn: () => trainingPackageService.getTrainingPackages(),
  });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: QUERY_KEYS.studentPackages.all });

  const createMutation = useMutation({
    mutationFn: (values: StudentPackageFormSubmitValues) => studentPackageService.createStudentPackage(studentId, values),
    onSuccess: invalidate,
  });

  const updateMutation = useMutation({
    mutationFn: ({ subscriptionId, values }: { subscriptionId: string; values: StudentPackageFormSubmitValues }) => studentPackageService.updateStudentPackage(studentId, subscriptionId, values),
    onSuccess: async () => {
      setSelectedSubscription(null);
      await invalidate();
    },
  });

  return <RequirePermission permissions="PACKAGE_READ"><AppShell><h1 className="text-2xl font-semibold text-zinc-900">Student Packages</h1><p className="mt-1 text-sm text-zinc-600">Kelola assign package dan status subscription murid.</p><div className="mt-6">{packagesQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}{packagesQuery.isError ? <ErrorMessage message="Gagal memuat paket murid." /> : null}{packagesQuery.data && packagesQuery.data.length === 0 ? <EmptyState title="Belum ada package" description="Assign package pertama untuk murid ini." /> : null}{packagesQuery.data && packagesQuery.data.length > 0 ? <StudentPackageTable packages={packagesQuery.data} canWrite={canWrite} onEdit={setSelectedSubscription} /> : null}{(createMutation.error || updateMutation.error) ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}</div><div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4"><h2 className="text-lg font-semibold text-zinc-900">{selectedSubscription ? "Update Subscription" : "Assign Package"}</h2><div className="mt-3"><StudentPackageForm packages={trainingPackagesQuery.data ?? []} initialData={selectedSubscription} canWrite={canWrite} isSubmitting={createMutation.isPending || updateMutation.isPending} onCancelEdit={() => setSelectedSubscription(null)} onSubmit={(values) => {
    if (selectedSubscription) {
      updateMutation.mutate({ subscriptionId: selectedSubscription.id, values });
      return;
    }
    createMutation.mutate(values);
  }} /></div></div></AppShell></RequirePermission>;
}
