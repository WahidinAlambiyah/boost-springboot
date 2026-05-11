"use client";

import { type ReactNode, useEffect } from "react";

import { TailAdminButton } from "./tailadmin-button";

type ConfirmTone = "default" | "warning" | "danger";

interface ConfirmModalProps {
  open: boolean;
  title: string;
  description: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  tone?: ConfirmTone;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

const toneClassName: Record<ConfirmTone, string> = {
  default: "bg-blue-50 text-blue-600",
  warning: "bg-amber-50 text-amber-600",
  danger: "bg-rose-50 text-rose-600",
};

const buttonVariant: Record<ConfirmTone, "primary" | "danger" | "secondary"> = {
  default: "primary",
  warning: "primary",
  danger: "danger",
};

export function ConfirmModal({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  tone = "default",
  loading = false,
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onCancel();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onCancel, open]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[99999] flex items-center justify-center overflow-y-auto bg-gray-900/50 px-4 py-6 backdrop-blur-sm">
      <button type="button" aria-label="Close modal" className="absolute inset-0 cursor-default" onClick={onCancel} />
      <div role="dialog" aria-modal="true" className="relative w-full max-w-md rounded-2xl border border-gray-200 bg-white p-6 shadow-xl">
        <div className="flex gap-4">
          <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full ${toneClassName[tone]}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 8V12.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
              <path d="M12 16H12.01" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
              <path d="M10.29 3.86L1.82 18A2 2 0 0 0 3.53 21H20.47A2 2 0 0 0 22.18 18L13.71 3.86A2 2 0 0 0 10.29 3.86Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
            </svg>
          </div>
          <div className="min-w-0 flex-1">
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
            <div className="mt-2 text-sm leading-6 text-gray-600">{description}</div>
          </div>
        </div>
        <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <TailAdminButton variant="secondary" onClick={onCancel} disabled={loading}>{cancelLabel}</TailAdminButton>
          <TailAdminButton variant={buttonVariant[tone]} onClick={onConfirm} disabled={loading}>{loading ? "Processing..." : confirmLabel}</TailAdminButton>
        </div>
      </div>
    </div>
  );
}
