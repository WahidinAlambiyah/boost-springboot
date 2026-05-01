"use client";

import { Academy } from "@/lib/api-types";
import { ConfirmDialog } from "@/app/components/confirm-dialog";
import { StatusBadge } from "@/app/components/status-badge";

interface AcademyTableProps {
  academies: Academy[];
  canWrite: boolean;
  onEdit: (academy: Academy) => void;
  onDelete: (academy: Academy) => void;
}

export default function AcademyTable({ academies, canWrite, onEdit, onDelete }: AcademyTableProps) {
  return (
    <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="bg-zinc-100 text-zinc-700">
          <tr>
            <th className="px-4 py-2">Code</th>
            <th className="px-4 py-2">Name</th>
            <th className="px-4 py-2">Owner / Contact</th>
            <th className="px-4 py-2">Active</th>
            {canWrite ? <th className="px-4 py-2">Action</th> : null}
          </tr>
        </thead>
        <tbody>
          {academies.map((academy) => (
            <tr key={academy.id} className="border-t border-zinc-200 text-zinc-800">
              <td className="px-4 py-2">{academy.code}</td>
              <td className="px-4 py-2">{academy.name}</td>
              <td className="px-4 py-2">{academy.email || academy.phone || "-"}</td>
              <td className="px-4 py-2">
                <StatusBadge label={academy.isActive ? "Active" : "Inactive"} tone={academy.isActive ? "success" : "neutral"} />
              </td>
              {canWrite ? (
                <td className="px-4 py-2">
                  <div className="flex gap-2">
                    <button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(academy)}>
                      Edit
                    </button>
                    <ConfirmDialog
                      options={{ title: "Hapus academy?", description: `Academy ${academy.name} akan dihapus.`, confirmText: "Hapus" }}
                      onConfirm={() => onDelete(academy)}
                    >
                      {(open) => (
                        <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={open}>
                          Delete
                        </button>
                      )}
                    </ConfirmDialog>
                  </div>
                </td>
              ) : null}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
