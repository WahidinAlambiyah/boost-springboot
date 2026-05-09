import { type ReactNode } from "react";

interface TailAdminCardProps {
  title?: string;
  description?: string;
  action?: ReactNode;
  children: ReactNode;
  className?: string;
  bodyClassName?: string;
}

export function TailAdminCard({
  title,
  description,
  action,
  children,
  className = "",
  bodyClassName = "",
}: TailAdminCardProps) {
  const hasHeader = title || description || action;

  return (
    <section className={["rounded-2xl border border-zinc-200 bg-white shadow-sm", className].filter(Boolean).join(" ")}>
      {hasHeader ? (
        <div className="flex flex-col gap-3 border-b border-zinc-100 px-5 py-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            {title ? <h2 className="text-base font-semibold text-zinc-900">{title}</h2> : null}
            {description ? <p className="mt-1 text-sm text-zinc-500">{description}</p> : null}
          </div>
          {action ? <div className="shrink-0">{action}</div> : null}
        </div>
      ) : null}
      <div className={["p-5", bodyClassName].filter(Boolean).join(" ")}>{children}</div>
    </section>
  );
}
