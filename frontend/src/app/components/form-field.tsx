import type { ReactNode } from "react";

export interface FormFieldProps {
  label: ReactNode;
  error?: string;
  required?: boolean;
  children?: ReactNode;
  htmlFor?: string;
  id?: string;
  className?: string;
}

export function FormField({
  label,
  error,
  required = false,
  children,
  htmlFor,
  id,
  className,
}: FormFieldProps) {
  const resolvedHtmlFor = htmlFor ?? id;

  return (
    <div className={["space-y-1", className].filter(Boolean).join(" ")}>
      <label htmlFor={resolvedHtmlFor} className="block text-sm font-medium text-zinc-800">
        {label}
        {required ? (
          <span className="ml-1 text-red-600" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>
      {children}
      {error ? (
        <p className="text-xs text-red-600" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}
