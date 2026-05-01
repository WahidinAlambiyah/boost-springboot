"use client";

import { useState } from "react";

import { ClassSessionCoach } from "@/lib/api-types";

interface SessionCoachManagerProps {
  coaches: ClassSessionCoach[];
  canWrite: boolean;
  onAdd: (payload: { coachId: string; attendanceStatus?: string }) => void;
  onUpdate: (sessionCoachId: string, payload: { attendanceStatus?: string }) => void;
  onRemove: (sessionCoachId: string) => void;
}

export default function SessionCoachManager({ coaches, canWrite, onAdd, onUpdate, onRemove }: SessionCoachManagerProps) {
  const [coachId, setCoachId] = useState("");
  const [attendanceStatus, setAttendanceStatus] = useState("ASSIGNED");

  return <div className="space-y-3">
    <h3 className="text-base font-semibold text-zinc-900">Coach Assigned</h3>
    <div className="overflow-hidden rounded-lg border border-zinc-200">
      <table className="w-full text-left text-sm"><thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Coach ID</th><th className="px-4 py-2">Role</th><th className="px-4 py-2">Attendance</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead><tbody>{coaches.map((coach) => <tr key={coach.id} className="border-t border-zinc-200"><td className="px-4 py-2">{coach.coachId}</td><td className="px-4 py-2">{coach.coachRole ?? "-"}</td><td className="px-4 py-2">{coach.attendanceStatus ?? "-"}</td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><select className="rounded border border-zinc-300 px-2 py-1" defaultValue={coach.attendanceStatus ?? "ASSIGNED"} onChange={(event) => onUpdate(coach.id, { attendanceStatus: event.target.value })}><option value="ASSIGNED">ASSIGNED</option><option value="PRESENT">PRESENT</option><option value="ABSENT">ABSENT</option></select><button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={() => onRemove(coach.id)}>Remove</button></div></td> : null}</tr>)}</tbody></table>
    </div>
    {canWrite ? <div className="flex flex-wrap items-end gap-2"><label className="text-sm"><span className="mb-1 block font-medium text-zinc-700">Coach ID</span><input className="rounded border border-zinc-300 px-3 py-2" value={coachId} onChange={(event) => setCoachId(event.target.value)} /></label><label className="text-sm"><span className="mb-1 block font-medium text-zinc-700">Attendance</span><select className="rounded border border-zinc-300 px-3 py-2" value={attendanceStatus} onChange={(event) => setAttendanceStatus(event.target.value)}><option value="ASSIGNED">ASSIGNED</option><option value="PRESENT">PRESENT</option><option value="ABSENT">ABSENT</option></select></label><button type="button" className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white" onClick={() => { if (!coachId.trim()) return; onAdd({ coachId: coachId.trim(), attendanceStatus }); setCoachId(""); }}>Tambah Coach</button></div> : null}
  </div>;
}
