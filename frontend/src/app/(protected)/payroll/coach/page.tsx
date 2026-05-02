"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
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

  return (
    <RequirePermission permissions={["PAYROLL_READ", "PAYROLL_WRITE"]} mode="any">
      <AppShell>
        <h1 className="text-2xl font-semibold text-zinc-900">Coach Payroll</h1>
        <p className="mt-2 text-zinc-600">Kelola payroll coach per periode bulan.</p>

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
          {periodsQuery.data ? (
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
              if (selectedPeriodId && window.confirm("Approve period payroll ini?")) {
                approveMutation.mutate(selectedPeriodId);
              }
            }}
            onMarkPaid={() => {
              if (selectedPeriodId && window.confirm("Mark payroll period ini sebagai PAID?")) {
                markPaidMutation.mutate(selectedPeriodId);
              }
            }}
          />
        </div>
      </AppShell>
    </RequirePermission>
  );
}
