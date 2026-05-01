"use client";

import { Academy, CoachProfile } from "@/lib/api-types";

interface CoachProfileTableProps {
  profiles: CoachProfile[];
  academies: Academy[];
  canWrite: boolean;
  onEdit: (profile: CoachProfile) => void;
  onDelete: (profile: CoachProfile) => void;
}

export default function CoachProfileTable({ profiles, academies, canWrite, onEdit, onDelete }: CoachProfileTableProps) {
  const academyMap = new Map(academies.map((academy) => [academy.id, academy.name]));

  return <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white"><table className="w-full text-left text-sm"><thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Academy</th><th className="px-4 py-2">Name</th><th className="px-4 py-2">Phone</th><th className="px-4 py-2">Employment</th><th className="px-4 py-2">Pay Type</th><th className="px-4 py-2">Active</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead><tbody>{profiles.map((profile) => <tr key={profile.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{academyMap.get(profile.academyId) ?? "-"}</td><td className="px-4 py-2">{profile.fullName}</td><td className="px-4 py-2">{profile.phone ?? "-"}</td><td className="px-4 py-2">{profile.employmentType}</td><td className="px-4 py-2">{profile.payType}</td><td className="px-4 py-2">{profile.isActive ? "Yes" : "No"}</td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(profile)}>Edit</button><button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={() => onDelete(profile)}>Delete</button></div></td> : null}</tr>)}</tbody></table></div>;
}
