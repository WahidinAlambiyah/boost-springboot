import { type ReactNode } from "react";

interface TailAdminCardProps {
  children: ReactNode;
  title?: string;
  description?: string;
  actions?: ReactNode;
  className?: string;
  bodyClassName?: string;
}

export function TailAdminCard({
  children,
  title,
  description,
  actions,
  className = "",
  bodyClassName = "",
}: TailAdminCardProps) {
  const hasHeader = title || description || actions;

  return (
    <section className={`rounded-2xl border border-zinc-200 bg-white shadow-sm ${className}`}>
      {hasHeader ? (
        <div className="flex flex-col gap-3 border-b border-zinc-100 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            {title ? <h2 className="text-base font-semibold text-zinc-900">{title}</h2> : null}
            {description ? <p className="mt-1 text-sm text-zinc-500">{description}</p> : null}
          </div>
          {actions ? <div className="flex items-center gap-2">{actions}</div> : null}
        </div>
      ) : null}
      <div className={`p-5 ${bodyClassName}`}>{children}</div>
    </section>
  );
}
