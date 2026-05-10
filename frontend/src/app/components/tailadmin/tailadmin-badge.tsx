import { type ReactNode } from "react";

type TailAdminBadgeTone = "default" | "success" | "warning" | "danger" | "info";

interface TailAdminBadgeProps {
  children: ReactNode;
  tone?: TailAdminBadgeTone;
  className?: string;
}

const toneClassName: Record<TailAdminBadgeTone, string> = {
  default: "bg-zinc-100 text-zinc-700",
  success: "bg-emerald-50 text-emerald-700 ring-emerald-600/20",
  warning: "bg-amber-50 text-amber-700 ring-amber-600/20",
  danger: "bg-rose-50 text-rose-700 ring-rose-600/20",
  info: "bg-blue-50 text-blue-700 ring-blue-600/20",
};

export function TailAdminBadge({ children, tone = "default", className = "" }: TailAdminBadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset ${toneClassName[tone]} ${className}`}>
      {children}
    </span>
  );
}
