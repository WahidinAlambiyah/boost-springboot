"use client";

import { useMemo, useState } from "react";

import { ConfirmDialog } from "@/app/components/confirm-dialog";
import { ClassSession } from "@/lib/api-types";

interface ClassSessionTableProps {
  sessions: ClassSession[];
  canWrite: boolean;
  onEdit: (session: ClassSession) => void;
  onDelete: (session: ClassSession) => void;
}

export default function ClassSessionTable({ sessions, canWrite, onEdit, onDelete }: ClassSessionTableProps) {
  const [statusFilter, setStatusFilter] = useState("");
  const [dateFilter, setDateFilter] = useState("");

  const filteredSessions = useMemo(() => sessions.filter((session) => {
    const byStatus = statusFilter ? session.status === statusFilter : true;
    const byDate = dateFilter ? session.sessionDate === dateFilter : true;
    return byStatus && byDate;
  }), [sessions, statusFilter, dateFilter]);

  return <div className="space-y-3">
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Filter status</span><select className="w-full rounded-md border border-zinc-300 px-3 py-2" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}><option value="">Semua</option><option value="SCHEDULED">SCHEDULED</option><option value="CANCELLED">CANCELLED</option><option value="COMPLETED">COMPLETED</option></select></label>
      <label className="block text-sm"><span className="mb-1 block font-medium text-zinc-700">Filter tanggal</span><input type="date" className="w-full rounded-md border border-zinc-300 px-3 py-2" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} /></label>
    </div>

    <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Tanggal</th><th className="px-4 py-2">Waktu</th><th className="px-4 py-2">Lokasi</th><th className="px-4 py-2">Status</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead>
        <tbody>
          {filteredSessions.map((session) => <tr key={session.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{session.sessionDate}</td><td className="px-4 py-2">{session.startTime} - {session.endTime}</td><td className="px-4 py-2">{session.locationId ?? "-"}</td><td className="px-4 py-2">{session.status}</td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(session)}>Edit</button><ConfirmDialog options={{ title: "Hapus session?", description: `Session ${session.sessionDate} akan dihapus.`, confirmText: "Hapus" }} onConfirm={() => onDelete(session)}>{(open) => <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={open}>Delete</button>}</ConfirmDialog></div></td> : null}</tr>)}
        </tbody>
      </table>
    </div>
  </div>;
}
