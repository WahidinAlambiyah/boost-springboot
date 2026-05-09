"use client";

import { useMemo, useState } from "react";

import { StudentProgressReportResponse } from "@/features/reports/student-progress-report.types";

import { buildFallbackWhatsappSummary } from "./student-progress-formatters";

interface WhatsAppSummaryButtonProps {
  report: StudentProgressReportResponse;
  from?: string;
  to?: string;
  className?: string;
}

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
  const summary = useMemo(() => report.whatsappSummary?.trim() || buildFallbackWhatsappSummary(report, { from, to }), [from, report, to]);

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
