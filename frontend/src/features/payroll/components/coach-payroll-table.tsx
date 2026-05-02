"use client";

import { CoachPayrollItem, CoachPayrollPeriod } from "@/lib/api-types";
import { formatCurrencyIDR } from "@/lib/formatters";

interface CoachPayrollTableProps {
  periods: CoachPayrollPeriod[];
  selectedPeriodId?: string | null;
  onSelectPeriod: (period: CoachPayrollPeriod) => void;
  onEditItem: (item: CoachPayrollItem, payload: { bonusAmount: number; deductionAmount: number; notes?: string }) => void;
}

export default function CoachPayrollTable({ periods, selectedPeriodId, onSelectPeriod, onEditItem }: CoachPayrollTableProps) {
  const selectedPeriod = periods.find((period) => period.id === selectedPeriodId) ?? periods[0];

  return (
    <div className="space-y-4">
      <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-100 text-zinc-700">
            <tr>
              <th className="px-4 py-2">Periode</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Jumlah Item</th>
              <th className="px-4 py-2">Aksi</th>
            </tr>
          </thead>
          <tbody>
            {periods.map((period) => (
              <tr key={period.id} className="border-t border-zinc-200 text-zinc-800">
                <td className="px-4 py-2">{`${period.periodMonth}/${period.periodYear}`}</td>
                <td className="px-4 py-2">{period.status}</td>
                <td className="px-4 py-2">{period.items.length}</td>
                <td className="px-4 py-2">
                  <button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onSelectPeriod(period)}>
                    Detail
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedPeriod ? (
        <div className="rounded-lg border border-zinc-200 bg-white p-4">
          <div className="mb-3 text-sm font-medium text-zinc-800">Status payroll period: {selectedPeriod.status}</div>
          <div className="overflow-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-zinc-100 text-zinc-700">
                <tr>
                  <th className="px-3 py-2">Coach</th>
                  <th className="px-3 py-2">Session (T/H/A)</th>
                  <th className="px-3 py-2">Base Salary</th>
                  <th className="px-3 py-2">Bonus</th>
                  <th className="px-3 py-2">Deduction</th>
                  <th className="px-3 py-2">Total</th>
                  <th className="px-3 py-2">Notes</th>
                  <th className="px-3 py-2">Aksi</th>
                </tr>
              </thead>
              <tbody>
                {selectedPeriod.items.map((item) => {
                  const disableEdit = selectedPeriod.status === "PAID";
                  return (
                    <tr key={item.id} className="border-t border-zinc-200">
                      <td className="px-3 py-2">{item.coachName ?? item.coachId}</td>
                      <td className="px-3 py-2">{`${item.totalSessions ?? 0}/${item.presentSessions ?? 0}/${item.absentSessions ?? 0}`}</td>
                      <td className="px-3 py-2">{formatCurrencyIDR(item.baseAmount)}</td>
                      <td className="px-3 py-2">{formatCurrencyIDR(item.bonusAmount)}</td>
                      <td className="px-3 py-2">{formatCurrencyIDR(item.deductionAmount)}</td>
                      <td className="px-3 py-2">{formatCurrencyIDR(item.totalAmount)}</td>
                      <td className="px-3 py-2">{item.notes ?? "-"}</td>
                      <td className="px-3 py-2">
                        <button
                          type="button"
                          disabled={disableEdit}
                          className="rounded border border-zinc-300 px-2 py-1 disabled:opacity-50"
                          onClick={() => onEditItem(item, { bonusAmount: item.bonusAmount, deductionAmount: item.deductionAmount, notes: item.notes })}
                        >
                          Edit
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}
    </div>
  );
}
