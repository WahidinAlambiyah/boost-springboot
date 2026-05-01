"use client";

import { ClassSessionConflict } from "@/lib/api-types";

interface SessionConflictAlertProps {
  conflicts: ClassSessionConflict[];
  isLoading?: boolean;
}

const toneByType: Record<ClassSessionConflict["type"], "danger" | "warning"> = {
  TIME_OVERLAP: "danger",
  COACH_OVERLAP: "danger",
  LOCATION_OVERLAP: "warning",
};

export default function SessionConflictAlert({ conflicts, isLoading = false }: SessionConflictAlertProps) {
  if (isLoading) {
    return <div className="rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-900">Memeriksa konflik jadwal...</div>;
  }

  if (conflicts.length === 0) {
    return null;
  }

  return <div className="space-y-2 rounded-md border border-red-300 bg-red-50 p-3">
    <p className="text-sm font-semibold text-red-800">Konflik jadwal terdeteksi ({conflicts.length})</p>
    <ul className="space-y-2 text-sm">
      {conflicts.map((conflict) => {
        const tone = toneByType[conflict.type];
        const classes = tone === "danger"
          ? "border-red-300 bg-red-100 text-red-900"
          : "border-amber-300 bg-amber-100 text-amber-900";

        return <li key={`${conflict.type}-${conflict.sessionId}-${conflict.startTime}`} className={`rounded border px-2 py-1 ${classes}`}>
          <p className="font-medium">{conflict.type}</p>
          <p>{conflict.message}</p>
          <p className="text-xs opacity-80">{conflict.sessionDate} {conflict.startTime}-{conflict.endTime}</p>
        </li>;
      })}
    </ul>
  </div>;
}
