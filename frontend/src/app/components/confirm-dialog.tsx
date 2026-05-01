"use client";

export interface ConfirmDialogOptions {
  title?: string;
  description: string;
  confirmText?: string;
}

export interface ConfirmDialogProps {
  options: ConfirmDialogOptions;
  onConfirm: () => void | Promise<void>;
  children: (open: () => Promise<void>) => import("react").ReactNode;
}

export function ConfirmDialog({ options, onConfirm, children }: ConfirmDialogProps) {
  const open = async () => {
    const message = options.title ? `${options.title}\n\n${options.description}` : options.description;
    const confirmed = window.confirm(message);

    if (confirmed) {
      await onConfirm();
    }
  };

  return children(open);
}
