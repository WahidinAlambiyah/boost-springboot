"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import { BillingSummary, PaymentPayload, PaymentResponse } from "@/lib/api-types";
import { ApiResponse } from "@/types/api";
import { api } from "@/lib/api";
import {
  parseErrorMessage,
  parseValidationErrors,
  useStandardErrorRedirect,
} from "@/lib/error-handler";

const BILLING_QUERY_KEY = ["billing"];

const statusBadgeClass = (status: string) => {
  const normalizedStatus = status.toUpperCase();

  if (normalizedStatus.includes("PAID") || normalizedStatus.includes("SUCCESS")) {
    return "bg-emerald-100 text-emerald-700 border-emerald-200";
  }

  if (normalizedStatus.includes("PENDING") || normalizedStatus.includes("PROCESS")) {
    return "bg-amber-100 text-amber-700 border-amber-200";
  }

  if (normalizedStatus.includes("FAILED") || normalizedStatus.includes("ERROR")) {
    return "bg-red-100 text-red-700 border-red-200";
  }

  return "bg-zinc-100 text-zinc-700 border-zinc-200";
};

export default function BillingPage() {
  const queryClient = useQueryClient();
  const handleErrorRedirect = useStandardErrorRedirect();

  const [invoiceId, setInvoiceId] = useState("");
  const [amount, setAmount] = useState("");
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const billingQuery = useQuery({
    queryKey: BILLING_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get<ApiResponse<BillingSummary[]>>("/api/billing");
      return response.data.data;
    },
  });

  const payMutation = useMutation({
    mutationFn: async (payload: PaymentPayload) => {
      const idempotencyKey = crypto.randomUUID();
      const response = await api.post<ApiResponse<PaymentResponse>>(
        "/api/billing/pay",
        payload,
        {
          headers: {
            "Idempotency-Key": idempotencyKey,
          },
        },
      );

      return response.data;
    },
    onSuccess: async (data) => {
      setSubmitMessage(`${data.message} (Payment ID: ${data.data.paymentId})`);
      setValidationErrors({});
      setInvoiceId("");
      setAmount("");
      await queryClient.invalidateQueries({ queryKey: BILLING_QUERY_KEY });
    },
    onError: (error) => {
      handleErrorRedirect(error);
      setSubmitMessage(parseErrorMessage(error));
      setValidationErrors(parseValidationErrors(error));
    },
  });

  if (billingQuery.error) {
    handleErrorRedirect(billingQuery.error);
  }

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitMessage(null);
    setValidationErrors({});

    payMutation.mutate({
      invoiceId,
      amount: Number(amount),
    });
  };

  return (
    <AppShell>
      <h1 className="text-2xl font-semibold text-zinc-900">Billing Module</h1>
      <p className="mt-2 text-zinc-600">Ringkasan billing dan form pembayaran invoice.</p>

      {billingQuery.isLoading ? <p className="mt-4 text-sm text-zinc-600">Memuat billing...</p> : null}

      {billingQuery.isError ? (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Gagal mengambil data billing.
        </p>
      ) : null}

      {billingQuery.data ? (
        <div className="mt-6 overflow-hidden rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-100 text-zinc-700">
              <tr>
                <th className="px-4 py-2">Code</th>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {billingQuery.data.map((item) => (
                <tr key={item.code} className="border-t border-zinc-200 text-zinc-800">
                  <td className="px-4 py-2">{item.code}</td>
                  <td className="px-4 py-2">{item.name}</td>
                  <td className="px-4 py-2">
                    <span
                      className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-semibold ${statusBadgeClass(item.status)}`}
                    >
                      {item.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      <section className="mt-8 max-w-xl rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-zinc-900">Payment Form</h2>
        <form className="mt-4 space-y-4" onSubmit={onSubmit}>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Invoice ID</span>
            <input
              value={invoiceId}
              onChange={(event) => setInvoiceId(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              placeholder="UUID invoiceId"
              required
            />
            {validationErrors.invoiceId ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.invoiceId}</p>
            ) : null}
          </label>

          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Amount</span>
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              placeholder="Contoh: 250000"
              required
            />
            {validationErrors.amount ? (
              <p className="mt-1 text-xs text-red-600">{validationErrors.amount}</p>
            ) : null}
          </label>

          <button
            type="submit"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
            disabled={payMutation.isPending}
          >
            {payMutation.isPending ? "Memproses..." : "Pay Invoice"}
          </button>
        </form>

        {submitMessage ? (
          <p className="mt-3 rounded-md border border-zinc-200 bg-zinc-50 p-2 text-sm text-zinc-700">
            {submitMessage}
          </p>
        ) : null}
      </section>
    </AppShell>
  );
}
