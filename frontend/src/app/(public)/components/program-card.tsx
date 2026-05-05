type ProgramCardProps = {
  title: string;
  level: string;
  description: string;
};

export default function ProgramCard({ title, level, description }: ProgramCardProps) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-wide text-emerald-700">{level}</p>
      <h3 className="mt-2 text-lg font-semibold text-zinc-900">{title}</h3>
      <p className="mt-3 text-sm leading-6 text-zinc-600">{description}</p>
    </article>
  );
}
