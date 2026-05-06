import type { ReactNode } from "react";

const STATUS_VARIANT_STYLES = {
  default: "bg-zinc-100 text-zinc-700",
  success: "bg-emerald-100 text-emerald-700",
  warning: "bg-amber-100 text-amber-700",
  danger: "bg-red-100 text-red-700",
  info: "bg-sky-100 text-sky-700",
} as const;

export type StatusBadgeVariant = keyof typeof STATUS_VARIANT_STYLES;
export type StatusBadgeTone = StatusBadgeVariant | "neutral";

const STATUS_VARIANT_MAP: Record<string, StatusBadgeVariant> = {
  ACTIVE: "success",
  INACTIVE: "default",
  PRESENT: "success",
  ABSENT: "danger",
  PERMIT: "info",
  SICK: "warning",
  LATE: "warning",
  DRAFT: "default",
  CALCULATED: "info",
  APPROVED: "success",
  PAID: "success",
  SCHEDULED: "info",
  CANCELLED: "danger",
  COMPLETED: "success",
};

export interface StatusBadgeProps {
  status?: string;
  variant?: StatusBadgeVariant;
  className?: string;
  /** @deprecated Use status instead. Kept for existing pages during migration. */
  label?: ReactNode;
  /** @deprecated Use variant instead. Kept for existing pages during migration. */
  tone?: StatusBadgeTone;
}

function normalizeTone(tone?: StatusBadgeTone): StatusBadgeVariant | undefined {
  if (!tone) {
    return undefined;
  }

  return tone === "neutral" ? "default" : tone;
}

function getDefaultVariant(status?: string): StatusBadgeVariant {
  if (!status) {
    return "default";
  }

  return STATUS_VARIANT_MAP[status.toUpperCase()] ?? "default";
}

export function StatusBadge({ status, variant, label, tone, className }: StatusBadgeProps) {
  const displayLabel = label ?? status;

  if (!displayLabel) {
    return null;
  }

  const resolvedVariant = variant ?? normalizeTone(tone) ?? getDefaultVariant(status ?? String(displayLabel));

  return (
    <span className={["inline-flex rounded-full px-2.5 py-1 text-xs font-medium", STATUS_VARIANT_STYLES[resolvedVariant], className].filter(Boolean).join(" ")}>
      {displayLabel}
    </span>
  );
}
