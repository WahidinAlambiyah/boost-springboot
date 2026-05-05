type ProgressPreviewCardProps = {
  studentName: string;
  level: string;
  balance: string;
  braking: string;
  confidence: string;
  coachNote: string;
};

export default function ProgressPreviewCard({
  studentName,
  level,
  balance,
  braking,
  confidence,
  coachNote,
}: ProgressPreviewCardProps) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-zinc-900">{studentName}</p>
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">Level {level}</span>
      </div>
      <div className="mt-4 space-y-2 text-sm text-zinc-700">
        <p>
          <span className="font-semibold">Balance:</span> {balance}
        </p>
        <p>
          <span className="font-semibold">Braking:</span> {braking}
        </p>
        <p>
          <span className="font-semibold">Confidence:</span> {confidence}
        </p>
      </div>
      <p className="mt-4 text-sm leading-6 text-zinc-600">
        <span className="font-semibold text-zinc-800">Catatan coach:</span> {coachNote}
      </p>
    </article>
  );
}
