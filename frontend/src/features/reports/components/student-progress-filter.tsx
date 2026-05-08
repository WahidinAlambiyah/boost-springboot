"use client";

import { FormEvent } from "react";

import { StudentSelect } from "@/features/lookups/components/specialized-selects";

export interface StudentProgressFilterValue {
  studentId: string;
  from: string;
  to: string;
}

interface StudentProgressFilterProps {
  value: StudentProgressFilterValue;
  onChange: (value: StudentProgressFilterValue) => void;
  onGenerate: () => void;
  isGenerating?: boolean;
}

export function StudentProgressFilter({ value, onChange, onGenerate, isGenerating }: StudentProgressFilterProps) {
  const updateValue = (patch: Partial<StudentProgressFilterValue>) => onChange({ ...value, ...patch });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onGenerate();
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="grid grid-cols-1 gap-4 rounded-lg border border-zinc-200 bg-white p-4 shadow-sm md:grid-cols-[minmax(0,2fr)_minmax(0,1fr)_minmax(0,1fr)_auto] md:items-end"
    >
      <StudentSelect
        allowAll
        label="Student"
        value={value.studentId}
        onChange={(studentId) => updateValue({ studentId })}
        placeholder="Pilih student"
        helperText="Pilih anak yang ingin dibuatkan report."
        required
      />

      <label className="space-y-1.5 text-sm font-medium text-zinc-800">
        <span>Dari Tanggal</span>
        <input
          type="date"
          className="min-h-12 w-full rounded-md border border-zinc-300 bg-white px-3 py-3 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900"
          value={value.from}
          onChange={(event) => updateValue({ from: event.target.value })}
        />
      </label>

      <label className="space-y-1.5 text-sm font-medium text-zinc-800">
        <span>Sampai Tanggal</span>
        <input
          type="date"
          className="min-h-12 w-full rounded-md border border-zinc-300 bg-white px-3 py-3 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900"
          value={value.to}
          onChange={(event) => updateValue({ to: event.target.value })}
        />
      </label>

      <button
        type="submit"
        disabled={!value.studentId || isGenerating}
        className="min-h-12 rounded-md bg-zinc-900 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-300 md:mb-[22px]"
      >
        {isGenerating ? "Generating..." : "Generate"}
      </button>
    </form>
  );
}
