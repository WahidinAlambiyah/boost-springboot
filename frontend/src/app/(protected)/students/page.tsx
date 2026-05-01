"use client";

import { useMemo, useState } from "react";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import StudentForm from "@/features/students/components/student-form";
import StudentTable from "@/features/students/components/student-table";
import { StudentFormValues } from "@/features/students/student.schema";
import { studentService } from "@/features/students/student.service";
import { Student } from "@/lib/api-types";
import { canAny } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function StudentsPage() {
  const queryClient = useQueryClient();
  const authorities = useAuthStore((state) => state.authorities);
  const [selected, setSelected] = useState<Student | null>(null);
  const [formVersion, setFormVersion] = useState(0);
  const canWrite = useMemo(
    () => canAny(authorities, ["STUDENT_WRITE", "USER_WRITE"]),
    [authorities],
  );

  const studentsQuery = useQuery({
    queryKey: QUERY_KEYS.students.list(),
    queryFn: () => studentService.list(),
    enabled: studentService.isEndpointEnabled,
  });

  const createMutation = useMutation({
    mutationFn: (values: StudentFormValues) => studentService.create(values),
    onSuccess: async () => {
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students.all });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: string; values: StudentFormValues }) =>
      studentService.update(id, values),
    onSuccess: async () => {
      setSelected(null);
      setFormVersion((prev) => prev + 1);
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students.all });
    },
  });

  return (
    <RequirePermission permissions={["STUDENT_READ", "USER_READ"]} mode="any">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Students</h1>

        {!studentService.isEndpointEnabled ? (
          <p className="mt-4 rounded-md border border-zinc-200 bg-zinc-50 p-4 text-sm text-zinc-700">
            Students endpoint belum tersedia
          </p>
        ) : (
          <>
            <div className="mt-6">
              {studentsQuery.isLoading ? <LoadingSkeleton rows={6} /> : null}
              {studentsQuery.isError ? <ErrorMessage message="Gagal memuat data students." /> : null}
              {studentsQuery.data && studentsQuery.data.length === 0 ? <EmptyState title="Belum ada student" description="Silakan tambah student baru." /> : null}
              {studentsQuery.data && studentsQuery.data.length > 0 ? (
                <StudentTable students={studentsQuery.data} canWrite={canWrite} onEdit={setSelected} />
              ) : null}
              {(createMutation.error || updateMutation.error) ? <ErrorMessage className="mt-4" message="Aksi gagal diproses. Coba lagi." /> : null}
            </div>
            <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
              <StudentForm key={`student-form-${selected?.id ?? "new"}-${formVersion}`}
                initialData={selected}
                canWrite={canWrite}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
                onCancelEdit={() => setSelected(null)}
                onSubmit={(values) => {
                  if (selected) {
                    updateMutation.mutate({ id: selected.id, values });
                    return;
                  }
                  createMutation.mutate(values);
                }}
              />
            </div>
          </>
        )}
      </AppShell>
    </RequirePermission>
  );
}
