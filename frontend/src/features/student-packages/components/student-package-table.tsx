"use client";

import { StatusBadge } from "@/app/components/status-badge";
import { StudentPackage } from "@/features/student-packages/student-package.service";

interface StudentPackageTableProps {
  packages: StudentPackage[];
  canWrite: boolean;
  onEdit: (item: StudentPackage) => void;
}

const toDate = (value?: string | null) => {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("id-ID");
};

export default function StudentPackageTable({ packages, canWrite, onEdit }: StudentPackageTableProps) {
  return <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white"><table className="w-full text-left text-sm"><thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Package Name</th><th className="px-4 py-2">Start Date</th><th className="px-4 py-2">End Date</th><th className="px-4 py-2">Remaining Sessions</th><th className="px-4 py-2">Status</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead><tbody>{packages.map((item) => <tr key={item.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{item.packageName}</td><td className="px-4 py-2">{toDate(item.startDate)}</td><td className="px-4 py-2">{toDate(item.endDate)}</td><td className="px-4 py-2">{item.remainingSessions}</td><td className="px-4 py-2"><StatusBadge label={item.status} tone={item.status === "ACTIVE" ? "success" : "neutral"} /></td>{canWrite ? <td className="px-4 py-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(item)}>Update</button></td> : null}</tr>)}</tbody></table></div>;
}
