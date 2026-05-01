import type { ReactNode } from "react";

const STATUS_STYLES = {
  success: "bg-emerald-100 text-emerald-700",
  warning: "bg-amber-100 text-amber-700",
  danger: "bg-red-100 text-red-700",
  info: "bg-sky-100 text-sky-700",
  neutral: "bg-zinc-100 text-zinc-700",
} as const;

export type StatusBadgeTone = keyof typeof STATUS_STYLES;

export interface StatusBadgeProps {
  label: ReactNode;
  tone?: StatusBadgeTone;
  className?: string;
}

export function StatusBadge({ label, tone = "neutral", className }: StatusBadgeProps) {
  return <span className={["inline-flex rounded-full px-2.5 py-1 text-xs font-medium", STATUS_STYLES[tone], className].filter(Boolean).join(" ")}>{label}</span>;
}
