"use client";

import { AxiosError } from "axios";
import { useRouter } from "next/navigation";

import { ErrorResponse } from "@/types/api";

export type StandardErrorCode = 401 | 403 | 422 | "UNKNOWN";

export const getStandardErrorCode = (error: unknown): StandardErrorCode => {
  if (!(error instanceof AxiosError)) {
    return "UNKNOWN";
  }

  const status = error.response?.status;

  if (status === 401 || status === 403 || status === 422) {
    return status;
  }

  return "UNKNOWN";
};

export const parseValidationErrors = (error: unknown): Record<string, string> => {
  if (getStandardErrorCode(error) !== 422 || !(error instanceof AxiosError<ErrorResponse>)) {
    return {};
  }

  const body = error.response?.data;
  return body?.data?.errors ?? {};
};

export const parseErrorMessage = (error: unknown): string => {
  if (!(error instanceof AxiosError<ErrorResponse>)) {
    return "Terjadi kesalahan tidak terduga.";
  }

  const body = error.response?.data;
  return body?.message ?? "Terjadi kesalahan tidak terduga.";
};

export const useStandardErrorRedirect = () => {
  const router = useRouter();

  return (error: unknown) => {
    const status = getStandardErrorCode(error);

    if (status === 401) {
      router.replace("/login");
      return;
    }

    if (status === 403) {
      router.replace("/forbidden");
    }
  };
};
