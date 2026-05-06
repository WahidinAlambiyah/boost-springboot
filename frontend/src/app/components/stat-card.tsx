import type { ReactNode } from "react";

export interface StatCardProps {
  title: string;
  value: ReactNode;
  description?: string;
  trend?: ReactNode;
  className?: string;
}

export function StatCard({ title, value, description, trend, className }: StatCardProps) {
  return (
    <section
      className={[
        "rounded-lg border border-zinc-200 bg-white p-5 shadow-sm",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-zinc-600">{title}</p>
          <p className="mt-2 text-2xl font-semibold text-zinc-900">{value}</p>
        </div>
        {trend ? <div className="text-sm font-medium text-zinc-600">{trend}</div> : null}
      </div>
      {description ? <p className="mt-3 text-sm text-zinc-500">{description}</p> : null}
    </section>
  );
}
