type ProgramCardProps = {
  title: string;
  level: string;
  description: string;
};

export default function ProgramCard({ title, level, description }: ProgramCardProps) {
  return (
    <article className="surface-card p-6">
      <p className="text-xs font-semibold uppercase tracking-wide text-emerald-700">{level}</p>
      <h3 className="mt-2 text-lg font-semibold text-zinc-900">{title}</h3>
      <p className="text-body mt-3">{description}</p>
    </article>
  );
}
