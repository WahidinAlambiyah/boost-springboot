"use client";

import { useId } from "react";

import { LookupOption } from "@/features/lookups/lookup.types";

export interface LookupSelectProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: LookupOption[];
  isLoading?: boolean;
  error?: string | null;
  placeholder?: string;
  required?: boolean;
  disabled?: boolean;
  helperText?: string;
  id?: string;
  className?: string;
}

export function LookupSelect({
  label,
  value,
  onChange,
  options,
  isLoading = false,
  error,
  placeholder = "Pilih opsi",
  required = false,
  disabled = false,
  helperText,
  id,
  className,
}: LookupSelectProps) {
  const generatedId = useId();
  const fieldId = id ?? `lookup-select-${generatedId}`;
  const hasError = Boolean(error);
  const resolvedHelperText = error ?? helperText;

  return (
    <div className={["space-y-1.5", className].filter(Boolean).join(" ")}>
      <label htmlFor={fieldId} className="block text-sm font-medium text-zinc-800">
        {label}
        {required ? (
          <span className="ml-1 text-red-600" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>

      <select
        id={fieldId}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        disabled={disabled || isLoading}
        aria-invalid={hasError}
        className={[
          "min-h-12 w-full rounded-md border bg-white px-3 py-3 text-sm text-zinc-900 shadow-sm transition focus:outline-none focus:ring-1 disabled:cursor-not-allowed disabled:bg-zinc-100 disabled:text-zinc-500",
          hasError
            ? "border-red-300 focus:border-red-600 focus:ring-red-600"
            : "border-zinc-300 focus:border-zinc-900 focus:ring-zinc-900",
        ].join(" ")}
      >
        <option value="">{isLoading ? "Memuat opsi..." : placeholder}</option>
        {options.map((option) => (
          <option key={option.id} value={option.id}>
            {option.label}
          </option>
        ))}
      </select>

      {resolvedHelperText ? (
        <p className={hasError ? "text-xs text-red-600" : "text-xs text-zinc-500"}>
          {resolvedHelperText}
        </p>
      ) : null}
    </div>
  );
}
