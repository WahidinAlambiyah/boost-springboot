import type { InputHTMLAttributes, ReactNode } from "react";

export interface FormFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "size"> {
  id: string;
  label: string;
  hint?: string;
  error?: string;
  rightElement?: ReactNode;
}

export function FormField({ id, label, hint, error, rightElement, className, ...inputProps }: FormFieldProps) {
  return (
    <div className={["space-y-1", className].filter(Boolean).join(" ")}>
      <label htmlFor={id} className="block text-sm font-medium text-zinc-800">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : hint ? `${id}-hint` : undefined}
          className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
          {...inputProps}
        />
        {rightElement ? <div className="pointer-events-none absolute inset-y-0 right-3 flex items-center">{rightElement}</div> : null}
      </div>
      {error ? (
        <p id={`${id}-error`} className="text-xs text-red-600">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="text-xs text-zinc-500">
          {hint}
        </p>
      ) : null}
    </div>
  );
}
