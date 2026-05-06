"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ConfirmDialog } from "@/app/components/confirm-dialog";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import RequirePermission from "@/app/components/require-permission";
import AssessmentSkillForm from "@/features/assessment-skills/components/assessment-skill-form";
import { AssessmentSkillFormValues } from "@/features/assessment-skills/assessment-skill.schema";
import { assessmentSkillService } from "@/features/assessment-skills/assessment-skill.service";
import { AssessmentSkill } from "@/lib/api-types";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function AssessmentSkillsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const [selectedSkill, setSelectedSkill] = useState<AssessmentSkill | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const canWrite = useMemo(() => can(authorities, "ASSESSMENT_WRITE"), [authorities]);

  const skillsQuery = useQuery({
    queryKey: QUERY_KEYS.assessmentSkills.list(),
    queryFn: () => assessmentSkillService.list(),
  });

  const createMutation = useMutation({
    mutationFn: (values: AssessmentSkillFormValues) =>
      assessmentSkillService.create({
        code: values.code,
        name: values.name,
        description: values.description || undefined,
        maxScore: values.maxScore,
        orderNo: values.orderNo,
        active: values.isActive,
      }),
    onSuccess: async () => {
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessmentSkills.all });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: AssessmentSkillFormValues }) =>
      assessmentSkillService.update(id, {
        name: values.name,
        description: values.description || undefined,
        maxScore: values.maxScore,
        orderNo: values.orderNo,
        active: values.isActive,
      }),
    onSuccess: async () => {
      setSelectedSkill(null);
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessmentSkills.all });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => assessmentSkillService.remove(id),
    onSuccess: async () => {
      if (selectedSkill) {
        setSelectedSkill(null);
      }
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.assessmentSkills.all });
    },
  });

  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const mutationError = createMutation.error || updateMutation.error || deleteMutation.error;

  return (
    <RequirePermission permissions="ASSESSMENT_READ">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Assessment Skills</h1>

        <div className="mt-6">
          {skillsQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}
          {skillsQuery.isError ? <ErrorMessage className="mt-4" message="Gagal memuat data assessment skills." /> : null}
          {skillsQuery.data && skillsQuery.data.length === 0 ? <EmptyState title="Belum ada skill" description="Silakan tambah assessment skill baru." /> : null}
          {skillsQuery.data && skillsQuery.data.length > 0 ? (
            <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
              <table className="min-w-full divide-y divide-zinc-200 text-sm">
                <thead className="bg-zinc-50 text-left text-zinc-600">
                  <tr>
                    <th className="px-4 py-3 font-medium">Code</th><th className="px-4 py-3 font-medium">Name</th><th className="px-4 py-3 font-medium">Max</th><th className="px-4 py-3 font-medium">Order</th><th className="px-4 py-3 font-medium">Status</th><th className="px-4 py-3 font-medium">Aksi</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {skillsQuery.data.map((skill) => (
                    <tr key={skill.id} className="hover:bg-zinc-50/70">
                      <td className="px-4 py-3 font-medium text-zinc-900">{skill.code}</td>
                      <td className="px-4 py-3 text-zinc-700">{skill.name}</td>
                      <td className="px-4 py-3 text-zinc-700">{skill.maxScore}</td>
                      <td className="px-4 py-3 text-zinc-700">{skill.orderNo ?? "-"}</td>
                      <td className="px-4 py-3 text-zinc-700">{skill.active ? "Active" : "Inactive"}</td>
                      <td className="px-4 py-3">
                        {canWrite ? (
                          <div className="flex gap-2">
                            <button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => setSelectedSkill(skill)}>Edit</button>
                            <ConfirmDialog options={{ title: "Hapus skill?", description: `Skill ${skill.name} akan dihapus permanen.`, confirmText: "Hapus" }} onConfirm={async () => { await deleteMutation.mutateAsync(skill.id); }}>
                              {(open) => (
                                <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={() => void open()}>
                                  Hapus
                                </button>
                              )}
                            </ConfirmDialog>
                          </div>
                        ) : (
                          <span className="text-zinc-400">-</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
          {mutationError ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}
        </div>

        <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="text-lg font-semibold text-zinc-900">{selectedSkill ? "Edit Skill" : "Tambah Skill"}</h2>
          <div className="mt-3">
            <AssessmentSkillForm
              key={`assessment-skill-form-${selectedSkill?.id ?? "new"}-${formVersion}`}
              initialData={selectedSkill}
              canWrite={canWrite}
              isSubmitting={isSubmitting}
              onCancelEdit={() => setSelectedSkill(null)}
              onSubmit={(values) => {
                if (selectedSkill) {
                  updateMutation.mutate({ id: selectedSkill.id, values });
                  return;
                }
                createMutation.mutate(values);
              }}
            />
          </div>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
