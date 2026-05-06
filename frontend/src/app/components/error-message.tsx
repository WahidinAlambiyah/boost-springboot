import type { ReactNode } from "react";

export interface ErrorMessageProps {
  title?: string;
  message: string;
  children?: ReactNode;
  className?: string;
}

export function ErrorMessage({ title, message, children, className }: ErrorMessageProps) {
  return (
    <div className={["rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700", className].filter(Boolean).join(" ")} role="alert">
      {title ? <p className="font-semibold text-red-800">{title}</p> : null}
      <p className={title ? "mt-1" : undefined}>{message}</p>
      {children ? <div className="mt-2 text-red-700">{children}</div> : null}
    </div>
  );
}
