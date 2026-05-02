"use client";

import { FormEvent, useState } from "react";

interface PayrollPeriodFilterProps {
  initialMonth: number;
  initialYear: number;
  onApply: (month: number, year: number) => void;
}

export default function PayrollPeriodFilter({ initialMonth, initialYear, onApply }: PayrollPeriodFilterProps) {
  const [month, setMonth] = useState(String(initialMonth));
  const [year, setYear] = useState(String(initialYear));

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onApply(Number(month), Number(year));
  };

  return (
    <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3 rounded-lg border border-zinc-200 bg-white p-4">
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-zinc-700">Bulan</span>
        <input
          type="number"
          min={1}
          max={12}
          value={month}
          onChange={(event) => setMonth(event.target.value)}
          className="w-24 rounded-md border border-zinc-300 px-3 py-2 text-sm"
        />
      </label>
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-zinc-700">Tahun</span>
        <input
          type="number"
          min={2000}
          max={new Date().getUTCFullYear() + 1}
          value={year}
          onChange={(event) => setYear(event.target.value)}
          className="w-32 rounded-md border border-zinc-300 px-3 py-2 text-sm"
        />
      </label>
      <button type="submit" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700">
        Terapkan
      </button>
    </form>
  );
}
