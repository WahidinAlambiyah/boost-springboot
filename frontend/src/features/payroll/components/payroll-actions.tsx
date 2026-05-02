"use client";

import { ConfirmDialog } from "@/app/components/confirm-dialog";

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
      <ConfirmDialog options={{ title: "Approve Payroll", description: "Yakin ingin approve payroll period ini?" }} onConfirm={onApprove}>
        {(openConfirm) => (
          <button
            type="button"
            onClick={() => void openConfirm()}
            disabled={disabled}
            className="rounded-md bg-emerald-600 px-4 py-2 text-sm text-white hover:bg-emerald-500 disabled:opacity-50"
          >
            Approve
          </button>
        )}
      </ConfirmDialog>

      <ConfirmDialog options={{ title: "Mark Payroll as Paid", description: "Yakin ingin menandai payroll period ini sebagai PAID?" }} onConfirm={onMarkPaid}>
        {(openConfirm) => (
          <button
            type="button"
            onClick={() => void openConfirm()}
            disabled={disabled}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-500 disabled:opacity-50"
          >
            Mark Paid
          </button>
        )}
      </ConfirmDialog>
    </div>
  );
}
