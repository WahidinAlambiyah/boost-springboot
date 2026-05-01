"use client";

import { Academy, AcademyLocation } from "@/lib/api-types";
import { ConfirmDialog } from "@/app/components/confirm-dialog";
import { StatusBadge } from "@/app/components/status-badge";

interface AcademyLocationTableProps {
  locations: AcademyLocation[];
  academies: Academy[];
  canWrite: boolean;
  onEdit: (location: AcademyLocation) => void;
  onDelete: (location: AcademyLocation) => void;
}

export default function AcademyLocationTable({ locations, academies, canWrite, onEdit, onDelete }: AcademyLocationTableProps) {
  const academyMap = new Map(academies.map((academy) => [academy.id, academy.name]));

  return (
    <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="bg-zinc-100 text-zinc-700">
          <tr><th className="px-4 py-2">Academy</th><th className="px-4 py-2">Code</th><th className="px-4 py-2">Name</th><th className="px-4 py-2">Address</th><th className="px-4 py-2">Active</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr>
        </thead>
        <tbody>
          {locations.map((location) => <tr key={location.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{academyMap.get(location.academyId) ?? "-"}</td><td className="px-4 py-2">{location.code}</td><td className="px-4 py-2">{location.name}</td><td className="px-4 py-2">{location.address || "-"}</td><td className="px-4 py-2"><StatusBadge label={location.isActive ? "Active" : "Inactive"} tone={location.isActive ? "success" : "neutral"} /></td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(location)}>Edit</button><ConfirmDialog options={{ title: "Hapus location?", description: `${location.name} akan dihapus.`, confirmText: "Hapus" }} onConfirm={() => onDelete(location)}>{(open) => <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={open}>Delete</button>}</ConfirmDialog></div></td> : null}</tr>)}
        </tbody>
      </table>
    </div>
  );
}
