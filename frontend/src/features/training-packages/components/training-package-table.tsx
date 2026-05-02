"use client";

import { ConfirmDialog } from "@/app/components/confirm-dialog";
import { StatusBadge } from "@/app/components/status-badge";
import { TrainingPackage } from "@/lib/api-types";

interface TrainingPackageTableProps {
  packages: TrainingPackage[];
  canWrite: boolean;
  onEdit: (pkg: TrainingPackage) => void;
  onDelete: (pkg: TrainingPackage) => void;
}

export default function TrainingPackageTable({ packages, canWrite, onEdit, onDelete }: TrainingPackageTableProps) {
  return <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white"><table className="w-full text-left text-sm"><thead className="bg-zinc-100 text-zinc-700"><tr><th className="px-4 py-2">Academy ID</th><th className="px-4 py-2">Code</th><th className="px-4 py-2">Name</th><th className="px-4 py-2">Package Type</th><th className="px-4 py-2">Session Quota</th><th className="px-4 py-2">Price</th><th className="px-4 py-2">Validity Days</th><th className="px-4 py-2">Active</th>{canWrite ? <th className="px-4 py-2">Action</th> : null}</tr></thead><tbody>{packages.map((pkg) => <tr key={pkg.id} className="border-t border-zinc-200 text-zinc-800"><td className="px-4 py-2">{pkg.academyId}</td><td className="px-4 py-2">{pkg.code}</td><td className="px-4 py-2">{pkg.name}</td><td className="px-4 py-2">{pkg.packageType}</td><td className="px-4 py-2">{pkg.sessionQuota ?? "-"}</td><td className="px-4 py-2">{pkg.price}</td><td className="px-4 py-2">{pkg.validityDays ?? "-"}</td><td className="px-4 py-2"><StatusBadge label={pkg.isActive ? "Active" : "Inactive"} tone={pkg.isActive ? "success" : "neutral"} /></td>{canWrite ? <td className="px-4 py-2"><div className="flex gap-2"><button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(pkg)}>Edit</button><ConfirmDialog options={{ title: "Hapus paket?", description: `Paket ${pkg.name} akan dihapus.`, confirmText: "Hapus" }} onConfirm={() => onDelete(pkg)}>{(open) => <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={open}>Delete</button>}</ConfirmDialog></div></td> : null}</tr>)}</tbody></table></div>;
}
