"use client";

import { useMemo, useState } from "react";

import { StudentProgressReportResponse } from "@/features/reports/progress-report.service";

interface WhatsAppSummaryButtonProps {
  report: StudentProgressReportResponse;
  from?: string;
  to?: string;
  className?: string;
}

const trendLabel: Record<string, string> = {
  UP: "naik",
  DOWN: "turun",
  STABLE: "stabil",
};

function fallbackCopyTextToClipboard(text: string) {
  const textArea = document.createElement("textarea");
  textArea.value = text;
  textArea.style.position = "fixed";
  textArea.style.left = "-9999px";

  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();

  const success = document.execCommand("copy");
  document.body.removeChild(textArea);

  return success;
}

export default function WhatsAppSummaryButton({ report, from, to, className }: WhatsAppSummaryButtonProps) {
  const [status, setStatus] = useState<"idle" | "success" | "error">("idle");

  const summary = useMemo(() => {
    const period = from && to ? `${from} s/d ${to}` : from ? `mulai ${from}` : to ? `sampai ${to}` : "Semua data";

    const topSkills = [...report.skillProgress].sort((a, b) => b.latest - a.latest).slice(0, 3);
    const skillsText =
      topSkills.length > 0
        ? topSkills
            .map((skill, index) => {
              const trend = trendLabel[skill.trend] || skill.trend.toLowerCase();
              return `${index + 1}. ${skill.skillName}: ${skill.latest} (${trend})`;
            })
            .join("\n")
        : "- Belum ada data skill.";

    return [
      `Ringkasan Progress ${report.student.fullName}`,
      `Periode: ${period}`,
      "",
      "Kehadiran:",
      `- Hadir ${report.attendanceSummary.present}/${report.attendanceSummary.total} pertemuan`,
      `- Izin ${report.attendanceSummary.permit}, Sakit ${report.attendanceSummary.sick}, Absen ${report.attendanceSummary.absent}, Alpha ${report.attendanceSummary.alpha}`,
      "",
      "Skill kunci:",
      skillsText,
      "",
      `Catatan coach: ${report.coachNotes || "Belum ada catatan."}`,
      `Rekomendasi: ${report.recommendation || "Lanjutkan latihan rutin di rumah."}`,
    ].join("\n");
  }, [from, report, to]);

  const handleCopy = async () => {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(summary);
        setStatus("success");
        return;
      }

      const copied = fallbackCopyTextToClipboard(summary);
      setStatus(copied ? "success" : "error");
    } catch {
      const copied = fallbackCopyTextToClipboard(summary);
      setStatus(copied ? "success" : "error");
    }
  };

  return (
    <div className="space-y-2">
      <button
        type="button"
        className={className || "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white hover:bg-emerald-700"}
        onClick={handleCopy}
      >
        Copy Ringkasan WhatsApp
      </button>

      {status === "success" ? (
        <p className="text-xs text-emerald-700">Ringkasan berhasil disalin. Tinggal paste ke WhatsApp.</p>
      ) : null}

      {status === "error" ? (
        <p className="text-xs text-rose-700">Gagal menyalin ringkasan. Silakan coba lagi.</p>
      ) : null}
    </div>
  );
}
