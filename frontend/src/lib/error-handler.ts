"use client";

import { AxiosError } from "axios";
import { useRouter } from "next/navigation";

import { ValidationErrorPayload } from "@/lib/api-types";

interface ApiErrorBody {
  status?: number;
  message?: string;
  data?: ValidationErrorPayload;
}

export const parseValidationErrors = (error: unknown): Record<string, string> => {
  if (!(error instanceof AxiosError)) {
    return {};
  }

  const body = error.response?.data as ApiErrorBody | undefined;
  return body?.data?.errors ?? {};
};

export const parseErrorMessage = (error: unknown): string => {
  if (!(error instanceof AxiosError)) {
    return "Terjadi kesalahan tidak terduga.";
  }

  const body = error.response?.data as ApiErrorBody | undefined;
  return body?.message ?? "Terjadi kesalahan tidak terduga.";
};

export const useStandardErrorRedirect = () => {
  const router = useRouter();

  return (error: unknown) => {
    if (!(error instanceof AxiosError)) {
      return;
    }

    const status = error.response?.status;
    if (status === 401) {
      router.replace("/login");
      return;
    }

    if (status === 403) {
      router.replace("/forbidden");
    }
  };
};
