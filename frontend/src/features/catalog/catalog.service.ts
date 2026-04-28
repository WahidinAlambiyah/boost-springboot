import { CatalogSummary } from "@/lib/api-types";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const catalogService = {
  async getSummaries(): Promise<CatalogSummary[]> {
    const response = await api.get<ApiResponse<CatalogSummary[]>>("/api/catalog");
    return response.data.data;
  },
};
