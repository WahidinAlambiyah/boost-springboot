"use client";

import { Academy } from "@/lib/api-types";

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
              <td className="px-4 py-2">{academy.isActive ? "Yes" : "No"}</td>
              {canWrite ? (
                <td className="px-4 py-2">
                  <div className="flex gap-2">
                    <button type="button" className="rounded border border-zinc-300 px-2 py-1" onClick={() => onEdit(academy)}>
                      Edit
                    </button>
                    <button type="button" className="rounded border border-red-300 px-2 py-1 text-red-700" onClick={() => onDelete(academy)}>
                      Delete
                    </button>
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
