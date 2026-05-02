"use client";

import { CoachPayrollPeriod } from "@/lib/api-types";

interface CoachPayrollTableProps {
  periods: CoachPayrollPeriod[];
  onSelectPeriod: (period: CoachPayrollPeriod) => void;
}

export default function CoachPayrollTable({ periods, onSelectPeriod }: CoachPayrollTableProps) {
  return (
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
                <button
                  type="button"
                  className="rounded border border-zinc-300 px-2 py-1"
                  onClick={() => onSelectPeriod(period)}
                >
                  Detail
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
