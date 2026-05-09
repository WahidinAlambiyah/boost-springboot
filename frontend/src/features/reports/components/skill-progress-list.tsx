import { SectionCard } from "@/app/components/section-card";

import { StudentProgressSkillProgress } from "../student-progress-report.types";
import {
  getSkillAverageScore,
  getSkillLatestScore,
  toNumber,
  trendLabel,
} from "./student-progress-formatters";

interface SkillProgressListProps {
  items: StudentProgressSkillProgress[];
}

export function SkillProgressList({ items }: SkillProgressListProps) {
  return (
    <SectionCard title="Skill Progress" description="Skor terakhir, rata-rata, dan tren skill tanpa chart library.">
      {!items.length ? (
        <p className="text-sm text-zinc-600">Belum ada data skill progress pada periode ini.</p>
      ) : (
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          {items.map((item, index) => {
            const latestScore = getSkillLatestScore(item);
            const averageScore = getSkillAverageScore(item);
            const maxScore = item.maxScore ?? 10;
            const latestWidth = `${Math.min(100, Math.max(0, (latestScore / maxScore) * 100))}%`;
            const trend = trendLabel[item.trend ?? ""] ?? item.trend ?? "-";

            return (
              <article key={`${item.skillCode || item.skillName}-${index}`} className="rounded-lg border border-zinc-200 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold text-zinc-900">{item.skillName}</h3>
                    {item.skillCode ? <p className="text-xs text-zinc-500">{item.skillCode}</p> : null}
                  </div>
                  <span className="rounded-full bg-zinc-100 px-2 py-1 text-xs font-medium text-zinc-700">{trend}</span>
                </div>

                <div className="mt-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-zinc-600">Latest Score</span>
                    <span className="font-semibold text-zinc-900">
                      {latestScore.toFixed(1)}/{maxScore}
                    </span>
                  </div>
                  <div className="h-2.5 overflow-hidden rounded-full bg-zinc-100">
                    <div className="h-full rounded-full bg-blue-600" style={{ width: latestWidth }} />
                  </div>
                  <div className="grid grid-cols-2 gap-3 pt-2 text-sm">
                    <div className="rounded bg-zinc-50 p-2">
                      <p className="text-xs text-zinc-500">Average</p>
                      <p className="font-semibold text-zinc-900">{averageScore.toFixed(1)}</p>
                    </div>
                    <div className="rounded bg-zinc-50 p-2">
                      <p className="text-xs text-zinc-500">Previous</p>
                      <p className="font-semibold text-zinc-900">{toNumber(item.previousScore).toFixed(1)}</p>
                    </div>
                  </div>
                </div>

                {item.latestNotes ? <p className="mt-3 text-sm text-zinc-600">{item.latestNotes}</p> : null}
              </article>
            );
          })}
        </div>
      )}
    </SectionCard>
  );
}
