"use client";

import { type ReactNode } from "react";

import { TailAdminButton } from "./tailadmin-button";
import { CloseIcon } from "./tailadmin-icons";

interface TailAdminConfirmModalProps {
  open: boolean;
  title: string;
  description: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  tone?: "danger" | "warning" | "primary";
  isLoading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function TailAdminConfirmModal({
  open,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  tone = "danger",
  isLoading = false,
  onConfirm,
  onCancel,
}: TailAdminConfirmModalProps) {
  if (!open) return null;

  const confirmVariant = tone === "danger" ? "danger" : "primary";

  return (
    <div className="fixed inset-0 z-[99999] flex items-center justify-center px-4 py-6" role="dialog" aria-modal="true">
      <button
        type="button"
        className="absolute inset-0 bg-gray-900/50 backdrop-blur-[2px]"
        aria-label="Close confirmation modal"
        onClick={isLoading ? undefined : onCancel}
      />

      <div className="relative w-full max-w-md rounded-2xl border border-gray-200 bg-white p-6 shadow-xl">
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <div
              className={`mb-3 flex h-12 w-12 items-center justify-center rounded-full ${
                tone === "danger" ? "bg-rose-50 text-rose-600" : tone === "warning" ? "bg-amber-50 text-amber-600" : "bg-blue-50 text-blue-600"
              }`}
            >
              <span className="text-xl font-semibold">!</span>
            </div>
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
          </div>

          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="rounded-lg p-1 text-gray-400 transition hover:bg-gray-100 hover:text-gray-700 disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Close modal"
          >
            <CloseIcon className="h-5 w-5" />
          </button>
        </div>

        <div className="mb-6 text-sm leading-6 text-gray-600">{description}</div>

        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <TailAdminButton variant="secondary" onClick={onCancel} disabled={isLoading}>
            {cancelLabel}
          </TailAdminButton>
          <TailAdminButton variant={confirmVariant} onClick={onConfirm} disabled={isLoading}>
            {isLoading ? "Processing..." : confirmLabel}
          </TailAdminButton>
        </div>
      </div>
    </div>
  );
}
