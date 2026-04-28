import { BillingSummary, PaymentPayload, PaymentResponse } from "@/lib/api-types";
import { api } from "@/lib/api";
import { createIdempotencyKey } from "@/lib/idempotency";
import { ApiResponse } from "@/types/api";

export const billingService = {
  async getSummaries(): Promise<BillingSummary[]> {
    const response = await api.get<ApiResponse<BillingSummary[]>>("/api/billing");
    return response.data.data;
  },

  async pay(payload: PaymentPayload): Promise<ApiResponse<PaymentResponse>> {
    const response = await api.post<ApiResponse<PaymentResponse>>(
      "/api/billing/pay",
      payload,
      {
        headers: {
          "Idempotency-Key": createIdempotencyKey(),
        },
      },
    );

    return response.data;
  },
};
