"use client";

export interface ConfirmActionButtonProps {
  label: string;
  confirmMessage: string;
  onConfirm: () => void | Promise<void>;
  variant?: "default" | "danger";
  disabled?: boolean;
  className?: string;
}

const VARIANT_STYLES = {
  default: "border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50",
  danger: "border-red-200 bg-white text-red-700 hover:bg-red-50",
} as const;

export function ConfirmActionButton({
  label,
  confirmMessage,
  onConfirm,
  variant = "default",
  disabled = false,
  className,
}: ConfirmActionButtonProps) {
  const handleClick = async () => {
    if (window.confirm(confirmMessage)) {
      await onConfirm();
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={disabled}
      className={[
        "rounded-md border px-3 py-2 text-sm font-medium shadow-sm transition disabled:cursor-not-allowed disabled:opacity-60",
        VARIANT_STYLES[variant],
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {label}
    </button>
  );
}
