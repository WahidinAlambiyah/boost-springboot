"use client";

import { type ReactNode } from "react";

import { TailAdminButton } from "./tailadmin-button";

interface TailAdminConfirmationModalProps {
  open: boolean;
  title: string;
  description: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  tone?: "default" | "danger" | "warning";
  isLoading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function TailAdminConfirmationModal({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  tone = "default",
  isLoading = false,
  onConfirm,
  onCancel,
}: TailAdminConfirmationModalProps) {
  if (!open) return null;

  const iconClassName = tone === "danger"
    ? "bg-rose-50 text-rose-600"
    : tone === "warning"
      ? "bg-amber-50 text-amber-600"
      : "bg-blue-50 text-blue-600";
  const confirmVariant = tone === "danger" ? "danger" : "primary";

  return (
    <div className="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/50 px-4 py-6 backdrop-blur-sm" role="dialog" aria-modal="true" aria-labelledby="confirmation-modal-title">
      <div className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-6 shadow-xl">
        <div className="flex gap-4">
          <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full ${iconClassName}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 8V12.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
              <path d="M12 16H12.01" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
              <path d="M10.29 3.86L2.82 17.5C2.09 18.84 3.05 20.5 4.58 20.5H19.42C20.95 20.5 21.91 18.84 21.18 17.5L13.71 3.86C12.95 2.47 11.05 2.47 10.29 3.86Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
            </svg>
          </div>
          <div className="min-w-0 flex-1">
            <h3 id="confirmation-modal-title" className="text-lg font-semibold text-gray-900">{title}</h3>
            <div className="mt-2 text-sm leading-6 text-gray-600">{description}</div>
          </div>
        </div>
        <div className="mt-6 flex justify-end gap-3">
          <TailAdminButton variant="secondary" onClick={onCancel} disabled={isLoading}>{cancelLabel}</TailAdminButton>
          <TailAdminButton variant={confirmVariant} onClick={onConfirm} disabled={isLoading}>{isLoading ? "Processing..." : confirmLabel}</TailAdminButton>
        </div>
      </div>
    </div>
  );
}
