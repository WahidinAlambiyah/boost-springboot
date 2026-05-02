"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import { academyService } from "@/features/academies/academy.service";
import TrainingPackageForm from "@/features/training-packages/components/training-package-form";
import TrainingPackageTable from "@/features/training-packages/components/training-package-table";
import { TrainingPackageFormSubmitValues } from "@/features/training-packages/training-package.schema";
import { trainingPackageService } from "@/features/training-packages/training-package.service";
import { TrainingPackage } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function TrainingPackagesPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const [academyId, setAcademyId] = useState("");
  const [selectedPackage, setSelectedPackage] = useState<TrainingPackage | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const canWrite = useMemo(() => can(authorities, "PACKAGE_WRITE"), [authorities]);

  const academiesQuery = useQuery({ queryKey: QUERY_KEYS.academies.list(), queryFn: () => academyService.list() });
  const packagesQuery = useQuery({ queryKey: QUERY_KEYS.trainingPackages.filter({ academyId: academyId || undefined }), queryFn: () => trainingPackageService.getTrainingPackages({ academyId: academyId || undefined }) });

  const invalidate = async () => queryClient.invalidateQueries({ queryKey: QUERY_KEYS.trainingPackages.all });
  const createMutation = useMutation({ mutationFn: (values: TrainingPackageFormSubmitValues) => trainingPackageService.createTrainingPackage(values), onSuccess: async () => { setFormVersion((prev) => prev + 1); await invalidate(); } });
  const updateMutation = useMutation({ mutationFn: ({ id, values }: { id: string; values: TrainingPackageFormSubmitValues }) => trainingPackageService.updateTrainingPackage(id, values), onSuccess: async () => { setSelectedPackage(null); setFormVersion((prev) => prev + 1); await invalidate(); } });
  const deleteMutation = useMutation({ mutationFn: (id: string) => trainingPackageService.deleteTrainingPackage(id), onSuccess: invalidate });

  return <RequirePermission permissions="PACKAGE_READ"><AppShell><h1 className="text-2xl font-semibold text-zinc-900">Training Packages</h1><div className="mt-4 rounded-lg border border-zinc-200 bg-white p-4"><label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Filter Academy</span><select value={academyId} onChange={(event) => setAcademyId(event.target.value)} className="w-full rounded-md border border-zinc-300 px-3 py-2"><option value="">Semua academy</option>{academiesQuery.data?.map((academy) => <option key={academy.id} value={academy.id}>{academy.name}</option>)}</select></label></div><div className="mt-6">{packagesQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}{packagesQuery.isError ? <ErrorMessage message="Gagal memuat training packages." /> : null}{packagesQuery.data && packagesQuery.data.length === 0 ? <EmptyState title="Belum ada paket" description="Silakan tambah paket training baru." /> : null}{packagesQuery.data && packagesQuery.data.length > 0 ? <TrainingPackageTable packages={packagesQuery.data} canWrite={canWrite} onEdit={setSelectedPackage} onDelete={(pkg) => deleteMutation.mutate(pkg.id)} /> : null}{(createMutation.error || updateMutation.error || deleteMutation.error) ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}</div><div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4"><h2 className="text-lg font-semibold text-zinc-900">{selectedPackage ? "Edit Paket" : "Tambah Paket"}</h2><div className="mt-3"><TrainingPackageForm key={`training-package-form-${selectedPackage?.id ?? "new"}-${formVersion}`} academies={academiesQuery.data ?? []} initialData={selectedPackage} canWrite={canWrite} isSubmitting={createMutation.isPending || updateMutation.isPending} onCancelEdit={() => setSelectedPackage(null)} onSubmit={(values) => selectedPackage ? updateMutation.mutate({ id: selectedPackage.id, values }) : createMutation.mutate(values)} /></div></div></AppShell></RequirePermission>;
}
