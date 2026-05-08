"use client";

import { useMemo, useState } from "react";

import { SectionCard } from "@/app/components/section-card";
import { createWhatsAppLink } from "@/lib/whatsapp";

import {
  StudentProgressReportParams,
  StudentProgressReportResponse,
} from "../student-progress-report.types";
import { buildFallbackWhatsappSummary } from "./student-progress-formatters";

interface WhatsappSummaryCardProps {
  report: StudentProgressReportResponse;
  params?: StudentProgressReportParams;
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

export function WhatsappSummaryCard({ report, params }: WhatsappSummaryCardProps) {
  const [copyStatus, setCopyStatus] = useState<"idle" | "success" | "error">("idle");
  const summary = useMemo(
    () => report.whatsappSummary?.trim() || buildFallbackWhatsappSummary(report, params),
    [params, report],
  );
  const whatsappLink = useMemo(() => createWhatsAppLink(summary), [summary]);

  const handleCopy = async () => {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(summary);
        setCopyStatus("success");
        return;
      }

      setCopyStatus(fallbackCopyTextToClipboard(summary) ? "success" : "error");
    } catch {
      setCopyStatus(fallbackCopyTextToClipboard(summary) ? "success" : "error");
    }
  };

  return (
    <SectionCard
      title="WhatsApp Summary"
      description="Copy ringkasan parent-ready ini lalu kirim ke WhatsApp. Jika backend tidak mengirim summary, frontend membuat fallback otomatis."
      actions={
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={handleCopy}
            className="rounded-md bg-emerald-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-emerald-700"
          >
            Copy Summary
          </button>
          <a
            href={whatsappLink}
            target="_blank"
            rel="noreferrer"
            className="rounded-md border border-emerald-600 px-3 py-2 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-50"
          >
            Open WhatsApp
          </a>
        </div>
      }
    >
      <textarea
        readOnly
        value={summary}
        className="min-h-72 w-full resize-y rounded-lg border border-zinc-300 bg-zinc-50 p-3 font-mono text-sm leading-6 text-zinc-800 focus:outline-none focus:ring-1 focus:ring-emerald-600"
      />
      {copyStatus === "success" ? (
        <p className="mt-2 text-sm text-emerald-700">Ringkasan berhasil disalin. Tinggal paste ke WhatsApp.</p>
      ) : null}
      {copyStatus === "error" ? (
        <p className="mt-2 text-sm text-rose-700">Gagal menyalin ringkasan. Silakan copy manual dari textarea.</p>
      ) : null}
    </SectionCard>
  );
}
