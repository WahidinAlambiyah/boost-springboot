"use client";

import { Academy, CoachProfile } from "@/lib/api-types";
import { ConfirmDialog } from "@/app/components/confirm-dialog";
import { StatusBadge } from "@/app/components/status-badge";

interface CoachProfileTableProps {
  profiles: CoachProfile[];
  academies: Academy[];
  canWrite: boolean;
  onEdit: (profile: CoachProfile) => void;
  onDelete: (profile: CoachProfile) => void;
}

export default function CoachProfileTable({ profiles, academies, canWrite, onEdit, onDelete }: CoachProfileTableProps) {
  const academyMap = new Map(academies.map((academy) => [academy.id, academy.name]));

  return <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white"><table className="w-full text-left text-sm"><thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Academy</th><th className="px-4 py-2">Name</th><th className="px-4 py-2">Phone</th><th className="px-4 py-2">Employment</th><th className="px-4 py-2">Pay Type</th><th className="px-4 py-2">Active</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead><tbody>{profiles.map((profile) => <tr key={profile.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{academyMap.get(profile.academyId) ?? "-"}</td><td className="px-4 py-2">{profile.fullName}</td><td className="px-4 py-2">{profile.phone ?? "-"}</td><td className="px-4 py-2">{profile.employmentType}</td><td className="px-4 py-2">{profile.payType}</td><td className="px-4 py-2"><StatusBadge label={profile.isActive ? "Active" : "Inactive"} tone={profile.isActive ? "success" : "neutral"} /></td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(profile)}>Edit</button><ConfirmDialog options={{ title: "Hapus coach profile?", description: `${profile.fullName} akan dihapus.`, confirmText: "Hapus" }} onConfirm={() => onDelete(profile)}>{(open) => <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={open}>Delete</button>}</ConfirmDialog></div></td> : null}</tr>)}</tbody></table></div>;
}
