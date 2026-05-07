import { progressPreviewLabels } from "@/content/public/hero";

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
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">{progressPreviewLabels.levelPrefix} {level}</span>
      </div>
      <div className="mt-4 space-y-2 text-sm text-zinc-700">
        <p>
          <span className="font-semibold">{progressPreviewLabels.balance}:</span> {balance}
        </p>
        <p>
          <span className="font-semibold">{progressPreviewLabels.braking}:</span> {braking}
        </p>
        <p>
          <span className="font-semibold">{progressPreviewLabels.confidence}:</span> {confidence}
        </p>
      </div>
      <p className="mt-4 text-sm leading-6 text-zinc-600">
        <span className="font-semibold text-zinc-800">{progressPreviewLabels.coachNote}:</span> {coachNote}
      </p>
    </article>
  );
}
