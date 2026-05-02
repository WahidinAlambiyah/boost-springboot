"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import CoachPayrollTable from "@/features/payroll/components/coach-payroll-table";
import PayrollActions from "@/features/payroll/components/payroll-actions";
import PayrollPeriodFilter from "@/features/payroll/components/payroll-period-filter";
import { coachPayrollService } from "@/features/payroll/coach-payroll.service";
import { QUERY_KEYS } from "@/lib/query-keys";

export default function CoachPayrollPage() {
  const now = useMemo(() => new Date(), []);
  const [month, setMonth] = useState(now.getUTCMonth() + 1);
  const [year, setYear] = useState(now.getUTCFullYear());
  const [selectedPeriodId, setSelectedPeriodId] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const periodsQuery = useQuery({
    queryKey: QUERY_KEYS.coachPayroll.filter({ month, year }),
    queryFn: () => coachPayrollService.getCoachPayrollByPeriod(month, year),
  });

  const approveMutation = useMutation({
    mutationFn: (payrollPeriodId: string) => coachPayrollService.approveCoachPayroll(payrollPeriodId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.all });
    },
  });

  const markPaidMutation = useMutation({
    mutationFn: (payrollPeriodId: string) => coachPayrollService.markCoachPayrollPaid(payrollPeriodId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: QUERY_KEYS.coachPayroll.all });
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
            onApply={(nextMonth, nextYear) => {
              setMonth(nextMonth);
              setYear(nextYear);
            }}
          />
        </div>

        <div className="mt-6">
          {periodsQuery.data ? (
            <CoachPayrollTable
              periods={periodsQuery.data}
              onSelectPeriod={(period) => {
                setSelectedPeriodId(period.id);
              }}
            />
          ) : null}
        </div>

        <div className="mt-4">
          <PayrollActions
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
