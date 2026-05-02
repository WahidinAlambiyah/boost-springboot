"use client";

import { StudentAssessment } from "@/lib/api-types";

interface AssessmentTableProps {
  assessments: StudentAssessment[];
  canWrite?: boolean;
  onEdit?: (assessment: StudentAssessment) => void;
  onDelete?: (assessment: StudentAssessment) => void;
}

export default function AssessmentTable({
  assessments,
  canWrite = false,
  onEdit,
  onDelete,
}: AssessmentTableProps) {
  return (
    <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="bg-zinc-100 text-zinc-700">
          <tr>
            <th className="px-4 py-2">Assessed At</th>
            <th className="px-4 py-2">Student</th>
            <th className="px-4 py-2">Class Session</th>
            <th className="px-4 py-2">Coach</th>
            <th className="px-4 py-2">Total Skill</th>
            <th className="px-4 py-2">Rata-rata</th>
            <th className="px-4 py-2">Catatan</th>
            {canWrite ? <th className="px-4 py-2">Aksi</th> : null}
          </tr>
        </thead>
        <tbody>
          {assessments.map((assessment) => {
            const totalSkills = assessment.scores.length;
            const averageScore =
              totalSkills > 0
                ? assessment.scores.reduce((acc, score) => acc + score.score, 0) / totalSkills
                : 0;

            return (
              <tr key={assessment.id} className="border-t border-zinc-200 text-zinc-800 align-top">
                <td className="px-4 py-2">{new Date(assessment.assessedAt).toLocaleString()}</td>
                <td className="px-4 py-2">{assessment.studentId}</td>
                <td className="px-4 py-2">{assessment.classSessionId}</td>
                <td className="px-4 py-2">{assessment.coachId}</td>
                <td className="px-4 py-2">{totalSkills}</td>
                <td className="px-4 py-2">{averageScore.toFixed(2)}</td>
                <td className="px-4 py-2">{assessment.notes || "-"}</td>
                {canWrite ? (
                  <td className="px-4 py-2">
                    <div className="flex gap-2">
                      {onEdit ? (
                        <button
                          type="button"
                          className="rounded border border-zinc-300 px-2 py-1"
                          onClick={() => onEdit(assessment)}
                        >
                          Edit
                        </button>
                      ) : null}
                      {onDelete ? (
                        <button
                          type="button"
                          className="rounded border border-red-300 px-2 py-1 text-red-600"
                          onClick={() => onDelete(assessment)}
                        >
                          Hapus
                        </button>
                      ) : null}
                    </div>
                  </td>
                ) : null}
              </tr>
            );
          })}
          {assessments.length === 0 ? (
            <tr>
              <td className="px-4 py-4 text-center text-zinc-500" colSpan={canWrite ? 8 : 7}>
                Belum ada data assessment.
              </td>
            </tr>
          ) : null}
        </tbody>
      </table>
    </div>
  );
}
