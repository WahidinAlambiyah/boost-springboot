import { SectionCard } from "@/app/components/section-card";

import {
  StudentProgressCoachNote,
  StudentProgressNextRecommendation,
} from "../student-progress-report.types";
import { formatDate } from "./student-progress-formatters";

interface CoachNotesListProps {
  notes?: StudentProgressCoachNote[] | string | null;
  recommendations?: StudentProgressNextRecommendation[] | null;
  legacyRecommendation?: string | null;
}

export function CoachNotesList({ notes, recommendations = [], legacyRecommendation }: CoachNotesListProps) {
  const normalizedNotes = typeof notes === "string" ? [] : notes ?? [];
  const textNote = typeof notes === "string" ? notes : null;
  const recommendationItems = recommendations ?? [];

  return (
    <SectionCard title="Coach Notes" description="Catatan coach dan rekomendasi latihan berikutnya.">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div>
          <h3 className="text-sm font-semibold text-zinc-900">Catatan</h3>
          {textNote ? <p className="mt-3 rounded-lg bg-zinc-50 p-3 text-sm text-zinc-700">{textNote}</p> : null}
          {!textNote && !normalizedNotes.length ? (
            <p className="mt-3 text-sm text-zinc-600">Belum ada catatan coach pada periode ini.</p>
          ) : null}
          <div className="mt-3 space-y-3">
            {normalizedNotes.map((note, index) => (
              <article key={note.assessmentId ?? `${note.sessionDate}-${index}`} className="rounded-lg border border-zinc-200 p-3 text-sm">
                <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-zinc-500">
                  <span>{formatDate(note.sessionDate)}</span>
                  <span>{note.coachName || "Coach"}</span>
                </div>
                <p className="mt-2 text-zinc-800">{note.overallNotes || "-"}</p>
                {note.recommendation ? <p className="mt-2 text-zinc-600">Rekomendasi: {note.recommendation}</p> : null}
              </article>
            ))}
          </div>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-zinc-900">Next Recommendation</h3>
          {!recommendationItems.length && !legacyRecommendation ? (
            <p className="mt-3 text-sm text-zinc-600">Belum ada rekomendasi khusus.</p>
          ) : null}
          {legacyRecommendation ? <p className="mt-3 rounded-lg bg-blue-50 p-3 text-sm text-blue-900">{legacyRecommendation}</p> : null}
          <div className="mt-3 space-y-3">
            {recommendationItems.map((item, index) => (
              <article key={`${item.title}-${index}`} className="rounded-lg bg-blue-50 p-3 text-sm text-blue-950">
                <div className="flex items-center justify-between gap-3">
                  <h4 className="font-semibold">{item.title}</h4>
                  {item.priority ? <span className="text-xs uppercase tracking-wide text-blue-700">{item.priority}</span> : null}
                </div>
                {item.description ? <p className="mt-1 text-blue-900">{item.description}</p> : null}
              </article>
            ))}
          </div>
        </div>
      </div>
    </SectionCard>
  );
}
