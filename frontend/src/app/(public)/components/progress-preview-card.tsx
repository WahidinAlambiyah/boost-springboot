type ProgressPreviewCardProps = {
  studentName: string;
  consistency: string;
  milestone: string;
};

export default function ProgressPreviewCard({
  studentName,
  consistency,
  milestone,
}: ProgressPreviewCardProps) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <p className="text-sm font-semibold text-zinc-900">{studentName}</p>
      <div className="mt-4 h-2 overflow-hidden rounded-full bg-zinc-100">
        <div className="h-full w-3/4 rounded-full bg-gradient-to-r from-emerald-400 to-amber-300" />
      </div>
      <div className="mt-4 flex items-center justify-between text-sm">
        <span className="text-zinc-500">Konsistensi</span>
        <span className="font-semibold text-zinc-800">{consistency}</span>
      </div>
      <p className="mt-2 text-sm text-zinc-600">Milestone: {milestone}</p>
    </article>
  );
}
