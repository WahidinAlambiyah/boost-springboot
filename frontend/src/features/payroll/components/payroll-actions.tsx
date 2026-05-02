"use client";

interface PayrollActionsProps {
  canWrite?: boolean;
  disabled?: boolean;
  onApprove: () => void;
  onMarkPaid: () => void;
}

export default function PayrollActions({ canWrite = false, disabled, onApprove, onMarkPaid }: PayrollActionsProps) {
  if (!canWrite) return null;

  return (
    <div className="flex gap-2">
      <button
        type="button"
        onClick={onApprove}
        disabled={disabled}
        className="rounded-md bg-emerald-600 px-4 py-2 text-sm text-white hover:bg-emerald-500 disabled:opacity-50"
      >
        Approve
      </button>
      <button
        type="button"
        onClick={onMarkPaid}
        disabled={disabled}
        className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-500 disabled:opacity-50"
      >
        Mark Paid
      </button>
    </div>
  );
}
