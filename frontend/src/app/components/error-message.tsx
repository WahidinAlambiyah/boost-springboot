import type { ReactNode } from "react";

export interface ErrorMessageProps {
  message?: string;
  children?: ReactNode;
  className?: string;
}

export function ErrorMessage({ message, children, className }: ErrorMessageProps) {
  if (!message && !children) {
    return null;
  }

  return <p className={["text-sm text-red-600", className].filter(Boolean).join(" ")}>{message ?? children}</p>;
}
