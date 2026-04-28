"use client";

import { AxiosError } from "axios";

import { ErrorResponse } from "@/types/api";
import { handleGlobalHttpError } from "@/lib/http-error-events";

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
  if (getStandardErrorCode(error) !== 422 || !(error instanceof AxiosError)) {
    return {};
  }

  const body = error.response?.data as ErrorResponse | undefined;
  return body?.data?.errors ?? {};
};

export const parseErrorMessage = (error: unknown): string => {
  if (!(error instanceof AxiosError)) {
    return "Terjadi kesalahan tidak terduga.";
  }

  const body = error.response?.data as ErrorResponse | undefined;
  return body?.message ?? "Terjadi kesalahan tidak terduga.";
};

export const useStandardErrorRedirect = () => {
  return (error: unknown) => {
    handleGlobalHttpError(error);
  };
};
