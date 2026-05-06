"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import CoachPayrollTable from "@/features/payroll/components/coach-payroll-table";
import PayrollActions from "@/features/payroll/components/payroll-actions";
import PayrollPeriodFilter from "@/features/payroll/components/payroll-period-filter";
import { coachPayrollService } from "@/features/payroll/coach-payroll.service";
import { can } from "@/lib/permissions";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

export default function CoachPayrollPage() {
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => can(authorities, "PAYROLL_WRITE"), [authorities]);
  const now = useMemo(() => new Date(), []);
  const [month, setMonth] = useState(now.getUTCMonth() + 1);
  const [year, setYear] = useState(now.getUTCFullYear());
  const [selectedPeriodId, setSelectedPeriodId] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const periodsQuery = useQuery({
    queryKey: QUERY_KEYS.coachPayroll.filter({ month, year }),
    queryFn: () => coachPayrollService.getCoachPayrollByPeriod(month, year),
  });

  const generateMutation = useMutation({
    mutationFn: (payload: { periodMonth: number; periodYear: number }) =>
      coachPayrollService.generateCoachPayroll({ academyId: "", ...payload }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.all });
    },
  });

  const approveMutation = useMutation({
    mutationFn: (payrollPeriodId: string) => coachPayrollService.approveCoachPayroll(payrollPeriodId),
    onSuccess: async (_, payrollPeriodId) => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.all });
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.detail(payrollPeriodId) });
    },
  });

  const markPaidMutation = useMutation({
    mutationFn: (payrollPeriodId: string) => coachPayrollService.markCoachPayrollPaid(payrollPeriodId),
    onSuccess: async (_, payrollPeriodId) => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.all });
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.detail(payrollPeriodId) });
    },
  });

  const errorMessage = periodsQuery.error instanceof Error
    ? `Gagal memuat payroll periode terpilih. ${periodsQuery.error.message}`
    : generateMutation.error instanceof Error
      ? `Gagal generate payroll. ${generateMutation.error.message}`
      : approveMutation.error instanceof Error
        ? `Gagal approve payroll period. ${approveMutation.error.message}`
        : markPaidMutation.error instanceof Error
          ? `Gagal menandai payroll sebagai PAID. ${markPaidMutation.error.message}`
          : undefined;

  return (
    <RequirePermission permissions={["PAYROLL_READ", "PAYROLL_WRITE"]} mode="any">
      <AppShell>
        <PageHeader title="Coach Payroll" description="Kelola payroll coach per periode bulan." />

        {errorMessage ? <ErrorMessage message={errorMessage} className="mb-4" /> : null}

        <div className="mt-4">
          <PayrollPeriodFilter
            initialMonth={month}
            initialYear={year}
            loading={generateMutation.isPending}
            canWrite={canWrite}
            onApply={(nextMonth, nextYear) => {
              setMonth(nextMonth);
              setYear(nextYear);
            }}
            onGenerate={(nextMonth, nextYear) => {
              generateMutation.mutate({ periodMonth: nextMonth, periodYear: nextYear });
            }}
          />
        </div>

        <div className="mt-6">
          {periodsQuery.isLoading ? <LoadingSkeleton rows={5} className="mt-0" /> : null}

          {periodsQuery.data && periodsQuery.data.length === 0 ? (
            <div className="rounded-lg border border-dashed border-zinc-300 bg-white px-4 py-6 text-sm text-zinc-600">
              Belum ada data payroll untuk periode ini. Gunakan tombol <span className="font-medium">Generate</span> untuk membuat data.
            </div>
          ) : null}

          {periodsQuery.data && periodsQuery.data.length > 0 ? (
            <CoachPayrollTable
              periods={periodsQuery.data}
              selectedPeriodId={selectedPeriodId}
              onSelectPeriod={(period) => {
                setSelectedPeriodId(period.id);
              }}
              canWrite={canWrite}
              onEditItem={() => undefined}
            />
          ) : null}
        </div>

        <div className="mt-4">
          <PayrollActions
            canWrite={canWrite}
            disabled={!selectedPeriodId || approveMutation.isPending || markPaidMutation.isPending}
            onApprove={() => {
              if (selectedPeriodId) {
                approveMutation.mutate(selectedPeriodId);
              }
            }}
            onMarkPaid={() => {
              if (selectedPeriodId) {
                markPaidMutation.mutate(selectedPeriodId);
              }
            }}
          />
        </div>

      </AppShell>
    </RequirePermission>
  );
}
