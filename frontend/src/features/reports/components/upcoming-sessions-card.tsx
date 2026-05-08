import { SectionCard } from "@/app/components/section-card";

import { StudentProgressUpcomingSession } from "../student-progress-report.types";
import { formatDate, formatTimeRange } from "./student-progress-formatters";

interface UpcomingSessionsCardProps {
  items?: StudentProgressUpcomingSession[] | null;
}

export function UpcomingSessionsCard({ items }: UpcomingSessionsCardProps) {
  const sessions = items ?? [];

  return (
    <SectionCard title="Upcoming Sessions" description="Jadwal sesi terdekat untuk follow-up orang tua.">
      {!sessions.length ? (
        <p className="text-sm text-zinc-600">Belum ada upcoming session.</p>
      ) : (
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
          {sessions.map((item) => (
            <article key={item.sessionId} className="rounded-lg border border-zinc-200 p-3 text-sm">
              <p className="font-semibold text-zinc-900">{formatDate(item.sessionDate)}</p>
              <p className="mt-1 text-zinc-600">{formatTimeRange(item.startTime, item.endTime)}</p>
              <p className="mt-2 text-zinc-800">{item.classGroupName || "-"}</p>
              <p className="text-zinc-500">{item.locationName || "-"}</p>
            </article>
          ))}
        </div>
      )}
    </SectionCard>
  );
}
