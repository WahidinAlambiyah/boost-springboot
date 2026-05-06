"use client";

export interface ConfirmActionButtonProps {
  label: string;
  confirmMessage: string;
  onConfirm: () => void | Promise<void>;
  variant?: "default" | "danger";
}

const VARIANT_STYLES = {
  default: "border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50",
  danger: "border-red-200 bg-white text-red-700 hover:bg-red-50",
} as const;

export function ConfirmActionButton({ label, confirmMessage, onConfirm, variant = "default" }: ConfirmActionButtonProps) {
  const handleClick = async () => {
    if (window.confirm(confirmMessage)) {
      await onConfirm();
    }
  };

  return (
    <button type="button" onClick={handleClick} className={["rounded-md border px-3 py-2 text-sm font-medium shadow-sm transition", VARIANT_STYLES[variant]].join(" ")}>
      {label}
    </button>
  );
}
